package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.model.response.DashboardResponses;
import com.zhixingchuangjing.platform.repository.DashboardQueryRepository;
import com.zhixingchuangjing.platform.repository.ExhibitionQueryRepository;
import com.zhixingchuangjing.platform.repository.TaskQueryRepository;
import com.zhixingchuangjing.platform.service.DashboardService;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final TaskQueryRepository taskQueryRepository;
    private final ExhibitionQueryRepository exhibitionQueryRepository;
    private final DashboardQueryRepository dashboardQueryRepository;

    public DashboardServiceImpl(TaskQueryRepository taskQueryRepository,
                                ExhibitionQueryRepository exhibitionQueryRepository,
                                DashboardQueryRepository dashboardQueryRepository) {
        this.taskQueryRepository = taskQueryRepository;
        this.exhibitionQueryRepository = exhibitionQueryRepository;
        this.dashboardQueryRepository = dashboardQueryRepository;
    }

    @Override
    public DashboardResponses.DashboardOverviewResponse getOverview(Long userId, String role) {
        return new DashboardResponses.DashboardOverviewResponse(
                taskQueryRepository.findOngoingTasks(userId, role, 5),
                exhibitionQueryRepository.findRecentExhibitions(userId, 3),
                dashboardQueryRepository.findLatestAnnouncements(userId, 5),
                dashboardQueryRepository.findActivityFeeds(6),
                dashboardQueryRepository.findRecommendedResources(6),
                exhibitionQueryRepository.findFeaturedExhibitions(3)
        );
    }
}
