package com.greenlink.core.application.receipt.service;

import com.greenlink.core.application.receipt.dto.ReceiptConfirmationResult;
import com.greenlink.core.application.receipt.dto.SaveReceiptConfirmationCommand;
import com.greenlink.core.application.receipt.usecase.GetReceiptConfirmationUseCase;
import com.greenlink.core.application.receipt.usecase.SaveReceiptConfirmationUseCase;
import com.greenlink.core.domain.photo.port.PhotoStorage;
import com.greenlink.core.domain.receipt.model.ReceiptConfirmation;
import com.greenlink.core.domain.receipt.port.ReceiptConfirmationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReceiptConfirmationService implements SaveReceiptConfirmationUseCase, GetReceiptConfirmationUseCase {

    private final ReceiptConfirmationRepository repository;
    private final PhotoStorage photoStorage;

    public ReceiptConfirmationService(ReceiptConfirmationRepository repository, PhotoStorage photoStorage) {
        this.repository = repository;
        this.photoStorage = photoStorage;
    }

    @Override
    @Transactional
    public ReceiptConfirmationResult save(SaveReceiptConfirmationCommand command) {
        List<String> photoUrls = new ArrayList<>();
        List<String> uploadedKeys = new ArrayList<>();

        for (SaveReceiptConfirmationCommand.PhotoFile pf : command.photoFiles()) {
            PhotoStorage.UploadResult uploaded = photoStorage.store(
                    command.orderId(), pf.originalFileName(), pf.content(), pf.contentType());
            photoUrls.add(uploaded.url());
            uploadedKeys.add(uploaded.s3Key());
        }

        try {
            ReceiptConfirmation confirmation = ReceiptConfirmation.create(
                    command.orderId(), command.receiverName(), command.receiverRelation(),
                    command.delivered(), command.memo(), command.confirmedAt(), photoUrls);
            return toResult(repository.save(confirmation));
        } catch (Exception e) {
            uploadedKeys.forEach(photoStorage::delete);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReceiptConfirmationResult> getByOrderId(Long orderId) {
        return repository.findByOrderId(orderId).map(this::toResult);
    }

    private ReceiptConfirmationResult toResult(ReceiptConfirmation rc) {
        return new ReceiptConfirmationResult(
                rc.getId(),
                rc.getOrderId(),
                rc.getReceiverName(),
                rc.getReceiverRelation(),
                rc.isDelivered(),
                rc.getMemo(),
                rc.getConfirmedAt() != null ? rc.getConfirmedAt().toString() : null,
                rc.getPhotoUrls()
        );
    }
}
