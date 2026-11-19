package com.greenlink.admin.adapter.out.persistence.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRegionJpaRepository extends JpaRepository<CompanyRegionJpaEntity, Long> {
    List<CompanyRegionJpaEntity> findAllByCompanyId(Long companyId);
    List<CompanyRegionJpaEntity> findAllByCompanyIdIn(java.util.Collection<Long> companyIds);
    void deleteAllByCompanyId(Long companyId);
}
