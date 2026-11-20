package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LookupShopOrderRequest(
        @NotBlank @Size(max = 40) String orderNo,
        @NotBlank @Size(max = 30) String ordererPhone
) {}
