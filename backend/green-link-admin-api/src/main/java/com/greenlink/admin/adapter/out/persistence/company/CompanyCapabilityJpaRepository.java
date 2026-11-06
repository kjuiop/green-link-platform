package com.greenlink.admin.adapter.out.persistence.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyCapabilityJpaRepository extends JpaRepository<CompanyCapabilityJpaEntity, Long> {
    List<CompanyCapabilityJpaEntity> findAllByCompanyId(Long companyId);
    void deleteAllByCompanyId(Long companyId);
}
