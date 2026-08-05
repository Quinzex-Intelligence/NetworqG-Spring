package com.networq.service;

import com.networq.logging.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/jpg");

    public String uploadImage(MultipartFile file) throws IOException {
        validateImage(file);

        String extension = getExtension(file.getOriginalFilename());
        String key = "services/" + UUID.randomUUID().toString() + "." + extension;
        long fileSize = file.getSize();

        log.info("S3 upload started. objectKey={}, fileSize={} bytes", key, fileSize);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(key)
                    .contentType(file.getContentType()).build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("S3 upload completed. objectKey={}, fileSize={} bytes", key, fileSize);
            return key;
        } catch (IOException | RuntimeException ex) {
            log.error("""
                    S3 upload failed.
                    objectKey  : {}
                    fileSize   : {} bytes
                    Stacktrace :
                    {}
                    """,
                    key,
                    fileSize,
                    LoggingUtils.stackTrace(ex));
            throw ex;
        }

    }

    public void deleteImage(String imageKey) {
        log.info("Deleting image from S3. objectKey={}", imageKey);
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(imageKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Deleted image from S3. objectKey={}", imageKey);
        } catch (RuntimeException ex) {
            log.error("""
                    Failed to delete image from S3.
                    objectKey  : {}
                    Stacktrace :
                    {}
                    """,
                    imageKey,
                    LoggingUtils.stackTrace(ex));
            throw ex;
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException("Image is required.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image size must not exceed 5 MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG, JPEG, PNG and WEBP images are allowed.");
        }

    }

    public String generatePresignedUrl(String imageKey) {
        log.info("Generating pre-signed URL. objectKey={}", imageKey);
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(imageKey).build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(30)).getObjectRequest(getObjectRequest).build();
            String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();
            log.info("Generated pre-signed URL. objectKey={}", imageKey);
            return presignedUrl;
        } catch (RuntimeException ex) {
            log.error("""
                    Failed to generate pre-signed URL.
                    objectKey  : {}
                    Stacktrace :
                    {}
                    """,
                    imageKey,
                    LoggingUtils.stackTrace(ex));
            throw ex;
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
