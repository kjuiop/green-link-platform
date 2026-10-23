package com.greenlink.admin.adapter.out.persistence.receipt;

import com.greenlink.core.domain.receipt.model.ReceiptConfirmation;
import com.greenlink.core.domain.receipt.port.ReceiptConfirmationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReceiptConfirmationRepositoryAdapter implements ReceiptConfirmationRepository {

    private final ReceiptConfirmationJpaRepository jpaRepository;

    public ReceiptConfirmationRepositoryAdapter(ReceiptConfirmationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ReceiptConfirmation save(ReceiptConfirmation confirmation) {
        ReceiptConfirmationJpaEntity entity = ReceiptConfirmationJpaEntity.from(confirmation);
        for (String url : confirmation.getPhotoUrls()) {
            entity.addPhoto(url, "");
        }
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<ReceiptConfirmation> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId).map(ReceiptConfirmationJpaEntity::toDomain);
    }
}
