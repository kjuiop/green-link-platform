package com.greenlink.core.application.shop.dto;

import com.greenlink.core.domain.shop.model.Shop;
import com.greenlink.core.domain.shop.model.ShopProduct;

import java.util.List;

public record PublicShopView(
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
        String businessInfo,
        List<PublicProductView> products
) {
    public static PublicShopView of(Shop s, List<ShopProduct> products) {
        return new PublicShopView(s.getSlug(), s.getDisplayName(), s.getLogoUrl(), s.getPhone(), s.getThemeColor(),
                s.getBannerUrl(), s.getBannerTitle(), s.getBannerSubtitle(), s.getNotice(), s.getDeliveryGuide(),
                s.getBusinessInfo(), products.stream().map(PublicProductView::from).toList());
    }
}
