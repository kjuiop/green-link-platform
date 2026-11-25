package com.greenlink.shopcore.domain.auth;

public enum ShopRole {
    SUPER_ADMIN(3),
    PARTNER_ADMIN(2),
    BRAND_ADMIN(1);

    private final int rank;

    ShopRole(int rank) {
        this.rank = rank;
    }

    public boolean outranks(ShopRole other) {
        return this.rank > other.rank;
    }
}
