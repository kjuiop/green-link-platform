package com.greenlink.shopcore.domain.member.model;

import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;

import java.time.LocalDateTime;

public class ShopAdminMember {

    private Long id;
    private Long onurimAdminId;
    private String loginId;
    private String name;
    private String companyName;
    private ShopAdminStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private Long processedBy;
    private String rejectReason;

    private ShopAdminMember() {}

    public static ShopAdminMember request(Long onurimAdminId, String loginId, String name,
                                          String companyName, LocalDateTime now) {
        ShopAdminMember m = new ShopAdminMember();
        m.onurimAdminId = onurimAdminId;
        m.loginId = loginId;
        m.name = name;
        m.companyName = companyName;
        m.status = ShopAdminStatus.PENDING;
        m.requestedAt = now;
        return m;
    }

    public static ShopAdminMember of(Long id, Long onurimAdminId, String loginId, String name, String companyName,
                                     ShopAdminStatus status, LocalDateTime requestedAt, LocalDateTime processedAt,
                                     Long processedBy, String rejectReason) {
        ShopAdminMember m = new ShopAdminMember();
        m.id = id;
        m.onurimAdminId = onurimAdminId;
        m.loginId = loginId;
        m.name = name;
        m.companyName = companyName;
        m.status = status;
        m.requestedAt = requestedAt;
        m.processedAt = processedAt;
        m.processedBy = processedBy;
        m.rejectReason = rejectReason;
        return m;
    }

    public void reapply(LocalDateTime now) {
        if (status != ShopAdminStatus.REJECTED) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_ALREADY_REQUESTED);
        }
        this.status = ShopAdminStatus.PENDING;
        this.requestedAt = now;
        this.processedAt = null;
        this.processedBy = null;
        this.rejectReason = null;
    }

    public void approve(Long processedBy, LocalDateTime now) {
        requirePending();
        this.status = ShopAdminStatus.APPROVED;
        this.processedAt = now;
        this.processedBy = processedBy;
        this.rejectReason = null;
    }

    public void reject(String reason, Long processedBy, LocalDateTime now) {
        if (reason == null || reason.isBlank()) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_REJECT_REASON_REQUIRED);
        }
        requirePending();
        this.status = ShopAdminStatus.REJECTED;
        this.processedAt = now;
        this.processedBy = processedBy;
        this.rejectReason = reason.trim();
    }

    public void refreshSnapshot(String loginId, String name, String companyName) {
        this.loginId = loginId;
        this.name = name;
        this.companyName = companyName;
    }

    private void requirePending() {
        if (status != ShopAdminStatus.PENDING) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_INVALID_TRANSITION);
        }
    }

    public Long getId() { return id; }
    public Long getOnurimAdminId() { return onurimAdminId; }
    public String getLoginId() { return loginId; }
    public String getName() { return name; }
    public String getCompanyName() { return companyName; }
    public ShopAdminStatus getStatus() { return status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public Long getProcessedBy() { return processedBy; }
    public String getRejectReason() { return rejectReason; }
}
