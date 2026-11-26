package com.greenlink.admin.adapter.out.persistence.onurimapplication;

import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OnurimApplicationJpaRepository extends JpaRepository<OnurimApplicationJpaEntity, Long> {

    Optional<OnurimApplicationJpaEntity> findFirstByCompanyIdOrderByIdDesc(Long companyId);

    boolean existsByPendingCompanyId(Long pendingCompanyId);

    Page<OnurimApplicationJpaEntity> findAllByStatus(OnurimApplicationStatus status, Pageable pageable);
}
