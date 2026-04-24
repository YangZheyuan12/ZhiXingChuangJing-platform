package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.common.util.PageUtils;
import com.zhixingchuangjing.platform.model.response.AssetResponses;
import com.zhixingchuangjing.platform.common.storage.MinioStorageService;
import com.zhixingchuangjing.platform.repository.AssetRepository;
import com.zhixingchuangjing.platform.service.AssetService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

@Service
public class AssetServiceImpl implements AssetService {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    private final AssetRepository assetRepository;
    private final MinioStorageService minioStorageService;

    public AssetServiceImpl(AssetRepository assetRepository,
                            MinioStorageService minioStorageService) {
        this.assetRepository = assetRepository;
        this.minioStorageService = minioStorageService;
    }

    @Override
    public AssetResponses.AssetUploadDataResponse uploadAsset(Long userId,
                                                              MultipartFile file,
                                                              String folder,
                                                              String bizType) {
        validateUpload(file, folder, bizType);
        String originalFileName = file.getOriginalFilename() == null ? "asset.bin" : file.getOriginalFilename();
        String fileExt = extractExtension(originalFileName);
        String mimeType = file.getContentType();
        String assetType = inferAssetType(mimeType, fileExt, folder, bizType);
        String storedFileName = buildStoredFileName(fileExt);
        String objectName = buildObjectName(userId, folder, storedFileName);
        String fileUrl;
        try {
            fileUrl = minioStorageService.upload(file.getInputStream(), file.getSize(), mimeType, objectName);
        } catch (java.io.IOException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50012, "素材文件读取失败");
        }
        Long assetId = assetRepository.createAsset(
                userId,
                assetType,
                resolveSourceType(folder, bizType),
                storedFileName,
                originalFileName,
                fileUrl,
                fileExt,
                mimeType,
                file.getSize()
        );
        return new AssetResponses.AssetUploadDataResponse(assetId, storedFileName, originalFileName, fileUrl, mimeType, file.getSize());
    }

    @Override
    public PageResponse<AssetResponses.AssetResponse> getAssets(Long userId,
                                                                String assetType,
                                                                String sourceType,
                                                                Integer page,
                                                                Integer pageSize) {
        int normalizedPage = PageUtils.normalizePage(page);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return new PageResponse<>(
                assetRepository.findAssets(userId, assetType, sourceType,
                        PageUtils.offset(normalizedPage, normalizedPageSize), normalizedPageSize),
                normalizedPage,
                normalizedPageSize,
                assetRepository.countAssets(userId, assetType, sourceType)
        );
    }

    private String inferAssetType(String mimeType, String fileExt, String folder, String bizType) {
        String normalizedMime = mimeType == null ? "" : mimeType.toLowerCase(Locale.ROOT);
        String normalizedExt = fileExt == null ? "" : fileExt.toLowerCase(Locale.ROOT);
        String context = ((folder == null ? "" : folder) + " " + (bizType == null ? "" : bizType)).toLowerCase(Locale.ROOT);
        if (normalizedMime.startsWith("image/") || matches(normalizedExt, "png", "jpg", "jpeg", "gif", "webp")) {
            return "image";
        }
        if (normalizedMime.startsWith("video/") || matches(normalizedExt, "mp4", "mov", "avi")) {
            return "video";
        }
        if (normalizedMime.startsWith("audio/") || matches(normalizedExt, "mp3", "wav", "ogg")) {
            return "audio";
        }
        if (matches(normalizedExt, "glb", "gltf", "obj", "fbx") || context.contains("model")) {
            return "model";
        }
        return "document";
    }

    private String resolveSourceType(String folder, String bizType) {
        if (isAvatarContext(folder, bizType)) {
            return "profile";
        }
        return "upload";
    }

    private void validateUpload(MultipartFile file, String folder, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40061, "上传文件不能为空");
        }

        String mimeType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String fileExt = extractExtension(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        boolean avatarContext = isAvatarContext(folder, bizType);

        if (avatarContext && !isImageFile(mimeType, fileExt)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40062, "头像仅支持图片格式");
        }

        if (avatarContext && file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40063, "头像大小不能超过5MB");
        }
    }

    private boolean isAvatarContext(String folder, String bizType) {
        String normalizedFolder = folder == null ? "" : folder.toLowerCase(Locale.ROOT);
        String normalizedBizType = bizType == null ? "" : bizType.toLowerCase(Locale.ROOT);
        return normalizedFolder.contains("avatar")
                || normalizedBizType.contains("avatar")
                || normalizedBizType.contains("profile");
    }

    private boolean isImageFile(String mimeType, String fileExt) {
        return mimeType.startsWith("image/")
                || matches(fileExt == null ? "" : fileExt.toLowerCase(Locale.ROOT), "png", "jpg", "jpeg", "gif", "webp", "bmp");
    }

    private boolean matches(String value, String... candidates) {
        for (String candidate : candidates) {
            if (candidate.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private String extractExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(index + 1);
    }

    private String buildStoredFileName(String fileExt) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (fileExt == null || fileExt.isBlank()) {
            return uuid;
        }
        return uuid + "." + fileExt.toLowerCase(Locale.ROOT);
    }

    private String buildObjectName(Long userId, String folder, String storedFileName) {
        String normalizedFolder = normalizeFolder(folder);
        return normalizedFolder + "/" + userId + "/" + storedFileName;
    }

    private String normalizeFolder(String folder) {
        String value = folder == null ? "" : folder.trim().toLowerCase(Locale.ROOT);
        value = value.replaceAll("[^a-z0-9/_-]", "-");
        value = value.replaceAll("/+", "/");
        value = value.replaceAll("^-+", "");
        value = value.replaceAll("-+$", "");
        if (value.isBlank() || "/".equals(value)) {
            return "assets";
        }
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value.isBlank() ? "assets" : value;
    }
}
