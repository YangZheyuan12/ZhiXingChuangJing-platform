package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    public record PlaceDigitalHumanRequest(
        @NotNull(message = "数字人ID不能为空") Long digitalHumanId,
        @DecimalMin(value = "0.0", message = "X 百分比不能小于 0") @DecimalMax(value = "100.0", message = "X 百分比不能超过 100") Double xPercent,
        @DecimalMin(value = "0.0", message = "Y 百分比不能小于 0") @DecimalMax(value = "100.0", message = "Y 百分比不能超过 100") Double yPercent,
        @DecimalMin(value = "0.1", message = "缩放不能小于 0.1") @DecimalMax(value = "5.0", message = "缩放不能超过 5.0") Double scale,
        @Pattern(regexp = "left|right", message = "朝向只支持 left / right") String facing
    ) {}
}
