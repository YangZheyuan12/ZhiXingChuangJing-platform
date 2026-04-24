package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;
import java.util.List;

public final class DashboardResponses {

    private DashboardResponses() {
    }

    public record MuseumResourceResponse(
            Long id,
            String providerCode,
            String title,
            String category,
            String museumName,
            String coverUrl,
            String detailUrl,
            String description
    ) {
    }

    public record ActivityFeedResponse(
            Long id,
            String type,
            String content,
            CommonResponses.SimpleUser operator,
            LocalDateTime createdAt
    ) {
    }

    public record DashboardOverviewResponse(
            List<TaskResponses.TaskSummaryResponse> ongoingTasks,
            List<ExhibitionResponses.ExhibitionSummaryResponse> recentExhibitions,
            List<ClassResponses.AnnouncementResponse> announcements,
            List<ActivityFeedResponse> activityFeeds,
            List<MuseumResourceResponse> recommendedResources,
            List<ExhibitionResponses.ExhibitionSummaryResponse> featuredExhibitions
    ) {
    }
}
