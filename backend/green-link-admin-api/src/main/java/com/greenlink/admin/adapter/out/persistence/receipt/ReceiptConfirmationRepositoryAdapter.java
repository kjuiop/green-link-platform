package com.greenlink.admin.adapter.out.persistence.receipt;

import com.greenlink.core.domain.receipt.model.ReceiptConfirmation;
import com.greenlink.core.domain.receipt.port.ReceiptConfirmationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ReceiptConfirmationRepositoryAdapter implements ReceiptConfirmationRepository {

    private final ReceiptConfirmationJpaRepository jpaRepository;

    public ReceiptConfirmationRepositoryAdapter(ReceiptConfirmationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ReceiptConfirmation save(ReceiptConfirmation confirmation) {
        ReceiptConfirmationJpaEntity entity = jpaRepository.findByOrderId(confirmation.getOrderId())
                .orElseGet(() -> ReceiptConfirmationJpaEntity.from(confirmation));

        entity.update(confirmation.getReceiverName(), confirmation.getReceiverRelation(),
                confirmation.isDelivered(), confirmation.getMemo(), confirmation.getConfirmedAt());
        entity.clearPhotos();
        for (String url : confirmation.getPhotoUrls()) {
            entity.addPhoto(url, "");
        }
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<ReceiptConfirmation> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId).map(ReceiptConfirmationJpaEntity::toDomain);
    }

    @Override
    public boolean existsWithPhotosByOrderId(Long orderId) {
        return jpaRepository.existsPhotosByOrderId(orderId);
    }

    @Override
    public ReceiptConfirmation appendPhotos(Long orderId, List<String> photoUrls) {
        ReceiptConfirmationJpaEntity entity = jpaRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    ReceiptConfirmationJpaEntity newEntity = new ReceiptConfirmationJpaEntity();
                    newEntity.initForOrder(orderId);
                    return newEntity;
                });
        for (String url : photoUrls) {
            entity.addPhoto(url, "");
        }
        return jpaRepository.save(entity).toDomain();
    }
}
