package com.qttracker.util;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Component
public class S3Uploader {

    private final S3Client s3Client;
    private final String   bucket;
    private final String   regionName;

    public S3Uploader(
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey,
            @Value("${aws.s3.region}")  String region,
            @Value("${aws.s3.bucket}")  String bucket) {
        this.bucket     = bucket;
        this.regionName = region;
        this.s3Client   = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    // ── 업로드
    public String upload(MultipartFile file, String folder) {
        // 파일 타입 검증 (Thumbnailator가 지원하는 포맷만 허용)
        String contentType = file.getContentType();
        if (contentType == null || !java.util.List.of(
                "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp").contains(contentType)) {
            throw new IllegalArgumentException("JPEG, PNG, GIF, WebP 형식만 업로드 가능합니다. (HEIC는 지원하지 않습니다)");
        }
        // 파일 크기 검증 (30MB 초과 거부)
        if (file.getSize() > 30 * 1024 * 1024L) {
            throw new IllegalArgumentException("이미지 크기는 30MB 이하여야 합니다.");
        }

        // 원본 파일명 제거 — UUID만 사용해 특수문자/한글 문제 방지
        String key = folder + "/" + UUID.randomUUID() + ".jpg";
        Path tempFile = null;
        try {
            // ByteArrayOutputStream 대신 임시 파일로 처리 → 메모리 절약
            tempFile = Files.createTempFile("upload-", ".jpg");
            Thumbnails.of(file.getInputStream())
                    .size(1280, 1280)
                    .keepAspectRatio(true)
                    .outputQuality(0.8)
                    .outputFormat("jpg")
                    .toFile(tempFile.toFile());

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket).key(key)
                            .contentType("image/jpeg")
                            .build(),
                    RequestBody.fromFile(tempFile));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, regionName, key);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            }
        }
    }

    // ── 삭제: URL에서 key를 파싱하여 S3 객체 삭제
    public void deleteFile(String imageUrl) {
        try {
            // upload()에서 생성한 URL 형식: https://{bucket}.s3.{region}.amazonaws.com/{key}
            String urlPrefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, regionName);
            if (!imageUrl.startsWith(urlPrefix)) {
                log.warn("S3 삭제 실패: 예상치 못한 URL 형식: {}", imageUrl);
                return;
            }
            String key = imageUrl.substring(urlPrefix.length());
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            log.warn("S3 삭제 실패 (무시): {} / {}", imageUrl, e.getMessage());
        }
    }
}
