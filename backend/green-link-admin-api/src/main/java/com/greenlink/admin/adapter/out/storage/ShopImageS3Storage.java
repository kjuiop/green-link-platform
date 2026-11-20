package com.greenlink.admin.adapter.out.storage;

import com.greenlink.core.domain.shop.port.ShopImageStorage;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.photo.PhotoErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Map;
import java.util.UUID;

@Component
public class ShopImageS3Storage implements ShopImageStorage {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");

    private final S3Client s3Client;
    private final String bucket;
    private final String region;

    public ShopImageS3Storage(S3Client s3Client,
                              @Value("${aws.s3.bucket}") String bucket,
                              @Value("${aws.s3.region}") String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.region = region;
    }

    @Override
    public String store(Long companyId, byte[] content, String contentType) {
        String key = "shop/" + companyId + "/" + UUID.randomUUID() + EXTENSIONS.getOrDefault(contentType, "");
        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build(),
                    RequestBody.fromBytes(content));
        } catch (S3Exception e) {
            throw new BusinessException(PhotoErrorCode.PHOTO_UPLOAD_FAILED);
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}
