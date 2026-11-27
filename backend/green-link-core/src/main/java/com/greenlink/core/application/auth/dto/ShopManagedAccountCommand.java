package com.greenlink.core.application.auth.dto;

import com.greenlink.core.domain.admin.model.AdminRole;

public record ShopManagedAccountCommand(
    String loginId,
    String name,
    String phone,
    String email,
    Long companyId,
    AdminRole role
) {}
