package com.greenlink.core.application.receipt.service;

import com.greenlink.core.application.orderhistory.usecase.RecordOrderHistoryUseCase;
import com.greenlink.core.domain.orderhistory.model.OrderHistoryAction;
import com.greenlink.core.application.receipt.dto.ReceiptConfirmationResult;
import com.greenlink.core.application.receipt.dto.SaveReceiptConfirmationCommand;
import com.greenlink.core.application.receipt.usecase.AddReceiptPhotosUseCase;
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
public class ReceiptConfirmationService implements SaveReceiptConfirmationUseCase, GetReceiptConfirmationUseCase, AddReceiptPhotosUseCase {

    private final ReceiptConfirmationRepository repository;
    private final PhotoStorage photoStorage;
    private final RecordOrderHistoryUseCase recordOrderHistoryUseCase;

    public ReceiptConfirmationService(ReceiptConfirmationRepository repository,
                                      PhotoStorage photoStorage,
                                      RecordOrderHistoryUseCase recordOrderHistoryUseCase) {
        this.repository = repository;
        this.photoStorage = photoStorage;
        this.recordOrderHistoryUseCase = recordOrderHistoryUseCase;
    }

    @Override
    @Transactional
    public ReceiptConfirmationResult save(SaveReceiptConfirmationCommand command) {
        List<String> sitePhotoUrls = new ArrayList<>();
        List<String> storePhotoUrls = new ArrayList<>();
        List<String> uploadedKeys = new ArrayList<>();

        for (SaveReceiptConfirmationCommand.PhotoFile pf : command.photoFiles()) {
            PhotoStorage.UploadResult uploaded = photoStorage.store(
                    command.orderId(), pf.originalFileName(), pf.content(), pf.contentType());
            uploadedKeys.add(uploaded.s3Key());
            if ("STORE".equals(pf.photoType())) {
                storePhotoUrls.add(uploaded.url());
            } else {
                sitePhotoUrls.add(uploaded.url());
            }
        }

        ReceiptConfirmationResult result;
        try {
            ReceiptConfirmation confirmation = ReceiptConfirmation.create(
                    command.orderId(), command.receiverName(), command.receiverRelation(),
                    command.delivered(), command.memo(), command.confirmedAt(), sitePhotoUrls, storePhotoUrls);
            result = toResult(repository.save(confirmation));
        } catch (Exception e) {
            uploadedKeys.forEach(photoStorage::delete);
            throw e;
        }
        recordOrderHistoryUseCase.record(command.orderId(), OrderHistoryAction.UPDATED, "인수확인 저장");
        return result;

    }

    @Override
    @Transactional
    public ReceiptConfirmationResult addPhotos(Long orderId, List<SaveReceiptConfirmationCommand.PhotoFile> photoFiles) {
        List<String> sitePhotoUrls = new ArrayList<>();
        List<String> storePhotoUrls = new ArrayList<>();
        List<String> uploadedKeys = new ArrayList<>();

        for (SaveReceiptConfirmationCommand.PhotoFile pf : photoFiles) {
            PhotoStorage.UploadResult uploaded = photoStorage.store(
                    orderId, pf.originalFileName(), pf.content(), pf.contentType());
            uploadedKeys.add(uploaded.s3Key());
            if ("STORE".equals(pf.photoType())) {
                storePhotoUrls.add(uploaded.url());
            } else {
                sitePhotoUrls.add(uploaded.url());
            }
        }

        try {
            ReceiptConfirmation result = null;
            if (!sitePhotoUrls.isEmpty()) {
                result = repository.appendPhotos(orderId, sitePhotoUrls, "SITE");
            }
            if (!storePhotoUrls.isEmpty()) {
                result = repository.appendPhotos(orderId, storePhotoUrls, "STORE");
            }
            if (result == null) {
                result = repository.findByOrderId(orderId).orElseThrow();
            }
            return toResult(result);
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
                rc.getSitePhotoUrls(),
                rc.getStorePhotoUrls()
        );
    }
}
