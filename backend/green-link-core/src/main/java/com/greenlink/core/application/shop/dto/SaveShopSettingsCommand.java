package com.greenlink.core.application.shop.dto;

public record SaveShopSettingsCommand(
        String slug,
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
) {}
