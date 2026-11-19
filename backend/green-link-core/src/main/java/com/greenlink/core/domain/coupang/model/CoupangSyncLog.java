package com.greenlink.core.domain.coupang.model;

import java.time.LocalDateTime;

public class CoupangSyncLog {

    private Long id;
    private Long companyId;
    private CoupangTriggerType triggerType;
    private LocalDateTime rangeFrom;
    private LocalDateTime rangeTo;
    private int fetchedCount;
    private int insertedCount;
    private boolean success;
    private String errorMessage;
    private LocalDateTime createdAt;

    private CoupangSyncLog() {}

    public static CoupangSyncLog success(Long companyId, CoupangTriggerType trigger, LocalDateTime from,
                                         LocalDateTime to, int fetched, int inserted) {
        CoupangSyncLog l = new CoupangSyncLog();
        l.companyId = companyId;
        l.triggerType = trigger;
        l.rangeFrom = from;
        l.rangeTo = to;
        l.fetchedCount = fetched;
        l.insertedCount = inserted;
        l.success = true;
        return l;
    }

    public static CoupangSyncLog failure(Long companyId, CoupangTriggerType trigger, LocalDateTime from,
                                         LocalDateTime to, String errorMessage) {
        CoupangSyncLog l = new CoupangSyncLog();
        l.companyId = companyId;
        l.triggerType = trigger;
        l.rangeFrom = from;
        l.rangeTo = to;
        l.success = false;
        l.errorMessage = errorMessage != null && errorMessage.length() > 500
                ? errorMessage.substring(0, 500) : errorMessage;
        return l;
    }

    public static CoupangSyncLog of(Long id, Long companyId, CoupangTriggerType triggerType,
                                    LocalDateTime rangeFrom, LocalDateTime rangeTo, int fetchedCount,
                                    int insertedCount, boolean success, String errorMessage,
                                    LocalDateTime createdAt) {
        CoupangSyncLog l = new CoupangSyncLog();
        l.id = id;
        l.companyId = companyId;
        l.triggerType = triggerType;
        l.rangeFrom = rangeFrom;
        l.rangeTo = rangeTo;
        l.fetchedCount = fetchedCount;
        l.insertedCount = insertedCount;
        l.success = success;
        l.errorMessage = errorMessage;
        l.createdAt = createdAt;
        return l;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public CoupangTriggerType getTriggerType() { return triggerType; }
    public LocalDateTime getRangeFrom() { return rangeFrom; }
    public LocalDateTime getRangeTo() { return rangeTo; }
    public int getFetchedCount() { return fetchedCount; }
    public int getInsertedCount() { return insertedCount; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
