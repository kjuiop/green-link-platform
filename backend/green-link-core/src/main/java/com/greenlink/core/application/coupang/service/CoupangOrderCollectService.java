package com.greenlink.core.application.coupang.service;

import com.greenlink.core.application.coupang.dto.CoupangSyncResult;
import com.greenlink.core.application.coupang.usecase.CollectCoupangOrdersUseCase;
import com.greenlink.core.domain.coupang.model.CoupangCredential;
import com.greenlink.core.domain.coupang.model.CoupangCredentialSecret;
import com.greenlink.core.domain.coupang.model.CoupangOrderSheet;
import com.greenlink.core.domain.coupang.model.CoupangSyncLog;
import com.greenlink.core.domain.coupang.model.CoupangTriggerType;
import com.greenlink.core.domain.coupang.port.CoupangApiException;
import com.greenlink.core.domain.coupang.port.CoupangApiPort;
import com.greenlink.core.domain.coupang.port.CoupangCredentialRepository;
import com.greenlink.core.domain.coupang.port.CoupangSyncLogRepository;
import com.greenlink.core.domain.customerorder.model.CustomerOrder;
import com.greenlink.core.domain.customerorder.port.CustomerOrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CoupangOrderCollectService implements CollectCoupangOrdersUseCase {

    private static final Logger log = LoggerFactory.getLogger(CoupangOrderCollectService.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int DEFAULT_LOOKBACK_HOURS = 24;
    private static final int MAX_CATCH_UP_DAYS = 7;

    private final CoupangCredentialRepository credentialRepository;
    private final CustomerOrderRepository orderRepository;
    private final CoupangSyncLogRepository syncLogRepository;
    private final CoupangApiPort apiPort;
    private final CoupangSecretResolver secretResolver;
    private final int overlapMinutes;
    private final int maxRangeHours;

    private final Set<Long> running = ConcurrentHashMap.newKeySet();

    public CoupangOrderCollectService(CoupangCredentialRepository credentialRepository,
                                      CustomerOrderRepository orderRepository,
                                      CoupangSyncLogRepository syncLogRepository,
                                      CoupangApiPort apiPort,
                                      CoupangSecretResolver secretResolver,
                                      @Value("${coupang.collect.overlap-minutes:10}") int overlapMinutes,
                                      @Value("${coupang.collect.max-range-hours:12}") int maxRangeHours) {
        this.credentialRepository = credentialRepository;
        this.orderRepository = orderRepository;
        this.syncLogRepository = syncLogRepository;
        this.apiPort = apiPort;
        this.secretResolver = secretResolver;
        this.overlapMinutes = overlapMinutes;
        this.maxRangeHours = Math.max(maxRangeHours, 1);
    }

    @Override
    public CoupangSyncResult collect(Long companyId, CoupangTriggerType trigger) {
        CoupangCredential credential = credentialRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new BusinessException(CoupangErrorCode.CREDENTIAL_NOT_FOUND));
        if (credential.isExpired(LocalDate.now(KST))) {
            String message = CoupangErrorCode.KEY_EXPIRED.getMessage();
            if (!message.equals(credential.getLastError())) {
                credential.recordError(message, LocalDateTime.now(KST));
                credentialRepository.save(credential);
            }
            throw new BusinessException(CoupangErrorCode.KEY_EXPIRED);
        }
        if (!running.add(companyId)) {
            throw new BusinessException(CoupangErrorCode.ALREADY_COLLECTING);
        }

        LocalDateTime end = LocalDateTime.now(KST);
        LocalDateTime start = resolveStart(credential, end);
        try {
            CoupangCredentialSecret secret = secretResolver.resolve(credential);
            int fetched = 0;
            int inserted = 0;
            LocalDateTime cursor = start;
            while (cursor.isBefore(end)) {
                LocalDateTime chunkEnd = cursor.plusHours(maxRangeHours);
                if (chunkEnd.isAfter(end)) chunkEnd = end;
                List<CoupangOrderSheet> sheets = apiPort.fetchNewOrderSheets(secret, cursor, chunkEnd);
                fetched += sheets.size();
                for (CoupangOrderSheet sheet : sheets) {
                    if (orderRepository.insertIfAbsent(CustomerOrder.fromCoupangSheet(companyId, sheet))) {
                        inserted++;
                    }
                }
                cursor = chunkEnd;
            }
            credential.recordSuccess(end);
            credentialRepository.save(credential);
            syncLogRepository.save(CoupangSyncLog.success(companyId, trigger, start, end, fetched, inserted));
            return new CoupangSyncResult(start, end, fetched, inserted);
        } catch (CoupangApiException e) {
            log.warn("쿠팡 주문 수집 실패 companyId={} httpStatus={}", companyId, e.getHttpStatus());
            recordFailure(credential, companyId, trigger, start, end, e.toUserMessage());
            throw new BusinessException(CoupangErrorCode.COLLECT_FAILED);
        } catch (BusinessException e) {
            recordFailure(credential, companyId, trigger, start, end, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("쿠팡 주문 수집 중 내부 오류 companyId={}", companyId, e);
            recordFailure(credential, companyId, trigger, start, end, "수집 중 내부 오류가 발생했습니다. 서버 로그를 확인해 주세요.");
            throw new BusinessException(CoupangErrorCode.COLLECT_FAILED);
        } finally {
            running.remove(companyId);
        }
    }

    private LocalDateTime resolveStart(CoupangCredential credential, LocalDateTime end) {
        LocalDateTime base = credential.getLastCollectedAt() != null
                ? credential.getLastCollectedAt().minusMinutes(overlapMinutes)
                : end.minusHours(DEFAULT_LOOKBACK_HOURS);
        LocalDateTime earliest = end.minusDays(MAX_CATCH_UP_DAYS);
        return base.isBefore(earliest) ? earliest : base;
    }

    private void recordFailure(CoupangCredential credential, Long companyId, CoupangTriggerType trigger,
                               LocalDateTime start, LocalDateTime end, String message) {
        credential.recordError(message, LocalDateTime.now(KST));
        credentialRepository.save(credential);
        syncLogRepository.save(CoupangSyncLog.failure(companyId, trigger, start, end, message));
    }
}
