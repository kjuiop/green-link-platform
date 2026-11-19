package com.greenlink.core.domain.coupang.port;

import com.greenlink.core.domain.coupang.model.CoupangOrder;
import com.greenlink.core.domain.coupang.model.CoupangOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CoupangOrderRepository {

    Optional<CoupangOrder> findById(Long id);

    /** 행 잠금(SELECT ... FOR UPDATE)으로 조회한다. 동시 승인 방지용. */
    Optional<CoupangOrder> findByIdForUpdate(Long id);

    boolean existsByCompanyIdAndShipmentBoxId(Long companyId, long shipmentBoxId);

    /** 이미 같은 배송박스가 있으면 저장하지 않고 false를 반환한다. */
    boolean insertIfAbsent(CoupangOrder order);

    CoupangOrder save(CoupangOrder order);

    Page<CoupangOrder> search(Long companyId, CoupangOrderStatus status, Pageable pageable);

    long countByStatus(Long companyId, CoupangOrderStatus status);
}
