package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.ClassResponses;
import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.model.response.DashboardResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DashboardQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public DashboardQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ClassResponses.AnnouncementResponse> findLatestAnnouncements(Long userId, int limit) {
        String sql = """
                SELECT
                  a.id,
                  a.class_id,
                  a.title,
                  a.content,
                  a.pinned,
                  a.published_at,
                  u.id AS publisher_id,
                  u.role AS publisher_role,
                  u.real_name AS publisher_name,
                  u.nickname AS publisher_nickname,
                  u.avatar_url AS publisher_avatar_url
                FROM announcements a
                JOIN class_members cm ON cm.class_id = a.class_id AND cm.user_id = ? AND cm.status = 'active'
                JOIN users u ON u.id = a.publisher_id
                ORDER BY a.pinned DESC, a.published_at DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ClassResponses.AnnouncementResponse(
                rs.getLong("id"),
                rs.getLong("class_id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getBoolean("pinned"),
                new CommonResponses.SimpleUser(
                        rs.getLong("publisher_id"),
                        rs.getString("publisher_role"),
                        rs.getString("publisher_name"),
                        rs.getString("publisher_nickname"),
                        rs.getString("publisher_avatar_url")
                ),
                rs.getObject("published_at", LocalDateTime.class)
        ), userId, limit);
    }

    public List<DashboardResponses.ActivityFeedResponse> findActivityFeeds(int limit) {
        String sql = """
                SELECT
                  e.id,
                  'exhibition_published' AS feed_type,
                  CONCAT(COALESCE(u.nickname, u.real_name), ' 发布了新展厅《', e.title, '》') AS content,
                  u.id AS operator_id,
                  u.role AS operator_role,
                  u.real_name AS operator_name,
                  u.nickname AS operator_nickname,
                  u.avatar_url AS operator_avatar_url,
                  COALESCE(e.published_at, e.updated_at) AS created_at
                FROM exhibitions e
                JOIN users u ON u.id = e.owner_id
                WHERE e.status = 'published'
                ORDER BY COALESCE(e.published_at, e.updated_at) DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new DashboardResponses.ActivityFeedResponse(
                rs.getLong("id"),
                rs.getString("feed_type"),
                rs.getString("content"),
                new CommonResponses.SimpleUser(
                        rs.getLong("operator_id"),
                        rs.getString("operator_role"),
                        rs.getString("operator_name"),
                        rs.getString("operator_nickname"),
                        rs.getString("operator_avatar_url")
                ),
                rs.getObject("created_at", LocalDateTime.class)
        ), limit);
    }

    public List<DashboardResponses.MuseumResourceResponse> findRecommendedResources(int limit) {
        String sql = """
                SELECT
                  mr.id,
                  mp.provider_code,
                  mr.title,
                  mr.category,
                  mr.museum_name,
                  mr.cover_url,
                  mr.detail_url,
                  mr.description
                FROM museum_resources mr
                JOIN museum_providers mp ON mp.id = mr.provider_id
                WHERE mr.cache_status = 'fresh'
                ORDER BY mr.updated_at DESC, mr.id DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new DashboardResponses.MuseumResourceResponse(
                rs.getLong("id"),
                rs.getString("provider_code"),
                rs.getString("title"),
                rs.getString("category"),
                rs.getString("museum_name"),
                rs.getString("cover_url"),
                rs.getString("detail_url"),
                rs.getString("description")
        ), limit);
    }
}
