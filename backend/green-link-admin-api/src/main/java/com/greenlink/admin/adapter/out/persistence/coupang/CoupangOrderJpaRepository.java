package com.greenlink.admin.adapter.out.persistence.coupang;

import com.greenlink.core.domain.coupang.model.CoupangOrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CoupangOrderJpaRepository extends JpaRepository<CoupangOrderJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from CoupangOrderJpaEntity o where o.id = :id")
    Optional<CoupangOrderJpaEntity> findByIdForUpdate(@Param("id") Long id);

    boolean existsByCompanyIdAndShipmentBoxId(Long companyId, long shipmentBoxId);

    Page<CoupangOrderJpaEntity> findAllByOrderByOrderedAtDescIdDesc(Pageable pageable);

    Page<CoupangOrderJpaEntity> findByCompanyIdOrderByOrderedAtDescIdDesc(Long companyId, Pageable pageable);

    Page<CoupangOrderJpaEntity> findByStatusOrderByOrderedAtDescIdDesc(CoupangOrderStatus status, Pageable pageable);

    Page<CoupangOrderJpaEntity> findByCompanyIdAndStatusOrderByOrderedAtDescIdDesc(
            Long companyId, CoupangOrderStatus status, Pageable pageable);

    long countByStatus(CoupangOrderStatus status);

    long countByCompanyIdAndStatus(Long companyId, CoupangOrderStatus status);
}
