package com.greenlink.core.domain.taxinvoice.port;

import com.greenlink.core.domain.taxinvoice.model.TaxInvoice;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaxInvoiceRepository {

    Optional<TaxInvoice> findById(Long id);

    Optional<TaxInvoice> findByPointTransactionId(Long pointTransactionId);

    /** 같은 거래에 대한 동시 재시도를 직렬화하기 위해 행 잠금을 건다. */
    Optional<TaxInvoice> findByPointTransactionIdForUpdate(Long pointTransactionId);

    List<TaxInvoice> findByPointTransactionIds(Collection<Long> pointTransactionIds);

    /** point_transaction_id 유니크 제약 충돌 시 empty를 반환한다. */
    Optional<TaxInvoice> saveIfAbsent(TaxInvoice invoice);

    TaxInvoice save(TaxInvoice invoice);
}
