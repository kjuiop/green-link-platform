package com.greenlink.shopcore.domain.member.model;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;

import java.time.LocalDateTime;

public class ShopAdminMember {

    private Long id;
    private Long adminId;
    private Long companyId;
    private ShopRole role;
    private ShopRole requestedRole;
    private ShopAdminStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private Long processedBy;
    private String rejectReason;

    private ShopAdminMember() {}

    public static ShopAdminMember request(Long adminId, Long companyId, ShopRole requestedRole, LocalDateTime now) {
        requireAssignable(requestedRole);
        ShopAdminMember m = new ShopAdminMember();
        m.adminId = adminId;
        m.companyId = companyId;
        m.requestedRole = requestedRole;
        m.status = ShopAdminStatus.PENDING;
        m.requestedAt = now;
        return m;
    }

    public static ShopAdminMember founder(Long adminId, Long companyId, LocalDateTime now) {
        ShopAdminMember m = new ShopAdminMember();
        m.adminId = adminId;
        m.companyId = companyId;
        m.role = ShopRole.PARTNER_ADMIN;
        m.requestedRole = ShopRole.PARTNER_ADMIN;
        m.status = ShopAdminStatus.APPROVED;
        m.requestedAt = now;
        m.processedAt = now;
        return m;
    }

    public static ShopAdminMember of(Long id, Long adminId, Long companyId, ShopRole role, ShopRole requestedRole,
                                     ShopAdminStatus status, LocalDateTime requestedAt, LocalDateTime processedAt,
                                     Long processedBy, String rejectReason) {
        ShopAdminMember m = new ShopAdminMember();
        m.id = id;
        m.adminId = adminId;
        m.companyId = companyId;
        m.role = role;
        m.requestedRole = requestedRole;
        m.status = status;
        m.requestedAt = requestedAt;
        m.processedAt = processedAt;
        m.processedBy = processedBy;
        m.rejectReason = rejectReason;
        return m;
    }

    public void reapply(Long companyId, ShopRole requestedRole, LocalDateTime now) {
        if (status != ShopAdminStatus.REJECTED) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_ALREADY_REQUESTED);
        }
        requireAssignable(requestedRole);
        this.companyId = companyId;
        this.requestedRole = requestedRole;
        this.status = ShopAdminStatus.PENDING;
        this.requestedAt = now;
        this.processedAt = null;
        this.processedBy = null;
        this.rejectReason = null;
    }

    public void validateApproval(ShopRole grantedRole) {
        requirePending();
        requireAssignable(grantedRole);
        if (grantedRole.outranks(requestedRole)) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_ROLE_EXCEEDS_REQUEST);
        }
    }

    public void approve(ShopRole grantedRole, Long processedBy, LocalDateTime now) {
        validateApproval(grantedRole);
        this.status = ShopAdminStatus.APPROVED;
        this.role = grantedRole;
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

    private void requirePending() {
        if (status != ShopAdminStatus.PENDING) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_INVALID_TRANSITION);
        }
    }

    private static void requireAssignable(ShopRole role) {
        if (role == null || role == ShopRole.SUPER_ADMIN) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        }
    }

    public Long getId() { return id; }
    public Long getAdminId() { return adminId; }
    public Long getCompanyId() { return companyId; }
    public ShopRole getRole() { return role; }
    public ShopRole getRequestedRole() { return requestedRole; }
    public ShopAdminStatus getStatus() { return status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public Long getProcessedBy() { return processedBy; }
    public String getRejectReason() { return rejectReason; }
}
