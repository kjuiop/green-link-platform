package com.greenlink.admin.adapter.out.persistence.company;

import com.greenlink.core.domain.company.model.CompanyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, Long> {
    Optional<CompanyJpaEntity> findByBusinessNo(String businessNo);
    boolean existsByBusinessNo(String businessNo);
    List<CompanyJpaEntity> findByIdIn(List<Long> ids);
    List<CompanyJpaEntity> findByType(CompanyType type);
    Optional<CompanyJpaEntity> findByCompanyName(String companyName);
}
