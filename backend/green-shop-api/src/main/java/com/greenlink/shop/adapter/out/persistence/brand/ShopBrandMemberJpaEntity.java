package com.greenlink.shop.adapter.out.persistence.brand;

import com.greenlink.shopcore.domain.brand.model.ShopBrandMember;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_brand_members",
    uniqueConstraints = @UniqueConstraint(name = "uk_shop_brand_members_brand_member", columnNames = {"brand_id", "member_id"}),
    indexes = @Index(name = "idx_shop_brand_members_member", columnList = "member_id"))
public class ShopBrandMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected ShopBrandMemberJpaEntity() {}

    static ShopBrandMemberJpaEntity from(ShopBrandMember m) {
        ShopBrandMemberJpaEntity e = new ShopBrandMemberJpaEntity();
        e.id = m.getId();
        e.brandId = m.getBrandId();
        e.memberId = m.getMemberId();
        e.deletedAt = m.getDeletedAt();
        return e;
    }

    ShopBrandMember toDomain() {
        return ShopBrandMember.of(id, brandId, memberId, deletedAt);
    }

    Long getBrandId() { return brandId; }

    Long getMemberId() { return memberId; }

    void restore() { this.deletedAt = null; }

    void softDelete(LocalDateTime now) {
        if (deletedAt == null) {
            this.deletedAt = now;
        }
    }
}
