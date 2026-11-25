package com.greenlink.shopcore.application.access.port;

public interface CompanyAccessGranter {
    void grant(Long adminId, Long companyId);
}
