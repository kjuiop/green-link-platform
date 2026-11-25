package com.greenlink.shopcore.application.auth.dto;

import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;

public record ShopSession(String accessToken, String loginId, String name, Long companyId, String companyName,
                          ShopRole role, AccessStatus accessStatus) {}
