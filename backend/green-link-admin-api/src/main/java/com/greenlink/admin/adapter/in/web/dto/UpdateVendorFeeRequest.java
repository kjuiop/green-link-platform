package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateVendorFeeRequest(
        @Min(0) @Max(10_000_000) long vendorFee
) {}
