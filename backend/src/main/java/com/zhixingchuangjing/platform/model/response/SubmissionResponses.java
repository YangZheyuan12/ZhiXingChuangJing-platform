package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;
import java.util.List;

public final class SubmissionResponses {

    private SubmissionResponses() {
    }

    public record SubmissionDetailResponse(
            Long id,
            Long taskId,
            Long exhibitionId,
            CommonResponses.SimpleUser submitter,
            Integer versionNo,
            String submitRemark,
            String submissionStatus,
            LocalDateTime submittedAt,
            LocalDateTime reviewedAt,
            List<ExhibitionResponses.SubmissionReviewResponse> reviews
    ) {
    }
}
