package com.greenlink.shopcore.application.auth.dto;

import com.greenlink.shopcore.domain.auth.AccessRequestType;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;

import java.util.Set;

public record AccessDecision(AccessStatus status, ShopRole role, TokenScope scope, String rejectReason,
                             AccessRequestType requestType, ShopRole requestedRole,
                             Set<Long> requestedBrandIds) {}
