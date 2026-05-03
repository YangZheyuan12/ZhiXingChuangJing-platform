package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.HotspotRequests;
import com.zhixingchuangjing.platform.model.response.EditorBundleResponses;
import com.zhixingchuangjing.platform.service.HotspotService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/exhibitions")
public class HotspotController extends BaseController {

    private final HotspotService hotspotService;

    public HotspotController(HotspotService hotspotService) {
        this.hotspotService = hotspotService;
    }

    /**
     * 列出展区下所有热点。
     */
    @GetMapping("/{exhibitionId}/zones/{zoneId}/hotspots")
    public ApiResponse<List<EditorBundleResponses.HotspotResponse>> listHotspots(
            @PathVariable Long exhibitionId,
            @PathVariable Long zoneId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(hotspotService.listByZone(
                exhibitionId, zoneId, currentUser.getId(), currentUser.getRole()));
    }

    /**
     * 创建热点。
     */
    @PostMapping("/{exhibitionId}/zones/{zoneId}/hotspots")
    public ApiResponse<EditorBundleResponses.HotspotResponse> createHotspot(
            @PathVariable Long exhibitionId,
            @PathVariable Long zoneId,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody HotspotRequests.CreateHotspotRequest request) {
        return success(hotspotService.createHotspot(
                exhibitionId, zoneId,
                currentUser.getId(), currentUser.getRole(),
                request));
    }

    /**
     * 更新热点；字段为 null 表示保留原值。
     */
    @PutMapping("/{exhibitionId}/hotspots/{hotspotId}")
    public ApiResponse<EditorBundleResponses.HotspotResponse> updateHotspot(
            @PathVariable Long exhibitionId,
            @PathVariable Long hotspotId,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody HotspotRequests.UpdateHotspotRequest request) {
        return success(hotspotService.updateHotspot(
                exhibitionId, hotspotId,
                currentUser.getId(), currentUser.getRole(),
                request));
    }

    /**
     * 删除热点。
     */
    @DeleteMapping("/{exhibitionId}/hotspots/{hotspotId}")
    public ApiResponse<Void> deleteHotspot(
            @PathVariable Long exhibitionId,
            @PathVariable Long hotspotId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        hotspotService.deleteHotspot(
                exhibitionId, hotspotId,
                currentUser.getId(), currentUser.getRole());
        return successMessage("热点已删除");
    }
}
