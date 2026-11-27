package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.domain.admin.model.AdminRole;
import jakarta.validation.constraints.NotNull;

public record ShopRoleChangeRequest(@NotNull AdminRole role) {}
