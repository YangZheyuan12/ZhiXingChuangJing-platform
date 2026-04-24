package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.response.AssetResponses;
import com.zhixingchuangjing.platform.service.AssetService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/assets")
public class AssetController extends BaseController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AssetResponses.AssetUploadDataResponse> uploadAsset(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String folder,
            @RequestParam(required = false) String bizType) {
        return success(assetService.uploadAsset(currentUser.getId(), file, folder, bizType));
    }

    @GetMapping
    public ApiResponse<PageResponse<AssetResponses.AssetResponse>> getAssets(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return success(assetService.getAssets(currentUser.getId(), assetType, sourceType, page, pageSize));
    }
}
