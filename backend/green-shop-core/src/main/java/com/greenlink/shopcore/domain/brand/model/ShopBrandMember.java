package com.greenlink.shopcore.domain.brand.model;

import java.time.LocalDateTime;

public class ShopBrandMember {

    private Long id;
    private Long brandId;
    private Long memberId;
    private LocalDateTime deletedAt;

    private ShopBrandMember() {}

    public static ShopBrandMember assign(Long brandId, Long memberId) {
        ShopBrandMember m = new ShopBrandMember();
        m.brandId = brandId;
        m.memberId = memberId;
        return m;
    }

    public static ShopBrandMember of(Long id, Long brandId, Long memberId, LocalDateTime deletedAt) {
        ShopBrandMember m = assign(brandId, memberId);
        m.id = id;
        m.deletedAt = deletedAt;
        return m;
    }

    public void unassign(LocalDateTime now) {
        if (deletedAt == null) {
            this.deletedAt = now;
        }
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    public Long getId() { return id; }
    public Long getBrandId() { return brandId; }
    public Long getMemberId() { return memberId; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
