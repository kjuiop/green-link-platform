package com.greenlink.core.application.photo.service;

import com.greenlink.core.application.photo.dto.PhotoResult;
import com.greenlink.core.application.photo.dto.UploadPhotoCommand;
import com.greenlink.core.application.photo.usecase.GetPhotosUseCase;
import com.greenlink.core.application.photo.usecase.SelectPhotoUseCase;
import com.greenlink.core.application.photo.usecase.UploadPhotoUseCase;
import com.greenlink.core.domain.photo.model.Photo;
import com.greenlink.core.domain.photo.model.PhotoType;
import com.greenlink.core.domain.photo.port.PhotoRepository;
import com.greenlink.core.domain.photo.port.PhotoStorage;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.photo.PhotoErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PhotoService implements UploadPhotoUseCase, SelectPhotoUseCase, GetPhotosUseCase {

    private final PhotoRepository photoRepository;
    private final PhotoStorage photoStorage;

    public PhotoService(PhotoRepository photoRepository, PhotoStorage photoStorage) {
        this.photoRepository = photoRepository;
        this.photoStorage = photoStorage;
    }

    @Override
    public PhotoResult uploadPhoto(UploadPhotoCommand command) {
        PhotoType photoType;
        try {
            photoType = PhotoType.valueOf(command.photoType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(PhotoErrorCode.INVALID_PHOTO_TYPE);
        }
        PhotoStorage.UploadResult uploaded = photoStorage.store(
                command.orderId(), command.originalFileName(), command.content(), command.contentType());
        try {
            Photo photo = Photo.create(command.orderId(), uploaded.s3Key(), uploaded.url(), photoType);
            return toResult(photoRepository.save(photo));
        } catch (Exception e) {
            photoStorage.delete(uploaded.s3Key());
            throw e;
        }
    }

    @Override
    @Transactional
    public PhotoResult selectPhoto(Long photoId, boolean selected) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new BusinessException(PhotoErrorCode.PHOTO_NOT_FOUND));
        photo.toggleSendToCustomer(selected);
        return toResult(photoRepository.save(photo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoResult> getPhotosByOrder(Long orderId) {
        return photoRepository.findAllByOrderId(orderId).stream().map(this::toResult).toList();
    }

    private PhotoResult toResult(Photo photo) {
        return new PhotoResult(
                photo.getId(),
                photo.getOrderId(),
                photo.getPhotoUrl(),
                photo.getPhotoType().name(),
                photo.isSendToCustomer(),
                photo.getUploadedAt()
        );
    }
}
