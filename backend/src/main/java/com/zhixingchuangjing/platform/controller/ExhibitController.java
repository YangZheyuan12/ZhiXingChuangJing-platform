package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.ExhibitRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitResponses;
import com.zhixingchuangjing.platform.service.ExhibitService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/exhibitions/{exhibitionId}")
public class ExhibitController extends BaseController {

    private final ExhibitService exhibitService;

    public ExhibitController(ExhibitService exhibitService) {
        this.exhibitService = exhibitService;
    }

    @GetMapping("/zones/{zoneId}/exhibits")
    public ApiResponse<List<ExhibitResponses.ExhibitSummaryResponse>> listExhibits(
            @PathVariable Long exhibitionId,
            @PathVariable Long zoneId,
            @AuthenticationPrincipal SecurityUserDetails user) {
        return success(exhibitService.listExhibits(zoneId, user.getId(), user.getRole()));
    }

    @GetMapping("/exhibits/{exhibitId}")
    public ApiResponse<ExhibitResponses.ExhibitResponse> getExhibit(
            @PathVariable Long exhibitionId,
            @PathVariable Long exhibitId,
            @AuthenticationPrincipal SecurityUserDetails user) {
        return success(exhibitService.getExhibitDetail(exhibitId, user.getId(), user.getRole()));
    }

    @PostMapping("/exhibits")
    public ApiResponse<Map<String, Long>> createExhibit(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails user,
            @Valid @RequestBody ExhibitRequests.CreateExhibitRequest request) {
        Long id = exhibitService.createExhibit(exhibitionId, user.getId(), user.getRole(), request);
        return success(Map.of("id", id));
    }

    @PutMapping("/exhibits/{exhibitId}")
    public ApiResponse<Void> updateExhibit(
            @PathVariable Long exhibitionId,
            @PathVariable Long exhibitId,
            @AuthenticationPrincipal SecurityUserDetails user,
            @Valid @RequestBody ExhibitRequests.UpdateExhibitRequest request) {
        exhibitService.updateExhibit(exhibitId, user.getId(), user.getRole(), request);
        return successMessage("展品更新成功");
    }

    @DeleteMapping("/exhibits/{exhibitId}")
    public ApiResponse<Void> deleteExhibit(
            @PathVariable Long exhibitionId,
            @PathVariable Long exhibitId,
            @AuthenticationPrincipal SecurityUserDetails user) {
        exhibitService.deleteExhibit(exhibitId, user.getId(), user.getRole());
        return successMessage("展品已删除");
    }

    @PutMapping("/exhibits/{exhibitId}/narrations")
    public ApiResponse<Map<String, Long>> upsertNarration(
            @PathVariable Long exhibitionId,
            @PathVariable Long exhibitId,
            @AuthenticationPrincipal SecurityUserDetails user,
            @Valid @RequestBody ExhibitRequests.UpsertNarrationRequest request) {
        Long id = exhibitService.upsertNarration(exhibitId, user.getId(), user.getRole(), request);
        return success(Map.of("id", id));
    }

    @PutMapping("/exhibits/{exhibitId}/interactions")
    public ApiResponse<Map<String, Long>> upsertInteraction(
            @PathVariable Long exhibitionId,
            @PathVariable Long exhibitId,
            @AuthenticationPrincipal SecurityUserDetails user,
            @Valid @RequestBody ExhibitRequests.UpsertInteractionRequest request) {
        Long id = exhibitService.upsertInteraction(exhibitId, user.getId(), user.getRole(), request);
        return success(Map.of("id", id));
    }

    @DeleteMapping("/narrations/{narrationId}")
    public ApiResponse<Void> deleteNarration(
            @PathVariable Long exhibitionId,
            @PathVariable Long narrationId,
            @AuthenticationPrincipal SecurityUserDetails user) {
        exhibitService.deleteNarration(narrationId, user.getId(), user.getRole());
        return successMessage("讲解已删除");
    }

    @DeleteMapping("/interactions/{interactionId}")
    public ApiResponse<Void> deleteInteraction(
            @PathVariable Long exhibitionId,
            @PathVariable Long interactionId,
            @AuthenticationPrincipal SecurityUserDetails user) {
        exhibitService.deleteInteraction(interactionId, user.getId(), user.getRole());
        return successMessage("互动题已删除");
    }
}
