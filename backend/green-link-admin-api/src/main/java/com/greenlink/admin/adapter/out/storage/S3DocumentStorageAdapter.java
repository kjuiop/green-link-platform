package com.greenlink.admin.adapter.out.storage;

import com.greenlink.core.domain.company.port.DocumentStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Component
public class S3DocumentStorageAdapter implements DocumentStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String region;

    public S3DocumentStorageAdapter(S3Client s3Client,
                                     @Value("${aws.s3.bucket}") String bucket,
                                     @Value("${aws.s3.region}") String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.region = region;
    }

    @Override
    public UploadResult store(Long companyId, String docType, String originalFileName,
                               byte[] content, String contentType) {
        String ext = (originalFileName != null && originalFileName.contains("."))
                ? originalFileName.substring(originalFileName.lastIndexOf('.'))
                : "";
        String key = "vendors/" + companyId + "/docs/" + docType + "/" + UUID.randomUUID() + ext;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType != null ? contentType : "application/octet-stream")
                        .build(),
                RequestBody.fromBytes(content)
        );

        String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
        return new UploadResult(key, url);
    }
}
