package com.zhixingchuangjing.platform.common.storage;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@ConditionalOnProperty(name = "app.storage.mode", havingValue = "minio")
public class MinioStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    private final MinioClient minioClient;
    private final MinioStorageProperties properties;
    private volatile boolean bucketInitialized;

    public MinioStorageService(MinioClient minioClient, MinioStorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public String upload(InputStream inputStream,
                         long contentLength,
                         String contentType,
                         String objectName) {
        try {
            ensureBucketReady();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .stream(inputStream, contentLength, -1)
                            .contentType(resolveContentType(contentType))
                            .build()
            );
            return buildFileUrl(objectName);
        } catch (BusinessException ex) {
            // ensureBucketReady 已经抛了明确的异常，直接传递
            throw ex;
        } catch (Exception ex) {
            log.error("MinIO 上传失败 endpoint={} bucket={} object={} size={} contentType={}",
                    properties.getEndpoint(), properties.getBucket(), objectName, contentLength, contentType, ex);
            String reason = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50010,
                    "素材文件存储失败: " + reason);
        }
    }

    private String resolveContentType(String contentType) {
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }

    private String buildFileUrl(String objectName) {
        String baseUrl = trimTrailingSlash(
                properties.getPublicBaseUrl() == null || properties.getPublicBaseUrl().isBlank()
                        ? properties.getEndpoint()
                        : properties.getPublicBaseUrl()
        );
        return baseUrl + "/" + properties.getBucket() + "/" + objectName;
    }

    private String trimTrailingSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private synchronized void ensureBucketReady() {
        if (bucketInitialized) {
            return;
        }

        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(properties.getBucket())
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(properties.getBucket())
                                .build()
                );
            }

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(properties.getBucket())
                            .config(buildReadOnlyPolicy(properties.getBucket()))
                            .build()
            );

            bucketInitialized = true;
        } catch (Exception ex) {
            log.error("MinIO 初始化失败 endpoint={} bucket={}",
                    properties.getEndpoint(), properties.getBucket(), ex);
            String reason = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50011,
                    "MinIO 初始化失败: " + reason);
        }
    }

    private String buildReadOnlyPolicy(String bucket) {
        return """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {
                        "AWS": [
                          "*"
                        ]
                      },
                      "Action": [
                        "s3:GetObject"
                      ],
                      "Resource": [
                        "arn:aws:s3:::%s/*"
                      ]
                    }
                  ]
                }
                """.formatted(bucket);
    }
}
