package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class HotspotRequests {
    private HotspotRequests() {}

    /**
     * 新建热点。
     * 坐标 x/y/w/h 为展区百分比（0-100）。
     * hotspotType 可选：navigation / exhibit-link / info / audio / video / image / custom
     */
    public record CreateHotspotRequest(
            @NotBlank(message = "热点类型不能为空")
            @Size(max = 32, message = "热点类型长度不能超过 32")
            String hotspotType,

            Long targetZoneId,

            @Size(max = 64, message = "热点名称长度不能超过 64")
            String label,

            @Size(max = 32, message = "图标标识长度不能超过 32")
            String icon,

            @NotNull(message = "x 坐标不能为空")
            @DecimalMin(value = "0", message = "x 坐标不能小于 0")
            @DecimalMax(value = "100", message = "x 坐标不能大于 100")
            Double xPercent,

            @NotNull(message = "y 坐标不能为空")
            @DecimalMin(value = "0", message = "y 坐标不能小于 0")
            @DecimalMax(value = "100", message = "y 坐标不能大于 100")
            Double yPercent,

            @DecimalMin(value = "0", message = "宽度不能小于 0")
            @DecimalMax(value = "100", message = "宽度不能大于 100")
            Double wPercent,

            @DecimalMin(value = "0", message = "高度不能小于 0")
            @DecimalMax(value = "100", message = "高度不能大于 100")
            Double hPercent,

            Object styleJson,
            Object actionConfig,
            Integer sortOrder
    ) {}

    /**
     * 更新热点。字段为 null 时保持原值（后端 COALESCE 策略）。
     */
    public record UpdateHotspotRequest(
            @Size(max = 32) String hotspotType,
            Long targetZoneId,
            @Size(max = 64) String label,
            @Size(max = 32) String icon,

            @DecimalMin(value = "0") @DecimalMax(value = "100") Double xPercent,
            @DecimalMin(value = "0") @DecimalMax(value = "100") Double yPercent,
            @DecimalMin(value = "0") @DecimalMax(value = "100") Double wPercent,
            @DecimalMin(value = "0") @DecimalMax(value = "100") Double hPercent,

            Object styleJson,
            Object actionConfig,
            Integer sortOrder
    ) {}
}
