package com.greenlink.core.application.photo.usecase;

import com.greenlink.core.application.photo.dto.PhotoResult;

import java.util.List;

public interface GetPhotosUseCase {

    List<PhotoResult> getAllPhotos();

    List<PhotoResult> getPhotosByOrder(Long orderId);
}
