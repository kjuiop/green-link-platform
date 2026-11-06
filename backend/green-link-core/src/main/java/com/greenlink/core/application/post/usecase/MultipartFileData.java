package com.greenlink.core.application.post.usecase;

public record MultipartFileData(String originalFileName, byte[] content, String contentType) {}
