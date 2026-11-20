package com.greenlink.admin.adapter.in.scheduler;

import com.greenlink.core.application.fax.usecase.SyncFaxStatusUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FaxStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(FaxStatusScheduler.class);

    private final SyncFaxStatusUseCase syncFaxStatusUseCase;

    public FaxStatusScheduler(SyncFaxStatusUseCase syncFaxStatusUseCase) {
        this.syncFaxStatusUseCase = syncFaxStatusUseCase;
    }

    @Scheduled(fixedDelayString = "${fax.status-sync.interval-ms:60000}", initialDelay = 60000)
    public void sync() {
        try {
            syncFaxStatusUseCase.sync();
        } catch (Exception e) {
            log.warn("[팩스 상태 동기화 실패] error={}", e.getClass().getSimpleName());
        }
    }
}
