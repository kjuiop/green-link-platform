package com.greenlink.core.application.coupang.dto;

import com.greenlink.core.domain.coupang.model.CoupangSyncLog;

import java.time.LocalDateTime;

public record CoupangSyncLogResult(
        Long id,
        String triggerType,
        LocalDateTime rangeFrom,
        LocalDateTime rangeTo,
        int fetchedCount,
        int insertedCount,
        boolean success,
        String errorMessage,
        LocalDateTime createdAt
) {
    public static CoupangSyncLogResult from(CoupangSyncLog l) {
        return new CoupangSyncLogResult(l.getId(), l.getTriggerType().name(), l.getRangeFrom(), l.getRangeTo(),
                l.getFetchedCount(), l.getInsertedCount(), l.isSuccess(), l.getErrorMessage(), l.getCreatedAt());
    }
}
