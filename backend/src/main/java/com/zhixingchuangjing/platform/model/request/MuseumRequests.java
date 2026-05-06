package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public final class MuseumRequests {

    private MuseumRequests() {
    }

    public record SyncMuseumResourcesRequest(
            @NotBlank(message = "资源提供方不能为空")
            @Size(max = 64, message = "资源提供方长度不能超过 64 个字符")
            String providerCode,
            @Size(max = 64, message = "分类长度不能超过 64 个字符")
            String category,
            @Size(max = 255, message = "关键词长度不能超过 255 个字符")
            String keyword
    ) {
    }

    public record ImportCsvRequest(
            @Size(max = 255, message = "关键词长度不能超过 255 个字符")
            String keyword
    ) {
    }

    public record FavoriteRequest(
            @Size(max = 64, message = "文件夹名称长度不能超过 64 个字符")
            String folder
    ) {
    }
}
