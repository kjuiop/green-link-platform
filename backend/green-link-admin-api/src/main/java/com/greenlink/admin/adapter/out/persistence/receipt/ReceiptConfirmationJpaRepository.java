package com.greenlink.admin.adapter.out.persistence.receipt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptConfirmationJpaRepository extends JpaRepository<ReceiptConfirmationJpaEntity, Long> {
    Optional<ReceiptConfirmationJpaEntity> findByOrderId(Long orderId);
}
