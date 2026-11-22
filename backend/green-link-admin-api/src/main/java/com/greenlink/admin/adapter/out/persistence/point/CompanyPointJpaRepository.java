package com.greenlink.admin.adapter.out.persistence.point;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyPointJpaRepository extends JpaRepository<CompanyPointJpaEntity, Long> {
    Optional<CompanyPointJpaEntity> findByCompanyId(Long companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from CompanyPointJpaEntity p where p.companyId = :companyId")
    Optional<CompanyPointJpaEntity> findByCompanyIdForUpdate(@Param("companyId") Long companyId);

    List<CompanyPointJpaEntity> findByCompanyIdIn(List<Long> companyIds);
}
