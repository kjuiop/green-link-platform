package com.greenlink.shopcore.application.common;

import com.greenlink.shopcore.domain.auth.ShopRole;

public record ManagementActor(Long adminId, Long companyId, ShopRole role) {}
