package com.greenlink.shopcore.application.access.dto;

import com.greenlink.shopcore.domain.auth.AccessStatus;

public record MyAccess(AccessStatus accessStatus, String rejectReason) {}
