package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;

public final class NotificationResponses {

    private NotificationResponses() {
    }

    public record NotificationResponse(
            Long id,
            String notificationType,
            String title,
            String content,
            String bizType,
            Long bizId,
            String readStatus,
            LocalDateTime createdAt,
            LocalDateTime readAt
    ) {
    }
}
