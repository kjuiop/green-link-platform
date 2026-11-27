package com.greenlink.shopcore.application.access.port;

import com.greenlink.shopcore.domain.auth.ShopRole;

public interface CompanyAccessGranter {
    void grant(Long adminId, Long companyId, ShopRole role);
    void changeRole(Long adminId, ShopRole role);
}
