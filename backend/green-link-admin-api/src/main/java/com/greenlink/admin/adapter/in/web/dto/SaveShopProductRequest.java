package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.shop.dto.SaveShopProductCommand;
import com.greenlink.core.domain.shop.model.ShopProductCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveShopProductRequest(
        @NotNull ShopProductCategory category,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 2000) String description,
        @Min(0) @Max(100_000_000) long price,
        @Size(max = 500) String imageUrl,
        boolean visible
) {
    public SaveShopProductCommand toCommand() {
        return new SaveShopProductCommand(category, name, description, price, imageUrl, visible);
    }
}
