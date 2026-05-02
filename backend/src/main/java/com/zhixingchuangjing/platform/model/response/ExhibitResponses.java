package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;
import java.util.List;

public final class ExhibitResponses {
    private ExhibitResponses() {}

    public record ExhibitResponse(
        Long id,
        Long exhibitionId,
        Long zoneId,
        String slotCode,
        String placementMode,
        Object placementJson,
        String title,
        String subtitle,
        String exhibitType,
        String coverUrl,
        String mediaUrl,
        String sourceType,
        Long museumResourceId,
        Long mediaAssetId,
        String description,
        Object sourceInfo,
        List<String> knowledgePoints,
        Integer sortOrder,
        String status,
        List<NarrationResponse> narrations,
        List<InteractionResponse> interactions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record ExhibitSummaryResponse(
        Long id,
        Long zoneId,
        String slotCode,
        String title,
        String exhibitType,
        String coverUrl,
        Integer sortOrder,
        String status
    ) {}

    public record NarrationResponse(
        Long id,
        Long exhibitId,
        String narrationType,
        String content,
        String audioUrl,
        String voiceType,
        Integer durationSeconds,
        Integer sortOrder,
        LocalDateTime createdAt
    ) {}

    public record InteractionResponse(
        Long id,
        Long exhibitId,
        String interactionType,
        String questionText,
        Object optionsJson,
        String correctAnswer,
        String explanation,
        Integer sortOrder,
        LocalDateTime createdAt
    ) {}
}
