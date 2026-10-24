package com.greenlink.core.application.referencephoto.usecase;

import com.greenlink.core.application.referencephoto.dto.ReferencePhotoResult;

import java.util.List;

public interface GetReferencePhotosUseCase {
    List<ReferencePhotoResult> getByOrderId(Long orderId);
}
