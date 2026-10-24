package com.greenlink.core.application.referencephoto.usecase;

import com.greenlink.core.application.referencephoto.dto.ReferencePhotoResult;
import com.greenlink.core.application.referencephoto.dto.UploadReferencePhotoCommand;

public interface UploadReferencePhotoUseCase {
    ReferencePhotoResult upload(UploadReferencePhotoCommand command);
}
