package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyCodeRequest(@NotBlank String phone, @NotBlank String code) {}
