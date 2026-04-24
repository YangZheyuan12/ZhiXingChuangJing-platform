package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;

public final class AssetResponses {

    private AssetResponses() {
    }

    public record AssetUploadDataResponse(
            Long assetId,
            String fileName,
            String originalFileName,
            String fileUrl,
            String mimeType,
            Long fileSize
    ) {
    }

    public record AssetResponse(
            Long assetId,
            String fileName,
            String originalFileName,
            String fileUrl,
            String mimeType,
            Long fileSize,
            String assetType,
            String sourceType,
            LocalDateTime createdAt
    ) {
    }
}
