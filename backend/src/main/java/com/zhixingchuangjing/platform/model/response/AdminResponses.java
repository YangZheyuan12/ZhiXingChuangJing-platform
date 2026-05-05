package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;
import java.util.List;

public final class AdminResponses {

    private AdminResponses() {
    }

    public record TeacherRegistrationItemResponse(
            Long userId,
            String account,
            String role,
            String realName,
            String nickname,
            Long schoolId,
            String schoolName,
            String teacherNo,
            String status,
            String reviewRemark,
            LocalDateTime createdAt
    ) {
    }

    public record TeacherRegistrationListResponse(
            List<TeacherRegistrationItemResponse> list
    ) {
    }
}
