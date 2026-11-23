package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PriceAdjustmentRequest(
        @NotNull @Min(0) Long newTotalAmount,
        @NotBlank @Size(max = 500) String reason) {}
