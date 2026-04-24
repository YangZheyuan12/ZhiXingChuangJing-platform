package com.zhixingchuangjing.platform.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotificationCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void markAsRead(Long userId, List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }
        String placeholders = String.join(",", notificationIds.stream().map(id -> "?").toList());
        String sql = """
                UPDATE notifications
                SET read_status = 'read', read_at = NOW()
                WHERE user_id = ? AND id IN (%s)
                """.formatted(placeholders);

        Object[] args = new Object[notificationIds.size() + 1];
        args[0] = userId;
        for (int index = 0; index < notificationIds.size(); index++) {
            args[index + 1] = notificationIds.get(index);
        }
        jdbcTemplate.update(sql, args);
    }
}
