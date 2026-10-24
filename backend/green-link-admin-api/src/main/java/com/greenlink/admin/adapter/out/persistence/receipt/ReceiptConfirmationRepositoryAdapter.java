package com.greenlink.admin.adapter.out.persistence.receipt;

import com.greenlink.core.domain.receipt.model.ReceiptConfirmation;
import com.greenlink.core.domain.receipt.port.ReceiptConfirmationRepository;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityNotFoundException;
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
        for (String url : confirmation.getSitePhotoUrls()) {
            entity.addPhoto(url, "", "SITE");
        }
        for (String url : confirmation.getStorePhotoUrls()) {
            entity.addPhoto(url, "", "STORE");
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
    public ReceiptConfirmation appendPhotos(Long orderId, List<String> photoUrls, String photoType) {
        ReceiptConfirmationJpaEntity entity = jpaRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    ReceiptConfirmationJpaEntity newEntity = new ReceiptConfirmationJpaEntity();
                    newEntity.initForOrder(orderId);
                    return newEntity;
                });
        for (String url : photoUrls) {
            entity.addPhoto(url, "", photoType);
        }
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public ReceiptConfirmation updateReceiverInfo(Long orderId, String receiverName, String receiverRelation,
                                                   boolean delivered, String memo, LocalDateTime confirmedAt) {
        ReceiptConfirmationJpaEntity entity = jpaRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("인수확인 정보를 찾을 수 없습니다. orderId=" + orderId));
        LocalDateTime effectiveConfirmedAt = confirmedAt != null ? confirmedAt : entity.getConfirmedAt();
        entity.update(receiverName, receiverRelation, delivered, memo, effectiveConfirmedAt);
        return jpaRepository.save(entity).toDomain();
    }
}
