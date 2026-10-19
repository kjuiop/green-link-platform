package com.greenlink.admin.adapter.out.persistence.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminJpaRepository extends JpaRepository<AdminJpaEntity, Long> {
    List<AdminJpaEntity> findByCompanyIdIsNotNull();
    List<AdminJpaEntity> findByCompanyId(Long companyId);
}
