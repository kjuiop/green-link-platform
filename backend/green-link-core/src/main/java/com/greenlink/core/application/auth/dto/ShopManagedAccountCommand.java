package com.greenlink.core.application.auth.dto;

public record ShopManagedAccountCommand(
    String loginId,
    String name,
    String phone,
    String email,
    Long companyId
) {}
