package com.zhixingchuangjing.platform.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.common.util.PageUtils;
import com.zhixingchuangjing.platform.entity.*;
import com.zhixingchuangjing.platform.model.request.MuseumRequests;
import com.zhixingchuangjing.platform.model.response.MuseumResponses;
import com.zhixingchuangjing.platform.repository.*;
import com.zhixingchuangjing.platform.service.MuseumService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MuseumServiceImpl implements MuseumService {

    private final MuseumRepository museumRepository;
    private final MuseumResourceRepository museumResourceRepository;
    private final AssetRepository assetRepository;
    private final AssetVersionRepository assetVersionRepository;
    private final AssetFolderRepository assetFolderRepository;
    private final AssetTagRepository assetTagRepository;
    private final MaterialPackRepository materialPackRepository;
    private final MaterialPackAssetRepository materialPackAssetRepository;
    private final ObjectMapper objectMapper;

    public MuseumServiceImpl(MuseumRepository museumRepository,
                           MuseumResourceRepository museumResourceRepository,
                           AssetRepository assetRepository,
                           AssetVersionRepository assetVersionRepository,
                           AssetFolderRepository assetFolderRepository,
                           AssetTagRepository assetTagRepository,
                           MaterialPackRepository materialPackRepository,
                           MaterialPackAssetRepository materialPackAssetRepository,
                           ObjectMapper objectMapper) {
        this.museumRepository = museumRepository;
        this.museumResourceRepository = museumResourceRepository;
        this.assetRepository = assetRepository;
        this.assetVersionRepository = assetVersionRepository;
        this.assetFolderRepository = assetFolderRepository;
        this.assetTagRepository = assetTagRepository;
        this.materialPackRepository = materialPackRepository;
        this.materialPackAssetRepository = materialPackAssetRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResponse<MuseumResponses.MuseumResourceResponse> getMuseumResources(String providerCode,
                                                                                   String category,
                                                                                   String keyword,
                                                                                   Integer page,
                                                                                   Integer pageSize) {
        int normalizedPage = PageUtils.normalizePage(page);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return new PageResponse<>(
                museumRepository.findResources(providerCode, category, keyword,
                        PageUtils.offset(normalizedPage, normalizedPageSize), normalizedPageSize),
                normalizedPage,
                normalizedPageSize,
                museumRepository.countResources(providerCode, category, keyword)
        );
    }

    @Override
    @Transactional
    public void syncMuseumResources(MuseumRequests.SyncMuseumResourcesRequest request) {
        Long providerId = museumRepository.findProviderId(request.providerCode());
        if (providerId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40061, "资源提供方不存在");
        }

        String category = normalizeText(request.category(), "红色文化");
        String keyword = normalizeText(request.keyword(), "数字策展");
        for (int index = 1; index <= 3; index++) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("era", "现代");
            metadata.put("difficulty", index);
            metadata.put("keyword", keyword);
            museumRepository.upsertResource(
                    providerId,
                    "%s-%s-%d".formatted(request.providerCode(), category, index),
                    "image",
                    category,
                    "%s资源样本 %d".formatted(keyword, index),
                    "知行合作馆藏",
                    "https://example.com/museum/%s/%d.jpg".formatted(request.providerCode(), index),
                    "https://example.com/museum/%s/%d".formatted(request.providerCode(), index),
                    "用于联调的文博资源样本数据，便于前端检索与数字人装备绑定。",
                    toJson(metadata)
            );
        }
    }

    @Override
    public MuseumResponses.MuseumResourceResponse getMuseumResourceDetail(Long resourceId) {
        MuseumResponses.MuseumResourceResponse detail = museumRepository.findResourceDetail(resourceId);
        if (detail == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40461, "文博资源不存在");
        }
        return detail;
    }

    @Override
    @Transactional
    public MuseumResponses.ImportResultResponse importCsvResources(MultipartFile file, Long userId) {
        List<String> errors = new ArrayList<>();
        int totalRows = 0;
        int successCount = 0;
        int failCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                totalRows++;
                try {
                    String[] parts = line.split(",");
                    if (parts.length < 5) {
                        errors.add("第" + totalRows + "行：列数不足");
                        failCount++;
                        continue;
                    }

                    MuseumResourceEntity resource = new MuseumResourceEntity();
                    resource.setProviderId(1L);
                    resource.setExternalId("CSV_" + System.currentTimeMillis() + "_" + totalRows);
                    resource.setResourceType("image");
                    resource.setCategory(parts.length > 3 ? parts[3].trim() : "其他");
                    resource.setDynasty(parts.length > 4 ? parts[4].trim() : null);
                    resource.setMaterial(parts.length > 5 ? parts[5].trim() : null);
                    resource.setRegion(parts.length > 6 ? parts[6].trim() : null);
                    resource.setTitle(parts[0].trim());
                    resource.setSubtitle(parts.length > 1 ? parts[1].trim() : null);
                    resource.setMuseumName(parts.length > 2 ? parts[2].trim() : null);
                    resource.setDescription(parts.length > 7 ? parts[7].trim() : null);
                    resource.setCoverUrl(parts.length > 8 ? parts[8].trim() : null);
                    resource.setDetailUrl(parts.length > 9 ? parts[9].trim() : null);
                    resource.setCacheStatus("fresh");
                    resource.setSyncedAt(LocalDateTime.now());
                    resource.setCreatedAt(LocalDateTime.now());
                    resource.setUpdatedAt(LocalDateTime.now());

                    museumResourceRepository.save(resource);
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + totalRows + "行：" + e.getMessage());
                    failCount++;
                }
            }
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40062, "CSV 导入失败：" + e.getMessage());
        }

        return new MuseumResponses.ImportResultResponse(totalRows, successCount, failCount, errors);
    }

    @Override
    public PageResponse<MuseumResponses.MuseumResourceResponse> getMuseumResourcesWithFilters(String dynasty,
                                                                                              String material,
                                                                                              String region,
                                                                                              String category,
                                                                                              String keyword,
                                                                                              Integer page,
                                                                                              Integer pageSize) {
        // 使用 JdbcTemplate 查询，直接调用原有 MuseumRepository 的逻辑
        // 这里简化处理，先查询所有，再在内存中过滤（生产环境建议优化 SQL）
        List<MuseumResourceEntity> resources = museumResourceRepository.findAll();
        
        // 内存过滤
        List<MuseumResourceEntity> filtered = resources.stream()
                .filter(r -> dynasty == null || dynasty.isEmpty() || dynasty.equals(r.getDynasty()))
                .filter(r -> material == null || material.isEmpty() || material.equals(r.getMaterial()))
                .filter(r -> region == null || region.isEmpty() || region.equals(r.getRegion()))
                .filter(r -> category == null || category.isEmpty() || category.equals(r.getCategory()))
                .filter(r -> keyword == null || keyword.isEmpty() || r.getTitle().contains(keyword))
                .collect(Collectors.toList());
        
        int normalizedPage = PageUtils.normalizePage(page);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        
        // 分页
        int fromIndex = (normalizedPage - 1) * normalizedPageSize;
        int toIndex = Math.min(fromIndex + normalizedPageSize, filtered.size());
        List<MuseumResponses.MuseumResourceResponse> responseList = filtered.stream()
                .skip(fromIndex)
                .limit(normalizedPageSize)
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(responseList, normalizedPage, normalizedPageSize, filtered.size());
    }

    @Override
    @Transactional
    public void favoriteMuseumResource(Long resourceId, Long userId, String folder) {
        MuseumResourceEntity resource = museumResourceRepository.findById(resourceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40461, "文博资源不存在"));

        String assetType = switch (resource.getResourceType()) {
            case "video" -> "video";
            case "audio" -> "audio";
            case "model" -> "model";
            case "document" -> "document";
            default -> "image";
        };

        Long assetId = assetRepository.createAsset(
                userId,
                assetType,
                "museum",
                resource.getTitle() + "." + getFileExt(resource.getCoverUrl()),
                resource.getTitle(),
                resource.getCoverUrl(),
                getFileExt(resource.getCoverUrl()),
                "image/jpeg",
                0L
        );

        if (folder != null && !folder.isEmpty()) {
            AssetFolderEntity folderEntity = new AssetFolderEntity();
            folderEntity.setOwnerId(userId);
            folderEntity.setFolderName(folder);
            folderEntity.setFolderPath("/" + folder);
            folderEntity.setSortNo(0);
            folderEntity.setCreatedAt(LocalDateTime.now());
            folderEntity.setUpdatedAt(LocalDateTime.now());
            assetFolderRepository.save(folderEntity);
        }
    }

    @Override
    public List<MuseumResponses.AssetVersionResponse> getAssetVersions(Long assetId) {
        return assetVersionRepository.findByAssetIdOrderByVersionNoDesc(assetId).stream()
                .map(v -> new MuseumResponses.AssetVersionResponse(
                        v.getId(),
                        v.getVersionNo(),
                        v.getFileName(),
                        v.getFileUrl(),
                        v.getFileSize(),
                        v.getChecksumMd5(),
                        v.getVersionNote(),
                        v.getCreatedBy(),
                        v.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MuseumResponses.AssetVersionResponse uploadAssetVersion(Long assetId, MultipartFile file, String note, Long userId) {
        AssetVersionEntity lastVersion = assetVersionRepository.findTopByAssetIdOrderByVersionNoDesc(assetId);
        int newVersionNo = (lastVersion != null ? lastVersion.getVersionNo() : 0) + 1;

        String checksumMd5 = calculateMd5(file);
        String fileName = file.getOriginalFilename();
        String fileUrl = "/assets/versions/" + assetId + "/v" + newVersionNo + "/" + fileName;

        AssetVersionEntity version = new AssetVersionEntity();
        version.setAssetId(assetId);
        version.setVersionNo(newVersionNo);
        version.setFileName(fileName);
        version.setFileUrl(fileUrl);
        version.setFileSize(file.getSize());
        version.setChecksumMd5(checksumMd5);
        version.setVersionNote(note);
        version.setCreatedBy(userId);
        version.setCreatedAt(LocalDateTime.now());

        assetVersionRepository.save(version);

        return new MuseumResponses.AssetVersionResponse(
                version.getId(),
                version.getVersionNo(),
                version.getFileName(),
                version.getFileUrl(),
                version.getFileSize(),
                version.getChecksumMd5(),
                version.getVersionNote(),
                version.getCreatedBy(),
                version.getCreatedAt()
        );
    }

    @Override
    public List<MuseumResponses.AssetFolderResponse> getAssetFolders(Long userId, Long parentId) {
        List<AssetFolderEntity> folders = (parentId == null) ?
                assetFolderRepository.findByOwnerId(userId) :
                assetFolderRepository.findByOwnerIdAndParentId(userId, parentId);
        
        return folders.stream()
                .map(f -> new MuseumResponses.AssetFolderResponse(
                        f.getId(),
                        f.getParentId(),
                        f.getFolderName(),
                        f.getFolderPath(),
                        f.getSortNo(),
                        f.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MuseumResponses.AssetFolderResponse createAssetFolder(Long userId, Long parentId, String folderName) {
        AssetFolderEntity parent = parentId != null ? assetFolderRepository.findById(parentId).orElse(null) : null;
        
        AssetFolderEntity folder = new AssetFolderEntity();
        folder.setOwnerId(userId);
        folder.setParentId(parentId);
        folder.setFolderName(folderName);
        folder.setFolderPath(parent != null ? parent.getFolderPath() + "/" + folderName : "/" + folderName);
        folder.setSortNo(0);
        folder.setCreatedAt(LocalDateTime.now());
        folder.setUpdatedAt(LocalDateTime.now());
        
        assetFolderRepository.save(folder);
        
        return new MuseumResponses.AssetFolderResponse(
                folder.getId(),
                folder.getParentId(),
                folder.getFolderName(),
                folder.getFolderPath(),
                folder.getSortNo(),
                folder.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public void deleteAssetFolder(Long folderId) {
        assetFolderRepository.deleteById(folderId);
    }

    @Override
    public List<MuseumResponses.AssetTagResponse> getAssetTags(Long assetId) {
        return assetTagRepository.findByAssetId(assetId).stream()
                .map(t -> new MuseumResponses.AssetTagResponse(
                        t.getId(),
                        t.getTagName(),
                        t.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addAssetTags(Long assetId, Long userId, List<String> tagNames) {
        for (String tagName : tagNames) {
            AssetTagEntity tag = new AssetTagEntity();
            tag.setAssetId(assetId);
            tag.setOwnerId(userId);
            tag.setTagName(tagName);
            tag.setCreatedAt(LocalDateTime.now());
            assetTagRepository.save(tag);
        }
    }

    @Override
    @Transactional
    public void removeAssetTag(Long assetId, String tagName) {
        List<AssetTagEntity> tags = assetTagRepository.findByAssetId(assetId);
        for (AssetTagEntity tag : tags) {
            if (tag.getTagName().equals(tagName)) {
                assetTagRepository.delete(tag);
            }
        }
    }

    @Override
    public List<MuseumResponses.MaterialPackResponse> getMaterialPacks(Long userId) {
        return materialPackRepository.findByOwnerId(userId).stream()
                .map(p -> new MuseumResponses.MaterialPackResponse(
                        p.getId(),
                        p.getPackName(),
                        p.getDescription(),
                        p.getAssetCount(),
                        p.getIsPublic(),
                        p.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public MuseumResponses.MaterialPackDetailResponse getMaterialPackDetail(Long packId) {
        MaterialPackEntity pack = materialPackRepository.findById(packId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40462, "素材包不存在"));
        
        List<MaterialPackAssetEntity> packAssets = materialPackAssetRepository.findByPackId(packId);
        List<MuseumResponses.AssetResponse> assets = packAssets.stream()
                .map(pa -> {
                    com.zhixingchuangjing.platform.model.response.AssetResponses.AssetResponse asset = 
                            assetRepository.findAssetById(pa.getAssetId());
                    return new MuseumResponses.AssetResponse(
                            asset.id(),
                            asset.assetType(),
                            asset.fileName(),
                            asset.fileUrl(),
                            asset.fileSize(),
                            asset.createdAt()
                    );
                })
                .collect(Collectors.toList());

        return new MuseumResponses.MaterialPackDetailResponse(
                pack.getId(),
                pack.getPackName(),
                pack.getDescription(),
                pack.getAssetCount(),
                pack.getIsPublic(),
                pack.getCreatedAt(),
                assets
        );
    }

    @Override
    @Transactional
    public MuseumResponses.MaterialPackResponse createMaterialPack(Long userId, String packName, String description) {
        MaterialPackEntity pack = new MaterialPackEntity();
        pack.setOwnerId(userId);
        pack.setPackName(packName);
        pack.setDescription(description);
        pack.setAssetCount(0);
        pack.setIsPublic(false);
        pack.setCreatedAt(LocalDateTime.now());
        pack.setUpdatedAt(LocalDateTime.now());
        
        materialPackRepository.save(pack);
        
        return new MuseumResponses.MaterialPackResponse(
                pack.getId(),
                pack.getPackName(),
                pack.getDescription(),
                pack.getAssetCount(),
                pack.getIsPublic(),
                pack.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public void addAssetToMaterialPack(Long packId, Long assetId) {
        MaterialPackEntity pack = materialPackRepository.findById(packId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40462, "素材包不存在"));
        
        MaterialPackAssetEntity packAsset = new MaterialPackAssetEntity();
        packAsset.setPackId(packId);
        packAsset.setAssetId(assetId);
        packAsset.setSortNo(0);
        packAsset.setCreatedAt(LocalDateTime.now());
        
        materialPackAssetRepository.save(packAsset);
        
        pack.setAssetCount(pack.getAssetCount() + 1);
        pack.setUpdatedAt(LocalDateTime.now());
        materialPackRepository.save(pack);
    }

    @Override
    @Transactional
    public void removeAssetFromMaterialPack(Long packId, Long assetId) {
        materialPackAssetRepository.deleteByPackId(packId);
        
        MaterialPackEntity pack = materialPackRepository.findById(packId).orElse(null);
        if (pack != null) {
            pack.setAssetCount(Math.max(0, pack.getAssetCount() - 1));
            pack.setUpdatedAt(LocalDateTime.now());
            materialPackRepository.save(pack);
        }
    }

    @Override
    @Transactional
    public void deleteMaterialPack(Long packId) {
        materialPackAssetRepository.deleteByPackId(packId);
        materialPackRepository.deleteById(packId);
    }

    private MuseumResponses.MuseumResourceResponse convertToResponse(MuseumResourceEntity resource) {
        List<String> tags = new ArrayList<>();
        if (resource.getTagsJson() != null) {
            try {
                tags = objectMapper.readValue(resource.getTagsJson(), List.class);
            } catch (JsonProcessingException e) {
            }
        }
        
        Map<String, Object> metadata = new HashMap<>();
        if (resource.getMetadataJson() != null) {
            try {
                metadata = objectMapper.readValue(resource.getMetadataJson(), Map.class);
            } catch (JsonProcessingException e) {
            }
        }

        return new MuseumResponses.MuseumResourceResponse(
                resource.getId(),
                resource.getProviderId(),
                null,
                resource.getTitle(),
                resource.getCategory(),
                resource.getDynasty(),
                resource.getMaterial(),
                resource.getRegion(),
                resource.getMuseumName(),
                resource.getCoverUrl(),
                resource.getDetailUrl(),
                resource.getDescription(),
                tags,
                metadata,
                resource.getSyncedAt()
        );
    }

    private String getFileExt(String url) {
        if (url == null || url.isEmpty()) return "jpg";
        int lastDot = url.lastIndexOf(".");
        return lastDot > 0 ? url.substring(lastDot + 1) : "jpg";
    }

    private String calculateMd5(MultipartFile file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = file.getBytes();
            byte[] digest = md.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50031, "文博资源同步失败");
        }
    }

    private String normalizeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
