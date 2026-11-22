package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefundReasonRequest(@NotBlank @Size(max = 500) String reason) {}
