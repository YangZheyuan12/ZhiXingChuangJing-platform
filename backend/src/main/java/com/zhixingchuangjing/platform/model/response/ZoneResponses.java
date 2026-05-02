package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;

public final class ZoneResponses {
    private ZoneResponses() {}

    public record ZoneResponse(
        Long id,
        Long exhibitionId,
        String zoneCode,
        String zoneType,
        String title,
        String subtitle,
        String description,
        String backgroundUrl,
        Object backgroundStyle,
        Object layoutConfig,
        String transitionIn,
        String narrationText,
        String narrationAudio,
        Object canvasData,
        Integer sortOrder,
        Long assignedUserId,
        Long lockedBy,
        LocalDateTime lockedAt,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record ZoneSummaryResponse(
        Long id,
        String zoneCode,
        String zoneType,
        String title,
        Integer sortOrder,
        String backgroundUrl,
        String transitionIn,
        Long assignedUserId,
        Long lockedBy,
        LocalDateTime lockedAt,
        String status
    ) {}
}
