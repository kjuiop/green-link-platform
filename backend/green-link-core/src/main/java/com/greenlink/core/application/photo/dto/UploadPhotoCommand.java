package com.greenlink.core.application.photo.dto;

public record UploadPhotoCommand(
        Long orderId,
        String originalFileName,
        byte[] content,
        String contentType,
        String photoType
) {}
