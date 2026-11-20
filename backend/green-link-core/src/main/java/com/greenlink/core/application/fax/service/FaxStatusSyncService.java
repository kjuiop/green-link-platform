package com.greenlink.core.application.fax.service;

import com.greenlink.core.application.fax.usecase.SyncFaxStatusUseCase;
import com.greenlink.core.domain.fax.model.FaxMessage;
import com.greenlink.core.domain.fax.model.FaxStatus;
import com.greenlink.core.domain.fax.port.FaxGateway;
import com.greenlink.core.domain.fax.port.FaxGateway.FaxAccount;
import com.greenlink.core.domain.fax.port.FaxGateway.FaxState;
import com.greenlink.core.domain.fax.port.FaxMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class FaxStatusSyncService implements SyncFaxStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(FaxStatusSyncService.class);
    private static final int BATCH_SIZE = 50;
    private static final Duration STALE_SENDING = Duration.ofMinutes(10);
    private static final Duration SUBMITTED_TIMEOUT = Duration.ofHours(24);

    private final FaxMessageRepository repository;
    private final FaxGateway gateway;
    private final FaxAccountResolver accountResolver;

    public FaxStatusSyncService(FaxMessageRepository repository, FaxGateway gateway, FaxAccountResolver accountResolver) {
        this.repository = repository;
        this.gateway = gateway;
        this.accountResolver = accountResolver;
    }

    @Override
    @Transactional
    public void sync() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        for (FaxMessage m : repository.findByStatus(FaxStatus.SENDING, BATCH_SIZE)) {
            if (m.getRequestedAt() != null && m.getRequestedAt().plus(STALE_SENDING).isBefore(now)) {
                m.markFailed("발송 처리 중 중단되었습니다.", now);
                repository.save(m);
            }
        }
        var submitted = repository.findByStatus(FaxStatus.SUBMITTED, BATCH_SIZE);
        if (submitted.isEmpty()) return;
        Optional<FaxAccount> account = accountResolver.resolve();
        if (account.isEmpty()) return;
        for (FaxMessage m : submitted) {
            try {
                FaxState state = gateway.getState(account.get(), m.getSendKey());
                if (state == FaxState.DELIVERED) {
                    m.markDelivered(now);
                    repository.save(m);
                } else if (state == FaxState.FAILED) {
                    m.markFailed("팩스 전송에 실패했습니다.", now);
                    repository.save(m);
                } else if (m.getRequestedAt() != null && m.getRequestedAt().plus(SUBMITTED_TIMEOUT).isBefore(now)) {
                    m.markFailed("팩스 전송 결과를 확인하지 못했습니다.", now);
                    repository.save(m);
                }
            } catch (Exception e) {
                log.warn("[팩스 상태 조회 실패] id={} error={}", m.getId(), e.getClass().getSimpleName());
            }
        }
    }
}
