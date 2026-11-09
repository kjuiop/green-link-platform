package com.greenlink.core.domain.integration.port;

import com.greenlink.core.domain.integration.model.CompanyIntegration;

import java.util.Optional;

public interface CompanyIntegrationRepository {
    Optional<CompanyIntegration> findByCompanyId(Long companyId);
    CompanyIntegration save(CompanyIntegration integration);
    void deleteByCompanyId(Long companyId);
}
