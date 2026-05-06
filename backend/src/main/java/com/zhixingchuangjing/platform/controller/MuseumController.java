package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.MuseumRequests;
import com.zhixingchuangjing.platform.model.response.MuseumResponses;
import com.zhixingchuangjing.platform.service.MuseumService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/museum/resources")
public class MuseumController extends BaseController {

    private final MuseumService museumService;

    public MuseumController(MuseumService museumService) {
        this.museumService = museumService;
    }

    @GetMapping
    public ApiResponse<PageResponse<MuseumResponses.MuseumResourceResponse>> getMuseumResources(
            @RequestParam(required = false) String providerCode,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return success(museumService.getMuseumResources(providerCode, category, keyword, page, pageSize));
    }

    @GetMapping("/filter")
    public ApiResponse<PageResponse<MuseumResponses.MuseumResourceResponse>> getMuseumResourcesWithFilters(
            @RequestParam(required = false) String dynasty,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return success(museumService.getMuseumResourcesWithFilters(dynasty, material, region, category, keyword, page, pageSize));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MuseumResponses.ImportResultResponse> importCsvResources(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam("file") MultipartFile file) {
        return success(museumService.importCsvResources(file, currentUser.getId()));
    }

    @PostMapping
    public ApiResponse<Void> syncMuseumResources(@Valid @RequestBody MuseumRequests.SyncMuseumResourcesRequest request) {
        museumService.syncMuseumResources(request);
        return successMessage("文博资源同步成功");
    }

    @GetMapping("/{resourceId}")
    public ApiResponse<MuseumResponses.MuseumResourceResponse> getMuseumResourceDetail(@PathVariable Long resourceId) {
        return success(museumService.getMuseumResourceDetail(resourceId));
    }

    @PostMapping("/{resourceId}/favorite")
    public ApiResponse<Void> favoriteMuseumResource(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestBody(required = false) MuseumRequests.FavoriteRequest request) {
        museumService.favoriteMuseumResource(resourceId, currentUser.getId(), request != null ? request.folder() : null);
        return successMessage("收藏成功");
    }

    @GetMapping("/assets/{assetId}/versions")
    public ApiResponse<List<MuseumResponses.AssetVersionResponse>> getAssetVersions(@PathVariable Long assetId) {
        return success(museumService.getAssetVersions(assetId));
    }

    @PostMapping(value = "/assets/{assetId}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MuseumResponses.AssetVersionResponse> uploadAssetVersion(
            @PathVariable Long assetId,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String note) {
        return success(museumService.uploadAssetVersion(assetId, file, note, currentUser.getId()));
    }

    @GetMapping("/folders")
    public ApiResponse<List<MuseumResponses.AssetFolderResponse>> getAssetFolders(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam(required = false) Long parentId) {
        return success(museumService.getAssetFolders(currentUser.getId(), parentId));
    }

    @PostMapping("/folders")
    public ApiResponse<MuseumResponses.AssetFolderResponse> createAssetFolder(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam(required = false) Long parentId,
            @RequestParam String folderName) {
        return success(museumService.createAssetFolder(currentUser.getId(), parentId, folderName));
    }

    @DeleteMapping("/folders/{folderId}")
    public ApiResponse<Void> deleteAssetFolder(@PathVariable Long folderId) {
        museumService.deleteAssetFolder(folderId);
        return successMessage("文件夹删除成功");
    }

    @GetMapping("/assets/{assetId}/tags")
    public ApiResponse<List<MuseumResponses.AssetTagResponse>> getAssetTags(@PathVariable Long assetId) {
        return success(museumService.getAssetTags(assetId));
    }

    @PostMapping("/assets/{assetId}/tags")
    public ApiResponse<Void> addAssetTags(
            @PathVariable Long assetId,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestBody List<String> tagNames) {
        museumService.addAssetTags(assetId, currentUser.getId(), tagNames);
        return successMessage("标签添加成功");
    }

    @DeleteMapping("/assets/{assetId}/tags/{tagName}")
    public ApiResponse<Void> removeAssetTag(@PathVariable Long assetId, @PathVariable String tagName) {
        museumService.removeAssetTag(assetId, tagName);
        return successMessage("标签删除成功");
    }

    @GetMapping("/packs")
    public ApiResponse<List<MuseumResponses.MaterialPackResponse>> getMaterialPacks(
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(museumService.getMaterialPacks(currentUser.getId()));
    }

    @GetMapping("/packs/{packId}")
    public ApiResponse<MuseumResponses.MaterialPackDetailResponse> getMaterialPackDetail(@PathVariable Long packId) {
        return success(museumService.getMaterialPackDetail(packId));
    }

    @PostMapping("/packs")
    public ApiResponse<MuseumResponses.MaterialPackResponse> createMaterialPack(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam String packName,
            @RequestParam(required = false) String description) {
        return success(museumService.createMaterialPack(currentUser.getId(), packName, description));
    }

    @PostMapping("/packs/{packId}/assets/{assetId}")
    public ApiResponse<Void> addAssetToMaterialPack(@PathVariable Long packId, @PathVariable Long assetId) {
        museumService.addAssetToMaterialPack(packId, assetId);
        return successMessage("素材添加成功");
    }

    @DeleteMapping("/packs/{packId}/assets/{assetId}")
    public ApiResponse<Void> removeAssetFromMaterialPack(@PathVariable Long packId, @PathVariable Long assetId) {
        museumService.removeAssetFromMaterialPack(packId, assetId);
        return successMessage("素材移除成功");
    }

    @DeleteMapping("/packs/{packId}")
    public ApiResponse<Void> deleteMaterialPack(@PathVariable Long packId) {
        museumService.deleteMaterialPack(packId);
        return successMessage("素材包删除成功");
    }
}
