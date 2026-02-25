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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
        String key = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(1280, 1280)
                    .keepAspectRatio(true)
                    .outputQuality(0.8)
                    .outputFormat("jpg")
                    .toOutputStream(out);
            byte[] compressed = out.toByteArray();

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket).key(key)
                            .contentType("image/jpeg")
                            .contentLength((long) compressed.length)
                            .build(),
                    RequestBody.fromInputStream(new ByteArrayInputStream(compressed), compressed.length));
            return String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucket, regionName, key);
        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
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
