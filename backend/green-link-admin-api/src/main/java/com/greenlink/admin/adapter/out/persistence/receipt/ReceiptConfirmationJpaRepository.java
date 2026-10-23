package com.greenlink.admin.adapter.out.persistence.receipt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReceiptConfirmationJpaRepository extends JpaRepository<ReceiptConfirmationJpaEntity, Long> {
    Optional<ReceiptConfirmationJpaEntity> findByOrderId(Long orderId);

    @Query("SELECT COUNT(p) > 0 FROM ReceiptPhotoJpaEntity p WHERE p.confirmation.orderId = :orderId")
    boolean existsPhotosByOrderId(@Param("orderId") Long orderId);
}
