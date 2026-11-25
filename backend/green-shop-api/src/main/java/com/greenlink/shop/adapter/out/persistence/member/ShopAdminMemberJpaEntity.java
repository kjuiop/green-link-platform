package com.greenlink.shop.adapter.out.persistence.member;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_admin_members", indexes = {
    @Index(name = "idx_shop_admin_members_status", columnList = "status"),
    @Index(name = "idx_shop_admin_members_company", columnList = "company_id")
})
public class ShopAdminMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false, unique = true)
    private Long adminId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ShopRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_role", nullable = false, length = 30)
    private ShopRole requestedRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShopAdminStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ShopAdminMemberJpaEntity() {}

    static ShopAdminMemberJpaEntity from(ShopAdminMember m) {
        ShopAdminMemberJpaEntity e = new ShopAdminMemberJpaEntity();
        e.id = m.getId();
        e.apply(m);
        return e;
    }

    void apply(ShopAdminMember m) {
        this.adminId = m.getAdminId();
        this.companyId = m.getCompanyId();
        this.role = m.getRole();
        this.requestedRole = m.getRequestedRole();
        this.status = m.getStatus();
        this.requestedAt = m.getRequestedAt();
        this.processedAt = m.getProcessedAt();
        this.processedBy = m.getProcessedBy();
        this.rejectReason = m.getRejectReason();
    }

    ShopAdminMember toDomain() {
        return ShopAdminMember.of(id, adminId, companyId, role, requestedRole, status,
            requestedAt, processedAt, processedBy, rejectReason);
    }
}
