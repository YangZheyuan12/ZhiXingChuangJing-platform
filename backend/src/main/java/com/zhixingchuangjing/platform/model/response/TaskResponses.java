package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;
import java.util.List;

public final class TaskResponses {

    private TaskResponses() {
    }

    public record TaskMaterialResponse(
            Long id,
            String title,
            String materialType,
            String url,
            String description
    ) {
    }

    public record TaskSummaryResponse(
            Long id,
            String title,
            String coverUrl,
            String description,
            CommonResponses.SimpleUser teacher,
            LocalDateTime startTime,
            LocalDateTime dueTime,
            String status
    ) {
    }

    public record TaskDetailResponse(
            Long id,
            String title,
            String coverUrl,
            String description,
            CommonResponses.SimpleUser teacher,
            LocalDateTime startTime,
            LocalDateTime dueTime,
            String status,
            String evaluationCriteria,
            List<TaskMaterialResponse> backgroundMaterials,
            List<CommonResponses.ClassInfo> targetClasses,
            List<ExhibitionResponses.ExhibitionSummaryResponse> excellentExhibitions
    ) {
    }

    public record TaskProgressGroupResponse(
            Long exhibitionId,
            String groupName,
            String leaderName,
            Integer memberCount,
            Integer progressPercent,
            String submissionStatus,
            LocalDateTime updatedAt
    ) {
    }

    public record TaskProgressResponse(
            Long taskId,
            Integer submittedCount,
            Integer reviewedCount,
            List<TaskProgressGroupResponse> groups
    ) {
    }
}
