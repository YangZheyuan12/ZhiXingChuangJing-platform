package com.zhixingchuangjing.platform.common.storage;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioStorageProperties properties;
    private volatile boolean bucketInitialized;

    public MinioStorageService(MinioClient minioClient, MinioStorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

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
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50010, "素材文件存储失败");
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
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50011, "MinIO 初始化失败");
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
