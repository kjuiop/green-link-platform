package com.greenlink.core.application.vendor.usecase;

public interface UploadVendorDocumentUseCase {
    record Result(String businessRegUrl, String bankBookUrl) {}
    Result uploadDocument(Long vendorId, String docType, String fileName, byte[] content, String contentType);
}
