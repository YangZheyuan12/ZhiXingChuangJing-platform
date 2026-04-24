package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class CommunityRequests {

    private CommunityRequests() {
    }

    public record CreateCommentRequest(
            @NotBlank(message = "评论内容不能为空")
            @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
            String content,
            Long parentCommentId,
            List<Long> mentionUserIds
    ) {
    }

    public record ShareExhibitionRequest(
            @NotBlank(message = "分享渠道不能为空")
            @Size(max = 32, message = "分享渠道长度不能超过32个字符")
            String channel
    ) {
    }

    public record FeatureExhibitionRequest(
            Boolean featured,
            @Size(max = 255, message = "推荐理由长度不能超过255个字符")
            String featuredReason
    ) {
    }
}
