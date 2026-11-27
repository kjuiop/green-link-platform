package com.greenlink.shopcore.application.auth.dto;

import com.greenlink.shopcore.domain.auth.ShopRole;

public record ManagedAccountCommand(String loginId, String name, String phone, String email, Long companyId, ShopRole role) {}
