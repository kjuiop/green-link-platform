package com.greenlink.shopcore.domain.auth;

import com.greenlink.shopcore.domain.brand.model.ShopBrand;

import java.util.Objects;
import java.util.Set;

public final class ShopAccessPolicy {

    private ShopAccessPolicy() {}

    public static boolean canAccessBrand(ShopRole role, Long actorCompanyId,
                                         Set<Long> assignedBrandIds, ShopBrand brand) {
        if (role == null) {
            return false;
        }
        return switch (role) {
            case SUPER_ADMIN -> true;
            case PARTNER_ADMIN -> sameCompany(actorCompanyId, brand);
            case BRAND_ADMIN -> sameCompany(actorCompanyId, brand)
                    && assignedBrandIds != null && assignedBrandIds.contains(brand.getId());
        };
    }

    public static boolean canManageCompany(ShopRole role, Long actorCompanyId, Long targetCompanyId) {
        if (role == null) {
            return false;
        }
        return switch (role) {
            case SUPER_ADMIN -> true;
            case PARTNER_ADMIN -> actorCompanyId != null && Objects.equals(actorCompanyId, targetCompanyId);
            case BRAND_ADMIN -> false;
        };
    }

    private static boolean sameCompany(Long actorCompanyId, ShopBrand brand) {
        return actorCompanyId != null && Objects.equals(actorCompanyId, brand.getCompanyId());
    }
}
