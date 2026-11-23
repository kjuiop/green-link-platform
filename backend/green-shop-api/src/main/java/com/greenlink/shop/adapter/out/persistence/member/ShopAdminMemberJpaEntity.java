package com.greenlink.shop.adapter.out.persistence.member;

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
@Table(name = "shop_admin_members", indexes = @Index(name = "idx_shop_admin_members_status", columnList = "status"))
public class ShopAdminMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "onurim_admin_id", nullable = false, unique = true)
    private Long onurimAdminId;

    @Column(name = "login_id", nullable = false, length = 100)
    private String loginId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "company_name", length = 200)
    private String companyName;

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
        this.onurimAdminId = m.getOnurimAdminId();
        this.loginId = m.getLoginId();
        this.name = m.getName();
        this.companyName = m.getCompanyName();
        this.status = m.getStatus();
        this.requestedAt = m.getRequestedAt();
        this.processedAt = m.getProcessedAt();
        this.processedBy = m.getProcessedBy();
        this.rejectReason = m.getRejectReason();
    }

    ShopAdminMember toDomain() {
        return ShopAdminMember.of(id, onurimAdminId, loginId, name, companyName, status,
            requestedAt, processedAt, processedBy, rejectReason);
    }
}
