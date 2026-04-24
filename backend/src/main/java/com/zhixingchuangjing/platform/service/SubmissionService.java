package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.SubmissionRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.model.response.SubmissionResponses;

public interface SubmissionService {

    SubmissionResponses.SubmissionDetailResponse getSubmissionDetail(Long submissionId, Long userId, String role);

    ExhibitionResponses.SubmissionReviewResponse createSubmissionReview(Long submissionId,
                                                                       Long userId,
                                                                       String role,
                                                                       String nickname,
                                                                       SubmissionRequests.CreateSubmissionReviewRequest request);
}
