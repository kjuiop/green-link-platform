package com.greenlink.shopcore.application.auth.dto;

import com.greenlink.shopcore.domain.auth.AccessRequestType;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;

public record AccessDecision(AccessStatus status, ShopRole role, TokenScope scope, String rejectReason,
                             AccessRequestType requestType, ShopRole requestedRole) {}
