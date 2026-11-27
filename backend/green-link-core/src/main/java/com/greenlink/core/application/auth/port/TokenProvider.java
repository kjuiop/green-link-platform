package com.greenlink.core.application.auth.port;

import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;

public interface TokenProvider {
    String generateToken(Long id, AdminRole role, CompanyType companyType, Long companyId);
}
