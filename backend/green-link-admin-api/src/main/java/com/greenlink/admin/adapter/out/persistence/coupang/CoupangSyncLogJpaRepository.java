package com.greenlink.admin.adapter.out.persistence.coupang;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoupangSyncLogJpaRepository extends JpaRepository<CoupangSyncLogJpaEntity, Long> {

    List<CoupangSyncLogJpaEntity> findByCompanyIdOrderByIdDesc(Long companyId, Pageable pageable);
}
