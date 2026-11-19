package com.greenlink.core.application.coupang.service;

import com.greenlink.core.application.coupang.dto.CoupangCredentialResult;
import com.greenlink.core.application.coupang.dto.CoupangSyncLogResult;
import com.greenlink.core.application.coupang.dto.CoupangTestResult;
import com.greenlink.core.application.coupang.dto.SaveCoupangCredentialCommand;
import com.greenlink.core.application.coupang.usecase.ManageCoupangCredentialUseCase;
import com.greenlink.core.domain.coupang.model.CoupangCredential;
import com.greenlink.core.domain.coupang.model.CoupangCredentialSecret;
import com.greenlink.core.domain.coupang.port.CoupangApiException;
import com.greenlink.core.domain.coupang.port.CoupangApiPort;
import com.greenlink.core.domain.coupang.port.CoupangCredentialRepository;
import com.greenlink.core.domain.coupang.port.CoupangSyncLogRepository;
import com.greenlink.core.domain.coupang.port.SecretCipher;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class CoupangCredentialService implements ManageCoupangCredentialUseCase {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final CoupangCredentialRepository credentialRepository;
    private final CoupangSyncLogRepository syncLogRepository;
    private final CoupangApiPort apiPort;
    private final SecretCipher cipher;
    private final CoupangSecretResolver secretResolver;

    public CoupangCredentialService(CoupangCredentialRepository credentialRepository,
                                    CoupangSyncLogRepository syncLogRepository,
                                    CoupangApiPort apiPort,
                                    SecretCipher cipher,
                                    CoupangSecretResolver secretResolver) {
        this.credentialRepository = credentialRepository;
        this.syncLogRepository = syncLogRepository;
        this.apiPort = apiPort;
        this.cipher = cipher;
        this.secretResolver = secretResolver;
    }

    @Override
    public Optional<CoupangCredentialResult> get(Long companyId) {
        return credentialRepository.findByCompanyId(companyId).map(this::toResult);
    }

    @Override
    public CoupangCredentialResult save(SaveCoupangCredentialCommand cmd) {
        if (isBlank(cmd.vendorCode()) || cmd.keyIssuedAt() == null) {
            throw new BusinessException(CoupangErrorCode.INVALID_CREDENTIAL);
        }
        Optional<CoupangCredential> existing = credentialRepository.findByCompanyId(cmd.companyId());

        String accessEnc;
        String secretEnc;
        if (existing.isPresent()) {
            accessEnc = isBlank(cmd.accessKey()) ? existing.get().getAccessKeyEnc() : cipher.encrypt(cmd.accessKey().trim());
            secretEnc = isBlank(cmd.secretKey()) ? existing.get().getSecretKeyEnc() : cipher.encrypt(cmd.secretKey().trim());
        } else {
            if (isBlank(cmd.accessKey()) || isBlank(cmd.secretKey())) {
                throw new BusinessException(CoupangErrorCode.INVALID_CREDENTIAL);
            }
            accessEnc = cipher.encrypt(cmd.accessKey().trim());
            secretEnc = cipher.encrypt(cmd.secretKey().trim());
        }

        CoupangCredential credential;
        if (existing.isPresent()) {
            credential = existing.get();
            credential.updateKeys(cmd.vendorCode().trim(), accessEnc, secretEnc, cmd.keyIssuedAt(), cmd.enabled());
        } else {
            credential = CoupangCredential.create(cmd.companyId(), cmd.vendorCode().trim(), accessEnc, secretEnc,
                    cmd.keyIssuedAt(), cmd.enabled());
        }
        return toResult(credentialRepository.save(credential));
    }

    @Override
    public CoupangTestResult test(Long companyId) {
        CoupangCredential credential = credentialRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new BusinessException(CoupangErrorCode.CREDENTIAL_NOT_FOUND));
        if (credential.isExpired(LocalDate.now(KST))) {
            return new CoupangTestResult(false, CoupangErrorCode.KEY_EXPIRED.getMessage());
        }
        CoupangCredentialSecret secret = secretResolver.resolve(credential);
        LocalDateTime to = LocalDateTime.now(KST);
        try {
            apiPort.fetchNewOrderSheets(secret, to.minusHours(1), to);
            return new CoupangTestResult(true, "쿠팡 API 연결에 성공했습니다.");
        } catch (CoupangApiException e) {
            return new CoupangTestResult(false, e.toUserMessage());
        }
    }

    @Override
    public List<CoupangSyncLogResult> recentSyncLogs(Long companyId, int limit) {
        return syncLogRepository.findRecent(companyId, Math.min(Math.max(limit, 1), 50)).stream()
                .map(CoupangSyncLogResult::from).toList();
    }

    private CoupangCredentialResult toResult(CoupangCredential c) {
        LocalDate today = LocalDate.now(KST);
        Long daysLeft = c.getKeyExpiresAt() == null ? null : ChronoUnit.DAYS.between(today, c.getKeyExpiresAt());
        return new CoupangCredentialResult(c.getCompanyId(), c.getVendorCode(), maskAccessKey(c),
                c.getSecretKeyEnc() != null, c.getKeyIssuedAt(), c.getKeyExpiresAt(), daysLeft,
                c.isExpired(today), c.isEnabled(), c.getLastCollectedAt(), c.getLastError(), c.getLastErrorAt());
    }

    private String maskAccessKey(CoupangCredential c) {
        String plain = cipher.decrypt(c.getAccessKeyEnc());
        if (plain == null || plain.length() <= 4) return "****";
        return "****" + plain.substring(plain.length() - 4);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
