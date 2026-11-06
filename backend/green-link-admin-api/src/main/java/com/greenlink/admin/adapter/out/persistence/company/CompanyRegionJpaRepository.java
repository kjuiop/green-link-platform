package com.greenlink.admin.adapter.out.persistence.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRegionJpaRepository extends JpaRepository<CompanyRegionJpaEntity, Long> {
    List<CompanyRegionJpaEntity> findAllByCompanyId(Long companyId);
    void deleteAllByCompanyId(Long companyId);
}
