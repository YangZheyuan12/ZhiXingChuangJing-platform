package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.NotificationResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotificationQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<NotificationResponses.NotificationResponse> findNotifications(Long userId,
                                                                              String readStatus,
                                                                              int offset,
                                                                              int pageSize) {
        QuerySpec spec = buildNotificationQuery(userId, readStatus, false);
        List<Object> args = new java.util.ArrayList<>(spec.args());
        args.add(offset);
        args.add(pageSize);
        return jdbcTemplate.query(spec.sql() + " ORDER BY created_at DESC LIMIT ?, ?", (rs, rowNum) -> new NotificationResponses.NotificationResponse(
                rs.getLong("id"),
                rs.getString("notification_type"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("biz_type"),
                rs.getObject("biz_id", Long.class),
                rs.getString("read_status"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("read_at") == null ? null : rs.getTimestamp("read_at").toLocalDateTime()
        ), args.toArray());
    }

    public long countNotifications(Long userId, String readStatus) {
        QuerySpec spec = buildNotificationQuery(userId, readStatus, true);
        Long count = jdbcTemplate.queryForObject(spec.sql(), Long.class, spec.args().toArray());
        return count == null ? 0L : count;
    }

    private QuerySpec buildNotificationQuery(Long userId, String readStatus, boolean countOnly) {
        StringBuilder sql = new StringBuilder(countOnly ? "SELECT COUNT(1) " : """
                SELECT
                  id,
                  notification_type,
                  title,
                  content,
                  biz_type,
                  biz_id,
                  read_status,
                  created_at,
                  read_at
                """);
        sql.append("FROM notifications WHERE user_id = ? ");
        java.util.List<Object> args = new java.util.ArrayList<>();
        args.add(userId);
        if (readStatus != null && !readStatus.isBlank()) {
            sql.append(" AND read_status = ? ");
            args.add(readStatus);
        }
        return new QuerySpec(sql.toString(), args);
    }

    private record QuerySpec(String sql, java.util.List<Object> args) {
    }
}
