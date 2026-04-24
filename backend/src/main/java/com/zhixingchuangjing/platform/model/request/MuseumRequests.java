package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class MuseumRequests {

    private MuseumRequests() {
    }

    public record SyncMuseumResourcesRequest(
            @NotBlank(message = "资源提供方不能为空")
            @Size(max = 64, message = "资源提供方长度不能超过64个字符")
            String providerCode,
            @Size(max = 64, message = "分类长度不能超过64个字符")
            String category,
            @Size(max = 255, message = "关键词长度不能超过255个字符")
            String keyword
    ) {
    }
}
