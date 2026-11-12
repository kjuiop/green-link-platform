package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeVendorPasswordRequest(
    @NotBlank @Size(min = 4, max = 100) String newPassword
) {}
