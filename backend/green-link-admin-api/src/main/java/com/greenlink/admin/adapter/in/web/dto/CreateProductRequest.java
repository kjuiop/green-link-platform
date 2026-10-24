package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.product.dto.CreateProductCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateProductRequest(
        @NotBlank String name,
        @Min(0) long price,
        String memo,
        Long orderingCompanyId
) {
    public CreateProductCommand toCommand(Long resolvedOrderingCompanyId) {
        return new CreateProductCommand(name, price, memo, resolvedOrderingCompanyId);
    }
}
