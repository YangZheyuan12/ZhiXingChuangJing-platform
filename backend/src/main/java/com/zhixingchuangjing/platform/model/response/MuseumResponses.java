package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class MuseumResponses {

    private MuseumResponses() {
    }

    public record MuseumResourceResponse(
            Long id,
            Long providerId,
            String providerCode,
            String title,
            String category,
            String dynasty,
            String material,
            String region,
            String museumName,
            String coverUrl,
            String detailUrl,
            String description,
            List<String> tags,
            Map<String, Object> metadata,
            LocalDateTime syncedAt
    ) {
    }

    public record ImportResultResponse(
            int totalRows,
            int successCount,
            int failCount,
            List<String> errors
    ) {
    }

    public record AssetVersionResponse(
            Long id,
            Integer versionNo,
            String fileName,
            String fileUrl,
            Long fileSize,
            String checksumMd5,
            String versionNote,
            Long createdBy,
            LocalDateTime createdAt
    ) {
    }

    public record AssetFolderResponse(
            Long id,
            Long parentId,
            String folderName,
            String folderPath,
            Integer sortNo,
            LocalDateTime createdAt
    ) {
    }

    public record AssetTagResponse(
            Long id,
            String tagName,
            LocalDateTime createdAt
    ) {
    }

    public record MaterialPackResponse(
            Long id,
            String packName,
            String description,
            Integer assetCount,
            Boolean isPublic,
            LocalDateTime createdAt
    ) {
    }

    public record MaterialPackDetailResponse(
            Long id,
            String packName,
            String description,
            Integer assetCount,
            Boolean isPublic,
            LocalDateTime createdAt,
            List<AssetResponse> assets
    ) {
    }

    public record AssetResponse(
            Long id,
            String assetType,
            String fileName,
            String fileUrl,
            Long fileSize,
            LocalDateTime createdAt
    ) {
    }
}
