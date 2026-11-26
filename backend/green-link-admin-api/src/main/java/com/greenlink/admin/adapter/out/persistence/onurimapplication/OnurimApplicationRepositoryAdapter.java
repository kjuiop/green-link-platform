package com.greenlink.admin.adapter.out.persistence.onurimapplication;

import com.greenlink.core.domain.onurimapplication.model.OnurimApplication;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationStatus;
import com.greenlink.core.domain.onurimapplication.port.OnurimApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OnurimApplicationRepositoryAdapter implements OnurimApplicationRepository {

    private final OnurimApplicationJpaRepository jpaRepository;

    public OnurimApplicationRepositoryAdapter(OnurimApplicationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OnurimApplication save(OnurimApplication application) {
        return jpaRepository.saveAndFlush(OnurimApplicationJpaEntity.from(application)).toDomain();
    }

    @Override
    public Optional<OnurimApplication> findById(Long id) {
        return jpaRepository.findById(id).map(OnurimApplicationJpaEntity::toDomain);
    }

    @Override
    public Optional<OnurimApplication> findLatestByCompanyId(Long companyId) {
        return jpaRepository.findFirstByCompanyIdOrderByIdDesc(companyId).map(OnurimApplicationJpaEntity::toDomain);
    }

    @Override
    public boolean existsPendingByCompanyId(Long companyId) {
        return jpaRepository.existsByPendingCompanyId(companyId);
    }

    @Override
    public Page<OnurimApplication> findAllByStatus(OnurimApplicationStatus status, Pageable pageable) {
        return jpaRepository.findAllByStatus(status, pageable).map(OnurimApplicationJpaEntity::toDomain);
    }
}
