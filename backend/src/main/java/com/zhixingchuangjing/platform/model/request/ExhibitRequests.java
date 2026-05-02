package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class ExhibitRequests {
    private ExhibitRequests() {}

    public record CreateExhibitRequest(
        Long zoneId,
        String slotCode,
        String placementMode,
        Object placementJson,
        @NotBlank(message = "展品标题不能为空") @Size(max = 128) String title,
        @Size(max = 255) String subtitle,
        String exhibitType,
        String coverUrl,
        String mediaUrl,
        String sourceType,
        Long museumResourceId,
        Long mediaAssetId,
        String description,
        Object sourceInfo,
        List<String> knowledgePoints,
        Integer sortOrder
    ) {}

    public record UpdateExhibitRequest(
        String slotCode,
        String placementMode,
        Object placementJson,
        @Size(max = 128) String title,
        @Size(max = 255) String subtitle,
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
        String status
    ) {}

    public record UpsertNarrationRequest(
        Long id,
        String narrationType,
        @NotBlank(message = "讲解内容不能为空") String content,
        String audioUrl,
        String voiceType,
        Integer durationSeconds,
        Integer sortOrder
    ) {}

    public record UpsertInteractionRequest(
        Long id,
        @NotBlank(message = "互动类型不能为空") String interactionType,
        @NotBlank(message = "问题不能为空") String questionText,
        Object optionsJson,
        String correctAnswer,
        String explanation,
        Integer sortOrder
    ) {}
}
