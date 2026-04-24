package com.zhixingchuangjing.platform.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class CommunityCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommunityCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createComment(Long exhibitionId, Long userId, Long parentCommentId, Long rootCommentId, String content) {
        String sql = """
                INSERT INTO exhibition_comments (
                  exhibition_id, user_id, parent_comment_id, root_comment_id, content, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, 'normal', NOW(), NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, exhibitionId);
            ps.setLong(2, userId);
            ps.setObject(3, parentCommentId);
            ps.setObject(4, rootCommentId);
            ps.setString(5, content);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Long findRootCommentId(Long parentCommentId) {
        String sql = """
                SELECT COALESCE(root_comment_id, id)
                FROM exhibition_comments
                WHERE id = ?
                LIMIT 1
                """;
        return jdbcTemplate.queryForObject(sql, Long.class, parentCommentId);
    }

    public Integer countCommentById(Long exhibitionId, Long commentId) {
        String sql = "SELECT COUNT(1) FROM exhibition_comments WHERE exhibition_id = ? AND id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, exhibitionId, commentId);
        return count == null ? 0 : count;
    }

    public void createMentions(Long commentId, List<Long> mentionUserIds) {
        if (mentionUserIds == null || mentionUserIds.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO comment_mentions (comment_id, mentioned_user_id, created_at) VALUES (?, ?, NOW())";
        jdbcTemplate.batchUpdate(sql, mentionUserIds, mentionUserIds.size(), (ps, userId) -> {
            ps.setLong(1, commentId);
            ps.setLong(2, userId);
        });
    }

    public void increaseCommentCount(Long exhibitionId) {
        String sql = "UPDATE exhibitions SET comment_count = comment_count + 1, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, exhibitionId);
    }

    public void createNotification(Long userId, String type, String title, String content, String bizType, Long bizId) {
        String sql = """
                INSERT INTO notifications (
                  user_id, notification_type, title, content, biz_type, biz_id, read_status, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, 'unread', NOW())
                """;
        jdbcTemplate.update(sql, userId, type, title, content, bizType, bizId);
    }

    public boolean hasInteraction(Long exhibitionId, Long userId, String interactionType, String channel) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM exhibition_interactions
                WHERE exhibition_id = ? AND user_id = ? AND interaction_type = ? AND channel = ?
                """, Integer.class, exhibitionId, userId, interactionType, channel);
        return count != null && count > 0;
    }

    public void addInteraction(Long exhibitionId, Long userId, String interactionType, String channel) {
        jdbcTemplate.update("""
                INSERT INTO exhibition_interactions (exhibition_id, user_id, interaction_type, channel, created_at)
                VALUES (?, ?, ?, ?, NOW())
                """, exhibitionId, userId, interactionType, channel);
    }

    public void removeInteraction(Long exhibitionId, Long userId, String interactionType) {
        jdbcTemplate.update("""
                DELETE FROM exhibition_interactions
                WHERE exhibition_id = ? AND user_id = ? AND interaction_type = ?
                """, exhibitionId, userId, interactionType);
    }

    public void updateInteractionCount(Long exhibitionId, String fieldName, int delta) {
        String sql = "UPDATE exhibitions SET " + fieldName + " = GREATEST(" + fieldName + " + ?, 0), updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, delta, exhibitionId);
    }

    public void setFeatured(Long exhibitionId, boolean featured, String featuredReason) {
        String sql = """
                UPDATE exhibitions
                SET featured_flag = ?,
                    featured_reason = ?,
                    featured_at = CASE WHEN ? = 1 THEN NOW() ELSE NULL END,
                    updated_at = NOW()
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, featured, featuredReason, featured ? 1 : 0, exhibitionId);
    }
}
