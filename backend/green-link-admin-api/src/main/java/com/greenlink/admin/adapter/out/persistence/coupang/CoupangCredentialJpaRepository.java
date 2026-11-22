package com.greenlink.admin.adapter.out.persistence.coupang;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CoupangCredentialJpaRepository extends JpaRepository<CoupangCredentialJpaEntity, Long> {

    Optional<CoupangCredentialJpaEntity> findByCompanyId(Long companyId);

    @Query("select c.companyId from CoupangCredentialJpaEntity c where c.enabled = true")
    List<Long> findEnabledCompanyIds();
}
