package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.util.PageUtils;
import com.zhixingchuangjing.platform.model.request.NotificationRequests;
import com.zhixingchuangjing.platform.model.response.NotificationResponses;
import com.zhixingchuangjing.platform.repository.NotificationCommandRepository;
import com.zhixingchuangjing.platform.repository.NotificationQueryRepository;
import com.zhixingchuangjing.platform.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationQueryRepository notificationQueryRepository;
    private final NotificationCommandRepository notificationCommandRepository;

    public NotificationServiceImpl(NotificationQueryRepository notificationQueryRepository,
                                   NotificationCommandRepository notificationCommandRepository) {
        this.notificationQueryRepository = notificationQueryRepository;
        this.notificationCommandRepository = notificationCommandRepository;
    }

    @Override
    public PageResponse<NotificationResponses.NotificationResponse> getNotifications(Long userId,
                                                                                     String readStatus,
                                                                                     Integer page,
                                                                                     Integer pageSize) {
        int normalizedPage = PageUtils.normalizePage(page);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return new PageResponse<>(
                notificationQueryRepository.findNotifications(userId, readStatus,
                        PageUtils.offset(normalizedPage, normalizedPageSize),
                        normalizedPageSize),
                normalizedPage,
                normalizedPageSize,
                notificationQueryRepository.countNotifications(userId, readStatus)
        );
    }

    @Override
    @Transactional
    public void readNotifications(Long userId, NotificationRequests.ReadNotificationsRequest request) {
        notificationCommandRepository.markAsRead(
                userId,
                request.notificationIds().stream().filter(id -> id != null).distinct().toList()
        );
    }
}
