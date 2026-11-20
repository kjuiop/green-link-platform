package com.greenlink.admin.adapter.out.persistence.taxinvoice;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaxInvoiceJpaRepository extends JpaRepository<TaxInvoiceJpaEntity, Long> {

    boolean existsByPointTransactionId(Long pointTransactionId);

    Optional<TaxInvoiceJpaEntity> findByPointTransactionId(Long pointTransactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TaxInvoiceJpaEntity t WHERE t.pointTransactionId = :pointTransactionId")
    Optional<TaxInvoiceJpaEntity> findByPointTransactionIdForUpdate(@Param("pointTransactionId") Long pointTransactionId);

    List<TaxInvoiceJpaEntity> findByPointTransactionIdIn(Collection<Long> pointTransactionIds);
}
