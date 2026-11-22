package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.shop.dto.SaveShopSettingsCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveShopSettingsRequest(
        @NotBlank @Size(max = 30) String slug,
        @NotBlank @Size(max = 100) String displayName,
        @Size(max = 500) String logoUrl,
        @Size(max = 30) String phone,
        @Size(max = 7) String themeColor,
        @Size(max = 500) String bannerUrl,
        @Size(max = 100) String bannerTitle,
        @Size(max = 200) String bannerSubtitle,
        @Size(max = 2000) String notice,
        @Size(max = 4000) String deliveryGuide,
        @Size(max = 2000) String businessInfo
) {
    public SaveShopSettingsCommand toCommand() {
        return new SaveShopSettingsCommand(slug, displayName, logoUrl, phone, themeColor, bannerUrl,
                bannerTitle, bannerSubtitle, notice, deliveryGuide, businessInfo);
    }
}
