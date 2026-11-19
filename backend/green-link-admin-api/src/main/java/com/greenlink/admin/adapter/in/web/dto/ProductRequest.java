package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.product.dto.ProductCommand;
import com.greenlink.core.domain.product.model.ProductCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Size(max = 100) String name,
        @Min(0) long price,
        @Size(max = 500) String memo,
        int sortOrder,
        @NotNull ProductCategory category
) {
    public ProductCommand toCommand() {
        return new ProductCommand(name, price, memo, sortOrder, category);
    }
}
