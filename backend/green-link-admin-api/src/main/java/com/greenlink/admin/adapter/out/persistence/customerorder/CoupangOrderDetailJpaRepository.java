package com.greenlink.admin.adapter.out.persistence.customerorder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoupangOrderDetailJpaRepository extends JpaRepository<CoupangOrderDetailJpaEntity, Long> {

    boolean existsByCoupangOrderIdAndCoupangShipmentBoxId(long coupangOrderId, long coupangShipmentBoxId);
}
