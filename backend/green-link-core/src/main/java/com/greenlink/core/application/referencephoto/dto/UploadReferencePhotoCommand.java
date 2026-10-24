package com.greenlink.core.application.referencephoto.dto;

public record UploadReferencePhotoCommand(
        Long orderId,
        String originalFileName,
        byte[] content,
        String contentType
) {}
