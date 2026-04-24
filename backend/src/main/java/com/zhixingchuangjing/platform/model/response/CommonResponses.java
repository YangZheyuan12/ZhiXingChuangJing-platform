package com.zhixingchuangjing.platform.model.response;

public final class CommonResponses {

    private CommonResponses() {
    }

    public record SchoolInfo(
            Long id,
            String name
    ) {
    }

    public record ClassInfo(
            Long id,
            String name,
            String grade,
            String academicYear
    ) {
    }

    public record SimpleUser(
            Long id,
            String role,
            String name,
            String nickname,
            String avatarUrl
    ) {
    }
}
