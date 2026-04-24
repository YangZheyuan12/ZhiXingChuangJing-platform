package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ExhibitionResponses {

    private ExhibitionResponses() {
    }

    public record ExhibitionStatsResponse(
            Integer viewCount,
            Integer likeCount,
            Integer favoriteCount,
            Integer commentCount
    ) {
    }

    public record ExhibitionSummaryResponse(
            Long id,
            Long taskId,
            String title,
            String coverUrl,
            String summary,
            String status,
            String visibility,
            String groupName,
            Long ownerId,
            CommonResponses.SimpleUser author,
            Integer latestVersionNo,
            Integer publishedVersionNo,
            ExhibitionStatsResponse stats,
            List<String> tags
    ) {
    }

    public record ExhibitionMemberResponse(
            Long userId,
            String role,
            String name,
            String avatarUrl,
            LocalDateTime joinedAt
    ) {
    }

    public record ExhibitionDetailResponse(
            Long id,
            Long taskId,
            String title,
            String coverUrl,
            String summary,
            String status,
            String visibility,
            String groupName,
            Long ownerId,
            CommonResponses.SimpleUser author,
            Integer latestVersionNo,
            Integer publishedVersionNo,
            ExhibitionStatsResponse stats,
            List<String> tags,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<ExhibitionMemberResponse> collaborators
    ) {
    }

    public record CanvasConfigResponse(
            Integer width,
            Integer height,
            String background,
            Double zoom
    ) {
    }

    public record ExhibitionElementResponse(
            String componentType,
            Double x,
            Double y,
            Double width,
            Double height,
            Map<String, Object> props
    ) {
    }

    public record ExhibitionRenderDataResponse(
            CanvasConfigResponse canvasConfig,
            List<ExhibitionElementResponse> elements
    ) {
    }

    public record ExhibitionVersionResponse(
            Long id,
            Integer versionNo,
            String saveType,
            String versionNote,
            CanvasConfigResponse canvasConfig,
            Integer elementCount,
            Object versionData,
            CommonResponses.SimpleUser createdBy,
            LocalDateTime createdAt
    ) {
    }

    public record DigitalHumanEquipmentResponse(
            Long id,
            String slotCode,
            String anchorCode,
            Integer displayOrder,
            Long resourceId,
            String resourceTitle,
            String museumName,
            Map<String, Object> resourceSnapshot
    ) {
    }

    public record DigitalHumanResponse(
            Long id,
            Long exhibitionId,
            String name,
            String avatar2dUrl,
            String model3dUrl,
            String persona,
            String voiceType,
            String storyScript,
            List<Map<String, Object>> storyTimeline,
            List<DigitalHumanEquipmentResponse> equippedItems
    ) {
    }

    public record SubmissionReviewResponse(
            Long id,
            CommonResponses.SimpleUser reviewer,
            Double score,
            String commentText,
            String commentAudioUrl,
            Boolean isPublic,
            LocalDateTime createdAt
    ) {
    }

    public record ExhibitionViewerDataResponse(
            ExhibitionDetailResponse exhibition,
            ExhibitionRenderDataResponse renderData,
            DigitalHumanResponse digitalHuman,
            List<SubmissionReviewResponse> teacherReviews,
            List<CommunityResponses.CommentResponse> comments
    ) {
    }
}
