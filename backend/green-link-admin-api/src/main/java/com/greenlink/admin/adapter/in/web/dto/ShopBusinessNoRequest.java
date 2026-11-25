package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ShopBusinessNoRequest(
    @NotBlank String businessNo
) {}
