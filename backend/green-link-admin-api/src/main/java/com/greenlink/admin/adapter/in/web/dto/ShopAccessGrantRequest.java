package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record ShopAccessGrantRequest(
    @NotNull Long adminId,
    @NotNull Long companyId
) {}
