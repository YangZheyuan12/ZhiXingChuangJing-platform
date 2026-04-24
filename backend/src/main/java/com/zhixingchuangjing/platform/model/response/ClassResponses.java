package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;

public final class ClassResponses {

    private ClassResponses() {
    }

    public record ClassDetailResponse(
            Long id,
            String name,
            String grade,
            String academicYear,
            CommonResponses.SchoolInfo school,
            CommonResponses.SimpleUser headTeacher,
            Integer memberCount
    ) {
    }

    public record ClassMemberResponse(
            Long userId,
            String role,
            String realName,
            String nickname,
            String avatarUrl,
            LocalDateTime joinedAt
    ) {
    }

    public record AnnouncementResponse(
            Long id,
            Long classId,
            String title,
            String content,
            Boolean pinned,
            CommonResponses.SimpleUser publisher,
            LocalDateTime publishedAt
    ) {
    }
}
