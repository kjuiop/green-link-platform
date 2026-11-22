package com.greenlink.admin.adapter.out.persistence.point;

import com.greenlink.core.domain.point.model.CompanyPoint;
import com.greenlink.core.domain.point.port.CompanyPointRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CompanyPointRepositoryAdapter implements CompanyPointRepository {

    private final CompanyPointJpaRepository jpaRepository;

    public CompanyPointRepositoryAdapter(CompanyPointJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CompanyPoint> findByCompanyId(Long companyId) {
        return jpaRepository.findByCompanyId(companyId).map(CompanyPointJpaEntity::toDomain);
    }

    @Override
    public Optional<CompanyPoint> findByCompanyIdForUpdate(Long companyId) {
        return jpaRepository.findByCompanyIdForUpdate(companyId).map(CompanyPointJpaEntity::toDomain);
    }

    @Override
    public List<CompanyPoint> findAllByCompanyIdIn(List<Long> companyIds) {
        if (companyIds.isEmpty()) return List.of();
        return jpaRepository.findByCompanyIdIn(companyIds).stream()
                .map(CompanyPointJpaEntity::toDomain)
                .toList();
    }

    @Override
    public CompanyPoint save(CompanyPoint point) {
        return jpaRepository.save(CompanyPointJpaEntity.from(point)).toDomain();
    }
}
