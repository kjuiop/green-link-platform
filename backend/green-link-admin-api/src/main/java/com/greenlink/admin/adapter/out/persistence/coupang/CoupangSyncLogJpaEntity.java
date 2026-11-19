package com.greenlink.admin.adapter.out.persistence.coupang;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.coupang.model.CoupangSyncLog;
import com.greenlink.core.domain.coupang.model.CoupangTriggerType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupang_sync_logs",
        indexes = @Index(name = "idx_coupang_sync_logs_company_created", columnList = "company_id, created_at"))
public class CoupangSyncLogJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CoupangTriggerType triggerType;

    private LocalDateTime rangeFrom;

    private LocalDateTime rangeTo;

    @Column(nullable = false)
    private int fetchedCount;

    @Column(nullable = false)
    private int insertedCount;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 500)
    private String errorMessage;

    protected CoupangSyncLogJpaEntity() {}

    static CoupangSyncLogJpaEntity from(CoupangSyncLog l) {
        CoupangSyncLogJpaEntity e = new CoupangSyncLogJpaEntity();
        e.companyId = l.getCompanyId();
        e.triggerType = l.getTriggerType();
        e.rangeFrom = l.getRangeFrom();
        e.rangeTo = l.getRangeTo();
        e.fetchedCount = l.getFetchedCount();
        e.insertedCount = l.getInsertedCount();
        e.success = l.isSuccess();
        e.errorMessage = l.getErrorMessage();
        return e;
    }

    CoupangSyncLog toDomain() {
        return CoupangSyncLog.of(id, companyId, triggerType, rangeFrom, rangeTo, fetchedCount, insertedCount,
                success, errorMessage, getCreatedAt());
    }
}
