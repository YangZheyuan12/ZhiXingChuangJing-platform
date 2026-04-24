package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;
import java.util.List;

public final class UserResponses {

    private UserResponses() {
    }

    public record UserProfileResponse(
            Long id,
            String role,
            String realName,
            String nickname,
            String avatarUrl,
            String email,
            String mobile,
            String bio,
            CommonResponses.SchoolInfo school,
            CommonResponses.ClassInfo classInfo
    ) {
    }

    public record UserHomepageStatsResponse(
            Integer exhibitionCount,
            Integer likeCount,
            Integer teacherPraiseCount
    ) {
    }

    public record PortfolioItemResponse(
            Long exhibitionId,
            String title,
            String coverUrl,
            LocalDateTime publishedAt,
            Integer viewCount,
            Integer likeCount
    ) {
    }

    public record UserHomepageResponse(
            UserProfileResponse user,
            UserHomepageStatsResponse stats,
            List<PortfolioItemResponse> portfolio
    ) {
    }
}
