package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.ZoneRequests;
import com.zhixingchuangjing.platform.model.response.ZoneResponses;
import com.zhixingchuangjing.platform.service.ZoneService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/exhibitions/{exhibitionId}/zones")
public class ZoneController extends BaseController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @GetMapping
    public ApiResponse<List<ZoneResponses.ZoneSummaryResponse>> listZones(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails user) {
        return success(zoneService.listZones(exhibitionId, user.getId(), user.getRole()));
    }

    @GetMapping("/{zoneId}")
    public ApiResponse<ZoneResponses.ZoneResponse> getZone(
            @PathVariable Long exhibitionId,
            @PathVariable Long zoneId,
            @AuthenticationPrincipal SecurityUserDetails user) {
        return success(zoneService.getZoneDetail(zoneId, user.getId(), user.getRole()));
    }

    @PostMapping
    public ApiResponse<Map<String, Long>> createZone(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails user,
            @Valid @RequestBody ZoneRequests.CreateZoneRequest request) {
        Long zoneId = zoneService.createZone(exhibitionId, user.getId(), user.getRole(), request);
        return success(Map.of("id", zoneId));
    }

    @PutMapping("/{zoneId}")
    public ApiResponse<Void> updateZone(
            @PathVariable Long exhibitionId,
            @PathVariable Long zoneId,
            @AuthenticationPrincipal SecurityUserDetails user,
            @Valid @RequestBody ZoneRequests.UpdateZoneRequest request) {
        zoneService.updateZone(zoneId, user.getId(), user.getRole(), request);
        return successMessage("展区更新成功");
    }

    @PutMapping("/{zoneId}/canvas-data")
    public ApiResponse<Void> updateCanvasData(
            @PathVariable Long exhibitionId,
            @PathVariable Long zoneId,
            @AuthenticationPrincipal SecurityUserDetails user,
            @RequestBody ZoneRequests.UpdateCanvasDataRequest request) {
        zoneService.updateCanvasData(zoneId, user.getId(), user.getRole(), request);
        return successMessage("画布数据已保存");
    }

    @PutMapping("/{zoneId}/assign")
    public ApiResponse<Void> assignZone(
            @PathVariable Long exhibitionId,
            @PathVariable Long zoneId,
            @AuthenticationPrincipal SecurityUserDetails user,
            @Valid @RequestBody ZoneRequests.AssignZoneRequest request) {
        zoneService.assignZone(zoneId, user.getId(), user.getRole(), request);
        return successMessage("展区已分配");
    }

    @PutMapping("/{zoneId}/lock")
    public ApiResponse<Void> lockZone(
            @PathVariable Long exhibitionId,
            @PathVariable Long zoneId,
            @AuthenticationPrincipal SecurityUserDetails user,
            @RequestBody ZoneRequests.LockZoneRequest request) {
        zoneService.lockZone(zoneId, user.getId(), user.getRole(), request);
        return successMessage(request.lock() ? "展区已锁定" : "展区已解锁");
    }

    @DeleteMapping("/{zoneId}")
    public ApiResponse<Void> deleteZone(
            @PathVariable Long exhibitionId,
            @PathVariable Long zoneId,
            @AuthenticationPrincipal SecurityUserDetails user) {
        zoneService.deleteZone(zoneId, user.getId(), user.getRole());
        return successMessage("展区已删除");
    }
}
