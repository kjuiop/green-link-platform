package com.greenlink.admin.adapter.out.persistence.taxinvoice;

import com.greenlink.core.domain.taxinvoice.model.TaxInvoice;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class TaxInvoiceRepositoryAdapter implements TaxInvoiceRepository {

    private final TaxInvoiceJpaRepository jpaRepository;

    public TaxInvoiceRepositoryAdapter(TaxInvoiceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<TaxInvoice> findById(Long id) {
        return jpaRepository.findById(id).map(TaxInvoiceJpaEntity::toDomain);
    }

    @Override
    public Optional<TaxInvoice> findByPointTransactionId(Long pointTransactionId) {
        return jpaRepository.findByPointTransactionId(pointTransactionId).map(TaxInvoiceJpaEntity::toDomain);
    }

    @Override
    public Optional<TaxInvoice> findByPointTransactionIdForUpdate(Long pointTransactionId) {
        return jpaRepository.findByPointTransactionIdForUpdate(pointTransactionId).map(TaxInvoiceJpaEntity::toDomain);
    }

    @Override
    public List<TaxInvoice> findByPointTransactionIds(Collection<Long> pointTransactionIds) {
        return jpaRepository.findByPointTransactionIdIn(pointTransactionIds).stream()
                .map(TaxInvoiceJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<TaxInvoice> saveIfAbsent(TaxInvoice invoice) {
        if (jpaRepository.existsByPointTransactionId(invoice.getPointTransactionId())) {
            return Optional.empty();
        }
        return Optional.of(jpaRepository.saveAndFlush(TaxInvoiceJpaEntity.from(invoice)).toDomain());
    }

    @Override
    public TaxInvoice save(TaxInvoice invoice) {
        return jpaRepository.save(TaxInvoiceJpaEntity.from(invoice)).toDomain();
    }
}
