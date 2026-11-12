package com.greenlink.core.domain.company.port;

public interface DocumentStorage {
    record UploadResult(String key, String url) {}
    UploadResult store(Long companyId, String docType, String originalFileName, byte[] content, String contentType);
}
