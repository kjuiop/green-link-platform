package com.greenlink.admin.adapter.in.scheduler;

import com.greenlink.core.application.coupang.usecase.CollectCoupangOrdersUseCase;
import com.greenlink.core.domain.coupang.model.CoupangTriggerType;
import com.greenlink.core.domain.coupang.port.CoupangCredentialRepository;
import com.greenlink.core.global.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "coupang.collect.enabled", havingValue = "true")
public class CoupangOrderCollectScheduler {

    private static final Logger log = LoggerFactory.getLogger(CoupangOrderCollectScheduler.class);

    private final CoupangCredentialRepository credentialRepository;
    private final CollectCoupangOrdersUseCase collectUseCase;

    public CoupangOrderCollectScheduler(CoupangCredentialRepository credentialRepository,
                                        CollectCoupangOrdersUseCase collectUseCase) {
        this.credentialRepository = credentialRepository;
        this.collectUseCase = collectUseCase;
    }

    @Scheduled(fixedDelayString = "${coupang.collect.interval-ms:240000}", initialDelay = 30000)
    public void collectAll() {
        for (Long companyId : credentialRepository.findEnabledCompanyIds()) {
            try {
                collectUseCase.collect(companyId, CoupangTriggerType.SCHEDULED);
            } catch (BusinessException e) {
                log.warn("쿠팡 주문 자동 수집 건너뜀 companyId={} code={}", companyId, e.getErrorCode().getCode());
            } catch (Exception e) {
                log.error("쿠팡 주문 자동 수집 오류 companyId={}", companyId, e);
            }
        }
    }
}
