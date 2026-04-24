package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;
import java.util.List;

public final class CommunityResponses {

    private CommunityResponses() {
    }

    public record CommentResponse(
            Long id,
            Long exhibitionId,
            Long userId,
            Long parentCommentId,
            Long rootCommentId,
            String content,
            String status,
            CommonResponses.SimpleUser user,
            List<CommonResponses.SimpleUser> mentionUsers,
            LocalDateTime createdAt
    ) {
    }

    public record CommunityCommentsResponse(
            List<ExhibitionResponses.SubmissionReviewResponse> teacherReviews,
            List<CommentResponse> studentComments
    ) {
    }
}
