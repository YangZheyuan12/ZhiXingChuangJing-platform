package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.SubmissionRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.model.response.SubmissionResponses;
import com.zhixingchuangjing.platform.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/submissions")
public class SubmissionController extends BaseController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping("/{submissionId}")
    public ApiResponse<SubmissionResponses.SubmissionDetailResponse> getSubmissionDetail(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(submissionService.getSubmissionDetail(submissionId, currentUser.getId(), currentUser.getRole()));
    }

    @PostMapping("/{submissionId}/reviews")
    public ApiResponse<ExhibitionResponses.SubmissionReviewResponse> createSubmissionReview(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody SubmissionRequests.CreateSubmissionReviewRequest request) {
        return success(submissionService.createSubmissionReview(
                submissionId,
                currentUser.getId(),
                currentUser.getRole(),
                currentUser.getNickname(),
                request
        ));
    }
}
