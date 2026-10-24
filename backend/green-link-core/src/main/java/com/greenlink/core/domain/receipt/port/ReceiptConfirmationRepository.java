package com.greenlink.core.domain.receipt.port;

import com.greenlink.core.domain.receipt.model.ReceiptConfirmation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReceiptConfirmationRepository {
    ReceiptConfirmation save(ReceiptConfirmation confirmation);
    Optional<ReceiptConfirmation> findByOrderId(Long orderId);

    List<ReceiptConfirmation> findAllByOrderIds(List<Long> orderIds);
    boolean existsWithPhotosByOrderId(Long orderId);
    ReceiptConfirmation appendPhotos(Long orderId, List<String> photoUrls, String photoType);
    ReceiptConfirmation updateReceiverInfo(Long orderId, String receiverName, String receiverRelation,
                                           Boolean delivered, String memo, LocalDateTime confirmedAt);
}
