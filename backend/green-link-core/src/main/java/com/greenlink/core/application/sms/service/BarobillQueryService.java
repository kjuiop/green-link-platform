package com.greenlink.core.application.sms.service;

import com.greenlink.core.application.common.BusinessNumber;
import com.greenlink.core.application.sms.dto.BarobillQueryResult;
import com.greenlink.core.application.sms.usecase.BarobillQueryUseCase;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.integration.model.CompanyIntegration;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import com.greenlink.core.domain.sms.model.SmsAccount;
import com.greenlink.core.domain.sms.port.BarobillGateway;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.sms.SmsErrorCode;
import org.springframework.stereotype.Service;

@Service
public class BarobillQueryService implements BarobillQueryUseCase {

    private final BarobillGateway gateway;
    private final SmsSettingsRepository smsSettingsRepository;
    private final CompanyIntegrationRepository companyIntegrationRepository;
    private final CompanyRepository companyRepository;

    public BarobillQueryService(BarobillGateway gateway,
                                SmsSettingsRepository smsSettingsRepository,
                                CompanyIntegrationRepository companyIntegrationRepository,
                                CompanyRepository companyRepository) {
        this.gateway = gateway;
        this.smsSettingsRepository = smsSettingsRepository;
        this.companyIntegrationRepository = companyIntegrationRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public BarobillQueryResult checkSenderNumber(Long companyId) {
        SmsAccount account = resolveAccount(companyId);
        if (account.senderNumber() == null || account.senderNumber().isBlank()) {
            throw new BusinessException(SmsErrorCode.BAROBILL_NOT_CONFIGURED);
        }
        int code = gateway.checkSenderNumber(account);
        String message = switch (code) {
            case 1 -> "발신번호가 등록되어 있습니다.";
            case 2 -> "바로빌에 등록되지 않은 발신번호입니다.";
            default -> "바로빌 오류 (" + code + ")";
        };
        return new BarobillQueryResult(code, message);
    }

    @Override
    public BarobillQueryResult getSendState(Long companyId, String sendKey) {
        if (sendKey == null || sendKey.isBlank()) {
            throw new BusinessException(SmsErrorCode.BAROBILL_NOT_CONFIGURED);
        }
        int code = gateway.getSendState(resolveAccount(companyId), sendKey);
        String message = switch (code) {
            case 0 -> "전송중";
            case 1 -> "전송완료";
            default -> "전송 실패 (" + code + ")";
        };
        return new BarobillQueryResult(code, message);
    }

    private SmsAccount resolveAccount(Long companyId) {
        SmsAccount account;
        if (companyId != null) {
            CompanyIntegration ci = companyIntegrationRepository.findByCompanyId(companyId)
                    .orElseThrow(() -> new BusinessException(SmsErrorCode.BAROBILL_NOT_CONFIGURED));
            String corpNum = companyRepository.findById(companyId)
                    .map(c -> BusinessNumber.normalize(c.getBusinessNo()))
                    .filter(n -> !isBlank(n))
                    .orElseThrow(() -> new BusinessException(SmsErrorCode.BAROBILL_BUSINESS_NO_MISSING));
            account = new SmsAccount(ci.getProvider(), ci.getApiKey(), ci.getSenderNumber(), ci.getUserId(), corpNum);
        } else {
            SmsSettings global = smsSettingsRepository.get();
            account = new SmsAccount(global.getProvider(), global.getApiKey(), global.getSenderNumber(),
                    global.getUserId(), global.getCorpNum());
        }
        if (!"BAROBILL".equalsIgnoreCase(account.provider())
                || isBlank(account.apiKey()) || isBlank(account.corpNum())) {
            throw new BusinessException(SmsErrorCode.BAROBILL_NOT_CONFIGURED);
        }
        return account;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
