package com.greenlink.shopcore.application.access.dto;

import com.greenlink.shopcore.domain.auth.AccessRequestType;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;

public record MyAccess(AccessStatus accessStatus, String rejectReason, AccessRequestType requestType,
                       ShopRole requestedRole) {}
