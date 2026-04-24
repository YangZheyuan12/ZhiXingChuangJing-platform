package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.response.DashboardResponses;

public interface DashboardService {

    DashboardResponses.DashboardOverviewResponse getOverview(Long userId, String role);
}
