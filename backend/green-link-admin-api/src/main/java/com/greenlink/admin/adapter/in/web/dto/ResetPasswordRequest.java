package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String phone,
        @NotBlank String loginId,
        @NotBlank @Size(min = 8) String newPassword
) {}
