package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public final class SubmissionRequests {

    private SubmissionRequests() {
    }

    public record CreateSubmissionReviewRequest(
            @DecimalMin(value = "0.0", message = "评分不能小于0")
            @DecimalMax(value = "100.0", message = "评分不能大于100")
            Double score,
            String commentText,
            @Size(max = 255, message = "点评音频地址长度不能超过255个字符")
            String commentAudioUrl,
            Boolean isPublic
    ) {
    }
}
