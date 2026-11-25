package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchShopAccountsRequest(
    @NotNull @Size(max = 100) List<Long> adminIds
) {}
