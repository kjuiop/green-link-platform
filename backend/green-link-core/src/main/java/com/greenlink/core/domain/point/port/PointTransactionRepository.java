package com.greenlink.core.domain.point.port;

import com.greenlink.core.domain.point.model.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PointTransactionRepository {
    PointTransaction save(PointTransaction transaction);
    Optional<PointTransaction> findById(Long id);
    Page<PointTransaction> findByCompanyId(Long companyId, Pageable pageable);
    boolean existsOrderTransaction(Long orderId);
    boolean existsSettle(Long orderId);
    long sumNetHeldByOrderId(Long orderId);
    boolean existsRestore(Long orderId);
    long sumSettleByOrderId(Long orderId);
    long sumNetAdjustByOrderId(Long orderId);
}
