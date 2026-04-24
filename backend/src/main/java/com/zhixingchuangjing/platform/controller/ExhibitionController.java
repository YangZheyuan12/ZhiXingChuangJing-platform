package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.ExhibitionRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.service.ExhibitionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/exhibitions")
public class ExhibitionController extends BaseController {

    private final ExhibitionService exhibitionService;

    public ExhibitionController(ExhibitionService exhibitionService) {
        this.exhibitionService = exhibitionService;
    }

    @PostMapping
    public ApiResponse<ExhibitionResponses.ExhibitionDetailResponse> createExhibition(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody ExhibitionRequests.CreateExhibitionRequest request) {
        return success(exhibitionService.createExhibition(currentUser.getId(), currentUser.getRole(), request));
    }

    @GetMapping
    public ApiResponse<PageResponse<ExhibitionResponses.ExhibitionSummaryResponse>> getMyExhibitions(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return success(exhibitionService.getMyExhibitions(currentUser.getId(), taskId, status, keyword, page, pageSize));
    }

    @PutMapping("/{exhibitionId}")
    public ApiResponse<Void> updateExhibition(@PathVariable Long exhibitionId,
                                              @AuthenticationPrincipal SecurityUserDetails currentUser,
                                              @Valid @RequestBody ExhibitionRequests.UpdateExhibitionRequest request) {
        exhibitionService.updateExhibition(exhibitionId, currentUser.getId(), currentUser.getRole(), request);
        return successMessage("展厅更新成功");
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<ExhibitionResponses.ExhibitionSummaryResponse>> getMyExhibitionsAlias(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return success(exhibitionService.getMyExhibitions(currentUser.getId(), taskId, status, keyword, page, pageSize));
    }

    @GetMapping("/{exhibitionId}")
    public ApiResponse<ExhibitionResponses.ExhibitionDetailResponse> getExhibitionDetail(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(exhibitionService.getExhibitionDetail(exhibitionId, currentUser.getId(), currentUser.getRole()));
    }

    @GetMapping("/{exhibitionId}/members")
    public ApiResponse<List<ExhibitionResponses.ExhibitionMemberResponse>> getExhibitionMembers(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(exhibitionService.getExhibitionMembers(exhibitionId, currentUser.getId(), currentUser.getRole()));
    }

    @PostMapping("/{exhibitionId}/members")
    public ApiResponse<Void> addExhibitionMembers(@PathVariable Long exhibitionId,
                                                  @AuthenticationPrincipal SecurityUserDetails currentUser,
                                                  @Valid @RequestBody ExhibitionRequests.AddExhibitionMembersRequest request) {
        exhibitionService.addExhibitionMembers(exhibitionId, currentUser.getId(), currentUser.getRole(), request);
        return successMessage("协作成员添加成功");
    }

    @GetMapping("/{exhibitionId}/versions")
    public ApiResponse<List<ExhibitionResponses.ExhibitionVersionResponse>> getVersions(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(exhibitionService.getVersions(exhibitionId, currentUser.getId(), currentUser.getRole()));
    }

    @PostMapping("/{exhibitionId}/versions")
    public ApiResponse<ExhibitionResponses.ExhibitionVersionResponse> saveVersion(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody ExhibitionRequests.SaveExhibitionVersionRequest request) {
        return success(exhibitionService.saveVersion(
                exhibitionId,
                currentUser.getId(),
                currentUser.getRole(),
                currentUser.getNickname(),
                request
        ));
    }

    @PostMapping("/{exhibitionId}/publish")
    public ApiResponse<Void> publishExhibition(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody ExhibitionRequests.PublishExhibitionRequest request) {
        exhibitionService.publishExhibition(exhibitionId, currentUser.getId(), currentUser.getRole(), request);
        return successMessage("展厅发布成功");
    }

    @GetMapping("/{exhibitionId}/viewer")
    public ApiResponse<ExhibitionResponses.ExhibitionViewerDataResponse> getViewerData(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(exhibitionService.getViewerData(exhibitionId, currentUser.getId(), currentUser.getRole()));
    }

    @GetMapping("/{exhibitionId}/digital-human")
    public ApiResponse<ExhibitionResponses.DigitalHumanResponse> getDigitalHuman(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(exhibitionService.getDigitalHuman(exhibitionId, currentUser.getId(), currentUser.getRole()));
    }

    @PutMapping("/{exhibitionId}/digital-human")
    public ApiResponse<ExhibitionResponses.DigitalHumanResponse> upsertDigitalHuman(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody ExhibitionRequests.UpsertDigitalHumanRequest request) {
        return success(exhibitionService.upsertDigitalHuman(exhibitionId, currentUser.getId(), currentUser.getRole(), request));
    }
}
