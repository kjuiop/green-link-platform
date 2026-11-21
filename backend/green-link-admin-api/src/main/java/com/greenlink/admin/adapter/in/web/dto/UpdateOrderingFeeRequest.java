package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateOrderingFeeRequest(
        @NotBlank String discountType,
        @Min(0) long discountValue
) {}
