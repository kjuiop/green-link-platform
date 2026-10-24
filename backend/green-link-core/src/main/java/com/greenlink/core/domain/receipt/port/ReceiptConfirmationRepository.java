package com.greenlink.core.domain.receipt.port;

import com.greenlink.core.domain.receipt.model.ReceiptConfirmation;

import java.util.List;
import java.util.Optional;

public interface ReceiptConfirmationRepository {
    ReceiptConfirmation save(ReceiptConfirmation confirmation);
    Optional<ReceiptConfirmation> findByOrderId(Long orderId);
    boolean existsWithPhotosByOrderId(Long orderId);
    ReceiptConfirmation appendPhotos(Long orderId, List<String> photoUrls, String photoType);
}
