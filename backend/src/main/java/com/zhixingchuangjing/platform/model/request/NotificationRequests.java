package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public final class NotificationRequests {

    private NotificationRequests() {
    }

    public record ReadNotificationsRequest(
            @NotEmpty(message = "待标记通知不能为空")
            List<Long> notificationIds
    ) {
    }
}
