package com.greenlink.admin.adapter.out.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemJpaEntity, Long> {

    List<OrderItemJpaEntity> findAllByOrderIdInOrderByOrderIdAscSortOrderAscIdAsc(List<Long> orderIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM OrderItemJpaEntity i WHERE i.orderId = :orderId")
    void deleteAllByOrderId(@Param("orderId") Long orderId);
}
