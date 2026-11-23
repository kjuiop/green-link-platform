package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyShopAccountRequest(
    @NotBlank String loginId,
    @NotBlank String password
) {}
