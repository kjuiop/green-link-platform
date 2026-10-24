package com.greenlink.core.application.referencephoto.service;

import com.greenlink.core.application.referencephoto.dto.ReferencePhotoResult;
import com.greenlink.core.application.referencephoto.dto.UploadReferencePhotoCommand;
import com.greenlink.core.application.referencephoto.usecase.GetReferencePhotosUseCase;
import com.greenlink.core.application.referencephoto.usecase.UploadReferencePhotoUseCase;
import com.greenlink.core.domain.photo.port.PhotoStorage;
import com.greenlink.core.domain.referencephoto.model.ReferencePhoto;
import com.greenlink.core.domain.referencephoto.port.ReferencePhotoRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.photo.PhotoErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReferencePhotoService implements UploadReferencePhotoUseCase, GetReferencePhotosUseCase {

    private final ReferencePhotoRepository referencePhotoRepository;
    private final PhotoStorage photoStorage;

    public ReferencePhotoService(ReferencePhotoRepository referencePhotoRepository, PhotoStorage photoStorage) {
        this.referencePhotoRepository = referencePhotoRepository;
        this.photoStorage = photoStorage;
    }

    @Override
    @Transactional
    public ReferencePhotoResult upload(UploadReferencePhotoCommand command) {
        PhotoStorage.UploadResult uploaded = photoStorage.store(
                command.orderId(), command.originalFileName(), command.content(), command.contentType());
        try {
            ReferencePhoto photo = ReferencePhoto.create(command.orderId(), uploaded.s3Key(), uploaded.url());
            return toResult(referencePhotoRepository.save(photo));
        } catch (Exception e) {
            photoStorage.delete(uploaded.s3Key());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferencePhotoResult> getByOrderId(Long orderId) {
        return referencePhotoRepository.findAllByOrderId(orderId).stream()
                .map(this::toResult)
                .toList();
    }

    private ReferencePhotoResult toResult(ReferencePhoto photo) {
        return new ReferencePhotoResult(photo.getId(), photo.getOrderId(), photo.getPhotoUrl(), photo.getUploadedAt());
    }
}
