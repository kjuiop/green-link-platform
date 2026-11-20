package com.greenlink.core.application.shop.dto;

import com.greenlink.core.domain.shop.model.Shop;

public record ShopSettingsResult(
        boolean exists,
        Long id,
        String slug,
        boolean published,
        String displayName,
        String logoUrl,
        String phone,
        String themeColor,
        String bannerUrl,
        String bannerTitle,
        String bannerSubtitle,
        String notice,
        String deliveryGuide,
        String businessInfo
) {
    public static ShopSettingsResult from(Shop s) {
        return new ShopSettingsResult(true, s.getId(), s.getSlug(), s.isPublished(), s.getDisplayName(),
                s.getLogoUrl(), s.getPhone(), s.getThemeColor(), s.getBannerUrl(), s.getBannerTitle(),
                s.getBannerSubtitle(), s.getNotice(), s.getDeliveryGuide(), s.getBusinessInfo());
    }

    public static ShopSettingsResult notCreated(String defaultName, String defaultPhone) {
        return new ShopSettingsResult(false, null, null, false, defaultName, null, defaultPhone,
                Shop.DEFAULT_THEME_COLOR, null, null, null, null, null, null);
    }
}
