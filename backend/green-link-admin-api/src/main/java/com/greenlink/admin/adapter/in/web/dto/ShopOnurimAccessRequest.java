package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record ShopOnurimAccessRequest(@NotNull Long companyId, @NotNull Boolean enabled) {}
