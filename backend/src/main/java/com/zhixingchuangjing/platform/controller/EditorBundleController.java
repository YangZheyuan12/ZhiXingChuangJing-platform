package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.EditorBundleRequests;
import com.zhixingchuangjing.platform.model.response.EditorBundleResponses;
import com.zhixingchuangjing.platform.service.EditorBundleService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/exhibitions/{exhibitionId}")
public class EditorBundleController extends BaseController {

    private final EditorBundleService editorBundleService;

    public EditorBundleController(EditorBundleService editorBundleService) {
        this.editorBundleService = editorBundleService;
    }

    @GetMapping("/editor-bundle")
    public ApiResponse<EditorBundleResponses.EditorBundleResponse> getBundle(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails user) {
        return success(editorBundleService.getBundle(exhibitionId, user.getId(), user.getRole()));
    }

    @PutMapping("/editor-bundle")
    public ApiResponse<EditorBundleResponses.SaveBundleResultResponse> saveBundle(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails user,
            @Valid @RequestBody EditorBundleRequests.SaveBundleRequest request) {
        return success(editorBundleService.saveBundle(exhibitionId, user.getId(), user.getRole(), request));
    }

    @PutMapping("/draft-bundle")
    public ApiResponse<Void> saveDraftBundle(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails user,
            @Valid @RequestBody EditorBundleRequests.DraftBundleRequest request) {
        editorBundleService.saveDraftBundle(exhibitionId, user.getId(), user.getRole(), request);
        return successMessage("草稿已自动保存");
    }
}
