package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.auth.dto.ShopSession;

public record SessionResponse(String accessToken, String loginId, String name, Long companyId, String companyName,
                              String role, String accessStatus) {

    public static SessionResponse from(ShopSession s) {
        return new SessionResponse(s.accessToken(), s.loginId(), s.name(), s.companyId(), s.companyName(),
            s.role() == null ? null : s.role().name(), s.accessStatus().name());
    }
}
