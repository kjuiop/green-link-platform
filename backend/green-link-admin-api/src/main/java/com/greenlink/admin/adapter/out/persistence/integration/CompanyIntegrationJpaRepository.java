package com.greenlink.admin.adapter.out.persistence.integration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyIntegrationJpaRepository extends JpaRepository<CompanyIntegrationJpaEntity, Long> {
    Optional<CompanyIntegrationJpaEntity> findByCompanyId(Long companyId);
    void deleteByCompanyId(Long companyId);
}
