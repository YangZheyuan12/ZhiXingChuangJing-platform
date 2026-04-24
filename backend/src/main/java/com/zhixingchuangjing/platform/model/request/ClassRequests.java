package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ClassRequests {

    private ClassRequests() {
    }

    public record CreateAnnouncementRequest(
            @NotBlank(message = "公告标题不能为空")
            @Size(max = 128, message = "公告标题长度不能超过128个字符")
            String title,
            @NotBlank(message = "公告内容不能为空")
            String content,
            Boolean pinned
    ) {
    }
}
