package com.greenlink.core.domain.shop.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;

import java.net.URI;
import java.util.regex.Pattern;

public class Shop {

    public static final String DEFAULT_THEME_COLOR = "#15803d";
    private static final Pattern COLOR = Pattern.compile("^#[0-9a-fA-F]{6}$");

    private Long id;
    private Long companyId;
    private String slug;
    private boolean published;
    private String displayName;
    private String logoUrl;
    private String phone;
    private String themeColor;
    private String bannerUrl;
    private String bannerTitle;
    private String bannerSubtitle;
    private String notice;
    private String deliveryGuide;
    private String businessInfo;

    private Shop() {}

    public static Shop create(Long companyId, String slug, String displayName) {
        Shop s = new Shop();
        s.companyId = companyId;
        s.slug = slug;
        s.displayName = requireText(displayName, 100);
        s.themeColor = DEFAULT_THEME_COLOR;
        return s;
    }

    public static Shop of(Long id, Long companyId, String slug, boolean published, String displayName,
                          String logoUrl, String phone, String themeColor, String bannerUrl, String bannerTitle,
                          String bannerSubtitle, String notice, String deliveryGuide, String businessInfo) {
        Shop s = new Shop();
        s.id = id;
        s.companyId = companyId;
        s.slug = slug;
        s.published = published;
        s.displayName = displayName;
        s.logoUrl = logoUrl;
        s.phone = phone;
        s.themeColor = themeColor;
        s.bannerUrl = bannerUrl;
        s.bannerTitle = bannerTitle;
        s.bannerSubtitle = bannerSubtitle;
        s.notice = notice;
        s.deliveryGuide = deliveryGuide;
        s.businessInfo = businessInfo;
        return s;
    }

    public void updateSettings(String displayName, String logoUrl, String phone, String themeColor,
                               String bannerUrl, String bannerTitle, String bannerSubtitle,
                               String notice, String deliveryGuide, String businessInfo) {
        this.displayName = requireText(displayName, 100);
        this.logoUrl = imageUrl(logoUrl);
        this.phone = optionalText(phone, 30);
        this.themeColor = color(themeColor);
        this.bannerUrl = imageUrl(bannerUrl);
        this.bannerTitle = optionalText(bannerTitle, 100);
        this.bannerSubtitle = optionalText(bannerSubtitle, 200);
        this.notice = optionalText(notice, 1000);
        this.deliveryGuide = optionalText(deliveryGuide, 2000);
        this.businessInfo = optionalText(businessInfo, 1000);
    }

    public void changeSlug(String normalizedSlug) {
        this.slug = normalizedSlug;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isPublishable(long visibleProductCount) {
        return displayName != null && !displayName.isBlank()
                && phone != null && !phone.isBlank()
                && visibleProductCount > 0;
    }

    public static String requireText(String value, int max) {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty() || v.length() > max) {
            throw new BusinessException(ShopErrorCode.INVALID_SETTINGS);
        }
        return v;
    }

    public static String optionalText(String value, int max) {
        String v = value == null ? "" : value.trim();
        if (v.length() > max) {
            throw new BusinessException(ShopErrorCode.INVALID_SETTINGS);
        }
        return v.isEmpty() ? null : v;
    }

    /** 업로드된 S3 이미지(https, amazonaws.com)만 허용한다. 비어 있으면 null. */
    public static String imageUrl(String value) {
        String v = optionalText(value, 500);
        if (v == null) return null;
        try {
            URI uri = URI.create(v);
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null || !host.endsWith(".amazonaws.com")) {
                throw new BusinessException(ShopErrorCode.INVALID_SETTINGS);
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ShopErrorCode.INVALID_SETTINGS);
        }
        return v;
    }

    private static String color(String value) {
        if (value == null || value.isBlank()) return DEFAULT_THEME_COLOR;
        String v = value.trim();
        if (!COLOR.matcher(v).matches()) {
            throw new BusinessException(ShopErrorCode.INVALID_SETTINGS);
        }
        return v.toLowerCase();
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getSlug() { return slug; }
    public boolean isPublished() { return published; }
    public String getDisplayName() { return displayName; }
    public String getLogoUrl() { return logoUrl; }
    public String getPhone() { return phone; }
    public String getThemeColor() { return themeColor; }
    public String getBannerUrl() { return bannerUrl; }
    public String getBannerTitle() { return bannerTitle; }
    public String getBannerSubtitle() { return bannerSubtitle; }
    public String getNotice() { return notice; }
    public String getDeliveryGuide() { return deliveryGuide; }
    public String getBusinessInfo() { return businessInfo; }
}
