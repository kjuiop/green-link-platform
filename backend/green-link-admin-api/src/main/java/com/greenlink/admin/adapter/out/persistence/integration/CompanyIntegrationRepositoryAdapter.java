package com.greenlink.admin.adapter.out.persistence.integration;

import com.greenlink.core.domain.integration.model.CompanyIntegration;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class CompanyIntegrationRepositoryAdapter implements CompanyIntegrationRepository {

    private final CompanyIntegrationJpaRepository jpaRepository;

    public CompanyIntegrationRepositoryAdapter(CompanyIntegrationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CompanyIntegration> findByCompanyId(Long companyId) {
        return jpaRepository.findByCompanyId(companyId).map(CompanyIntegrationJpaEntity::toDomain);
    }

    @Override
    public CompanyIntegration save(CompanyIntegration integration) {
        CompanyIntegrationJpaEntity entity = jpaRepository.findByCompanyId(integration.getCompanyId())
                .orElseGet(() -> CompanyIntegrationJpaEntity.createNew(
                        integration.getCompanyId(), integration.getProvider(),
                        integration.getApiKey(), integration.getSenderNumber()));
        entity.update(integration.getProvider(), integration.getApiKey(), integration.getSenderNumber());
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    @Transactional
    public void deleteByCompanyId(Long companyId) {
        jpaRepository.deleteByCompanyId(companyId);
    }
}
