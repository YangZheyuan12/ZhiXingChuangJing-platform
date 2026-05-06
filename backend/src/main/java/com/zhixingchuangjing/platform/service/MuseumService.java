package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.MuseumRequests;
import com.zhixingchuangjing.platform.model.response.MuseumResponses;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MuseumService {

    PageResponse<MuseumResponses.MuseumResourceResponse> getMuseumResources(String providerCode,
                                                                           String category,
                                                                           String keyword,
                                                                           Integer page,
                                                                           Integer pageSize);

    void syncMuseumResources(MuseumRequests.SyncMuseumResourcesRequest request);

    MuseumResponses.MuseumResourceResponse getMuseumResourceDetail(Long resourceId);

    MuseumResponses.ImportResultResponse importCsvResources(MultipartFile file, Long userId);

    PageResponse<MuseumResponses.MuseumResourceResponse> getMuseumResourcesWithFilters(String dynasty,
                                                                                      String material,
                                                                                      String region,
                                                                                      String category,
                                                                                      String keyword,
                                                                                      Integer page,
                                                                                      Integer pageSize);

    void favoriteMuseumResource(Long resourceId, Long userId, String folder);

    List<MuseumResponses.AssetVersionResponse> getAssetVersions(Long assetId);

    MuseumResponses.AssetVersionResponse uploadAssetVersion(Long assetId, MultipartFile file, String note, Long userId);

    List<MuseumResponses.AssetFolderResponse> getAssetFolders(Long userId, Long parentId);

    MuseumResponses.AssetFolderResponse createAssetFolder(Long userId, Long parentId, String folderName);

    void deleteAssetFolder(Long folderId);

    List<MuseumResponses.AssetTagResponse> getAssetTags(Long assetId);

    void addAssetTags(Long assetId, Long userId, List<String> tagNames);

    void removeAssetTag(Long assetId, String tagName);

    List<MuseumResponses.MaterialPackResponse> getMaterialPacks(Long userId);

    MuseumResponses.MaterialPackDetailResponse getMaterialPackDetail(Long packId);

    MuseumResponses.MaterialPackResponse createMaterialPack(Long userId, String packName, String description);

    void addAssetToMaterialPack(Long packId, Long assetId);

    void removeAssetFromMaterialPack(Long packId, Long assetId);

    void deleteMaterialPack(Long packId);
}
