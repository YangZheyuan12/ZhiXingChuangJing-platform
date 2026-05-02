package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ZoneRequests {
    private ZoneRequests() {}

    public record CreateZoneRequest(
        @NotBlank(message = "展区编码不能为空") @Size(max = 64) String zoneCode,
        @NotBlank(message = "展区类型不能为空") String zoneType,
        @NotBlank(message = "展区标题不能为空") @Size(max = 128) String title,
        @Size(max = 255) String subtitle,
        String description,
        Integer sortOrder,
        String backgroundUrl,
        String transitionIn
    ) {}

    public record UpdateZoneRequest(
        @Size(max = 128) String title,
        @Size(max = 255) String subtitle,
        String description,
        String backgroundUrl,
        Object backgroundStyle,
        Object layoutConfig,
        String narrationText,
        String transitionIn,
        Integer sortOrder
    ) {}

    public record UpdateCanvasDataRequest(
        Object canvasData
    ) {}

    public record AssignZoneRequest(
        Long assignedUserId
    ) {}

    public record LockZoneRequest(
        boolean lock
    ) {}
}
