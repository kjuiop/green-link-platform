package com.greenlink.admin.adapter.out.persistence.point;

import com.greenlink.core.domain.point.model.PointTransaction;
import com.greenlink.core.domain.point.model.PointTransactionType;
import com.greenlink.core.domain.point.port.PointTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PointTransactionRepositoryAdapter implements PointTransactionRepository {

    private final PointTransactionJpaRepository jpaRepository;

    public PointTransactionRepositoryAdapter(PointTransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PointTransaction save(PointTransaction transaction) {
        return jpaRepository.save(PointTransactionJpaEntity.from(transaction)).toDomain();
    }

    @Override
    public Optional<PointTransaction> findById(Long id) {
        return jpaRepository.findById(id).map(PointTransactionJpaEntity::toDomain);
    }

    @Override
    public Page<PointTransaction> findByCompanyId(Long companyId, Pageable pageable) {
        return jpaRepository.findByCompanyId(companyId, pageable).map(PointTransactionJpaEntity::toDomain);
    }

    @Override
    public boolean existsOrderTransaction(Long orderId) {
        return jpaRepository.existsByOrderIdAndTypeIn(orderId, List.of(
                PointTransactionType.USE, PointTransactionType.REFUND, PointTransactionType.SETTLE));
    }

    @Override
    public boolean existsSettle(Long orderId) {
        return jpaRepository.existsByOrderIdAndTypeIn(orderId, List.of(PointTransactionType.SETTLE));
    }

    @Override
    public boolean existsRestore(Long orderId) {
        return jpaRepository.existsByOrderIdAndTypeIn(orderId, List.of(PointTransactionType.RESTORE));
    }

    @Override
    public long sumSettleByOrderId(Long orderId) {
        return jpaRepository.sumAmountByOrderIdAndType(orderId, PointTransactionType.SETTLE);
    }

    @Override
    public long sumNetAdjustByOrderId(Long orderId) {
        return jpaRepository.sumAmountByOrderIdAndType(orderId, PointTransactionType.ADJUST_USE)
                - jpaRepository.sumAmountByOrderIdAndType(orderId, PointTransactionType.ADJUST_RESTORE);
    }

    @Override
    public long sumNetHeldByOrderId(Long orderId) {
        return jpaRepository.sumAmountByOrderIdAndType(orderId, PointTransactionType.USE)
                - jpaRepository.sumAmountByOrderIdAndType(orderId, PointTransactionType.REFUND);
    }
}
