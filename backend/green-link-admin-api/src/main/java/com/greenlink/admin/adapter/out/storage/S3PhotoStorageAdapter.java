package com.greenlink.admin.adapter.out.storage;

import com.greenlink.core.domain.photo.port.PhotoStorage;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.photo.PhotoErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.UUID;

@Component
public class S3PhotoStorageAdapter implements PhotoStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String region;

    public S3PhotoStorageAdapter(S3Client s3Client,
                                  @Value("${aws.s3.bucket}") String bucket,
                                  @Value("${aws.s3.region}") String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.region = region;
    }

    @Override
    public UploadResult store(Long orderId, String originalFileName, byte[] content, String contentType) {
        String ext = extractExtension(originalFileName);
        String key = "orders/" + orderId + "/" + UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content)
            );
        } catch (S3Exception e) {
            throw new BusinessException(PhotoErrorCode.PHOTO_UPLOAD_FAILED);
        }

        String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
        return new UploadResult(key, url);
    }

    @Override
    public void delete(String s3Key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(s3Key).build());
    }

    private String extractExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }
}
