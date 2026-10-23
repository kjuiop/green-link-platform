package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.product.dto.UpdateProductCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateProductRequest(
        @NotBlank String name,
        @Min(0) long price,
        String memo
) {
    public UpdateProductCommand toCommand() {
        return new UpdateProductCommand(name, price, memo);
    }
}
