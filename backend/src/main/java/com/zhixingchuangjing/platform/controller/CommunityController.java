package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.CommunityRequests;
import com.zhixingchuangjing.platform.model.response.CommunityResponses;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.service.CommunityService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/community/exhibitions")
public class CommunityController extends BaseController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ExhibitionResponses.ExhibitionSummaryResponse>> getCommunityExhibitions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return success(communityService.getCommunityExhibitions(keyword, grade, theme, tag, sortBy, page, pageSize));
    }

    @GetMapping("/{exhibitionId}")
    public ApiResponse<ExhibitionResponses.ExhibitionViewerDataResponse> getCommunityExhibitionDetail(@PathVariable Long exhibitionId) {
        return success(communityService.getCommunityExhibitionDetail(exhibitionId));
    }

    @GetMapping("/{exhibitionId}/comments")
    public ApiResponse<CommunityResponses.CommunityCommentsResponse> getCommunityComments(@PathVariable Long exhibitionId) {
        return success(communityService.getCommunityComments(exhibitionId));
    }

    @PostMapping("/{exhibitionId}/comments")
    public ApiResponse<CommunityResponses.CommentResponse> createComment(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody CommunityRequests.CreateCommentRequest request) {
        return success(communityService.createComment(
                exhibitionId,
                currentUser.getId(),
                currentUser.getRole(),
                currentUser.getNickname(),
                request
        ));
    }

    @PostMapping("/{exhibitionId}/like")
    public ApiResponse<Void> likeExhibition(@PathVariable Long exhibitionId,
                                            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        communityService.likeExhibition(exhibitionId, currentUser.getId());
        return successMessage("点赞成功");
    }

    @DeleteMapping("/{exhibitionId}/like")
    public ApiResponse<Void> unlikeExhibition(@PathVariable Long exhibitionId,
                                              @AuthenticationPrincipal SecurityUserDetails currentUser) {
        communityService.unlikeExhibition(exhibitionId, currentUser.getId());
        return successMessage("已取消点赞");
    }

    @PostMapping("/{exhibitionId}/favorite")
    public ApiResponse<Void> favoriteExhibition(@PathVariable Long exhibitionId,
                                                @AuthenticationPrincipal SecurityUserDetails currentUser) {
        communityService.favoriteExhibition(exhibitionId, currentUser.getId());
        return successMessage("收藏成功");
    }

    @DeleteMapping("/{exhibitionId}/favorite")
    public ApiResponse<Void> unfavoriteExhibition(@PathVariable Long exhibitionId,
                                                  @AuthenticationPrincipal SecurityUserDetails currentUser) {
        communityService.unfavoriteExhibition(exhibitionId, currentUser.getId());
        return successMessage("已取消收藏");
    }

    @PostMapping("/{exhibitionId}/share")
    public ApiResponse<Void> shareExhibition(@PathVariable Long exhibitionId,
                                             @AuthenticationPrincipal SecurityUserDetails currentUser,
                                             @Valid @RequestBody CommunityRequests.ShareExhibitionRequest request) {
        communityService.shareExhibition(exhibitionId, currentUser.getId(), request);
        return successMessage("分享记录成功");
    }

    @PostMapping("/{exhibitionId}/feature")
    public ApiResponse<Void> featureExhibition(@PathVariable Long exhibitionId,
                                               @AuthenticationPrincipal SecurityUserDetails currentUser,
                                               @Valid @RequestBody CommunityRequests.FeatureExhibitionRequest request) {
        communityService.featureExhibition(exhibitionId, currentUser.getId(), currentUser.getRole(), request);
        return successMessage("推荐状态已更新");
    }
}
