package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.model.request.NotificationRequests;
import com.zhixingchuangjing.platform.model.response.NotificationResponses;

public interface NotificationService {

    PageResponse<NotificationResponses.NotificationResponse> getNotifications(Long userId,
                                                                              String readStatus,
                                                                              Integer page,
                                                                              Integer pageSize);

    void readNotifications(Long userId, NotificationRequests.ReadNotificationsRequest request);
}
