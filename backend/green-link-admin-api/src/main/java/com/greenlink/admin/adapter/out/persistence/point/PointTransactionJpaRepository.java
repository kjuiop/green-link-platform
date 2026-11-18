package com.greenlink.admin.adapter.out.persistence.point;

import com.greenlink.core.domain.point.model.PointTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransactionJpaEntity, Long> {
    Page<PointTransactionJpaEntity> findByCompanyId(Long companyId, Pageable pageable);

    boolean existsByOrderIdAndTypeIn(Long orderId, Collection<PointTransactionType> types);

    @Query("""
            select coalesce(sum(case when t.type = com.greenlink.core.domain.point.model.PointTransactionType.USE then t.amount
                                     when t.type = com.greenlink.core.domain.point.model.PointTransactionType.REFUND then -t.amount
                                     else 0 end), 0)
            from PointTransactionJpaEntity t
            where t.orderId = :orderId
            """)
    long sumNetHeldByOrderId(@Param("orderId") Long orderId);
}
