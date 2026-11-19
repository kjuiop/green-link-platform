package com.greenlink.admin.adapter.out.persistence.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminJpaRepository extends JpaRepository<AdminJpaEntity, Long> {
    List<AdminJpaEntity> findByCompanyId(Long companyId);
    List<AdminJpaEntity> findByIdIn(java.util.Collection<Long> ids);
    List<AdminJpaEntity> findByCompanyIdInAndDeletedAtIsNull(java.util.Collection<Long> companyIds);
    java.util.Optional<AdminJpaEntity> findByPhone(String phone);
}
