package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateVendorStatusRequest(@NotBlank String action) {}
