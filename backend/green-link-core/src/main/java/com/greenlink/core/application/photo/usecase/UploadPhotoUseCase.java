package com.greenlink.core.application.photo.usecase;

import com.greenlink.core.application.photo.dto.PhotoResult;
import com.greenlink.core.application.photo.dto.UploadPhotoCommand;

public interface UploadPhotoUseCase {

    PhotoResult uploadPhoto(UploadPhotoCommand command);
}
