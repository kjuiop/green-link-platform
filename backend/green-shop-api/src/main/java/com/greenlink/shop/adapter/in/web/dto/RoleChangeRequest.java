package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.domain.auth.ShopRole;
import jakarta.validation.constraints.NotNull;

public record RoleChangeRequest(@NotNull ShopRole role) {}
