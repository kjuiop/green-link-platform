package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.auth.dto.ShopSession;

public record SessionResponse(String accessToken, String loginId, String name, String companyName,
                              String role, String accessStatus) {

    public static SessionResponse from(ShopSession s) {
        return new SessionResponse(s.accessToken(), s.loginId(), s.name(), s.companyName(),
            s.role().name(), s.accessStatus().name());
    }
}
