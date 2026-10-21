package com.greenlink.core.application.photo.usecase;

import com.greenlink.core.application.photo.dto.PhotoResult;

public interface SelectPhotoUseCase {

    PhotoResult selectPhoto(Long photoId, boolean selected);
}
