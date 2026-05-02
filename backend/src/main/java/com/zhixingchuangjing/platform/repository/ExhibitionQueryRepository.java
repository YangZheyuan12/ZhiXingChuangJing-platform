package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.model.response.CommunityResponses;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ExhibitionQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public ExhibitionQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean canAccessExhibition(Long exhibitionId, Long userId, String role) {
        String sql;
        if ("admin".equals(role)) {
            sql = "SELECT COUNT(1) FROM exhibitions WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, exhibitionId);
            return count != null && count > 0;
        }

        sql = """
                SELECT COUNT(DISTINCT e.id)
                FROM exhibitions e
                LEFT JOIN exhibition_members em ON em.exhibition_id = e.id AND em.user_id = ? AND em.status = 'active'
                WHERE e.id = ? AND (e.owner_id = ? OR em.user_id IS NOT NULL)
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, exhibitionId, userId);
        return count != null && count > 0;
    }

    public boolean isPublicPublishedExhibition(Long exhibitionId) {
        String sql = "SELECT COUNT(1) FROM exhibitions WHERE id = ? AND status = 'published' AND visibility = 'public'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, exhibitionId);
        return count != null && count > 0;
    }

    public List<ExhibitionResponses.ExhibitionSummaryResponse> findPagedMyExhibitions(Long userId,
                                                                                      Long taskId,
                                                                                      String status,
                                                                                      String keyword,
                                                                                      int offset,
                                                                                      int pageSize) {
        QuerySpec spec = buildMyExhibitionsQuery(userId, taskId, status, keyword, false);
        List<Object> args = new ArrayList<>(spec.args());
        args.add(offset);
        args.add(pageSize);
        return jdbcTemplate.query(spec.sql() + " ORDER BY e.updated_at DESC LIMIT ?, ?", (rs, rowNum) ->
                mapExhibitionSummary(rs.getLong("id"), rs.getObject("task_id", Long.class), rs.getString("title"),
                        rs.getString("cover_url"), rs.getString("summary"), rs.getString("status"),
                        rs.getString("visibility"), rs.getString("group_name"), rs.getLong("owner_id"),
                        rs.getLong("author_id"), rs.getString("author_role"), rs.getString("author_name"),
                        rs.getString("author_nickname"), rs.getString("author_avatar_url"),
                        rs.getInt("latest_version_no"), rs.getInt("published_version_no"),
                        rs.getInt("view_count"), rs.getInt("like_count"), rs.getInt("favorite_count"), rs.getInt("comment_count")),
                args.toArray());
    }

    public long countMyExhibitions(Long userId, Long taskId, String status, String keyword) {
        QuerySpec spec = buildMyExhibitionsQuery(userId, taskId, status, keyword, true);
        Long count = jdbcTemplate.queryForObject(spec.sql(), Long.class, spec.args().toArray());
        return count == null ? 0L : count;
    }

    public List<ExhibitionResponses.ExhibitionSummaryResponse> findRecentExhibitions(Long userId, int limit) {
        List<ExhibitionResponses.ExhibitionSummaryResponse> result = findPagedMyExhibitions(userId, null, null, null, 0, limit);
        return result;
    }

    public List<ExhibitionResponses.ExhibitionSummaryResponse> findFeaturedExhibitions(int limit) {
        String sql = """
                SELECT
                  e.id,
                  e.task_id,
                  e.title,
                  e.cover_url,
                  e.summary,
                  e.status,
                  e.visibility,
                  e.group_name,
                  e.owner_id,
                  e.latest_version_no,
                  e.published_version_no,
                  e.view_count,
                  e.like_count,
                  e.favorite_count,
                  e.comment_count,
                  u.id AS author_id,
                  u.role AS author_role,
                  u.real_name AS author_name,
                  u.nickname AS author_nickname,
                  u.avatar_url AS author_avatar_url
                FROM exhibitions e
                JOIN users u ON u.id = e.owner_id
                WHERE e.featured_flag = 1 AND e.status = 'published' AND e.visibility = 'public'
                ORDER BY e.featured_at DESC, e.published_at DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                mapExhibitionSummary(rs.getLong("id"), rs.getObject("task_id", Long.class), rs.getString("title"),
                        rs.getString("cover_url"), rs.getString("summary"), rs.getString("status"),
                        rs.getString("visibility"), rs.getString("group_name"), rs.getLong("owner_id"),
                        rs.getLong("author_id"), rs.getString("author_role"), rs.getString("author_name"),
                        rs.getString("author_nickname"), rs.getString("author_avatar_url"),
                        rs.getInt("latest_version_no"), rs.getInt("published_version_no"),
                        rs.getInt("view_count"), rs.getInt("like_count"), rs.getInt("favorite_count"), rs.getInt("comment_count")),
                limit);
    }

    public Optional<ExhibitionResponses.ExhibitionDetailResponse> findExhibitionDetail(Long exhibitionId) {
        String sql = """
                SELECT
                  e.id,
                  e.task_id,
                  e.title,
                  e.cover_url,
                  e.summary,
                  e.status,
                  e.visibility,
                  e.group_name,
                  e.owner_id,
                  e.latest_version_no,
                  e.published_version_no,
                  e.view_count,
                  e.like_count,
                  e.favorite_count,
                  e.comment_count,
                  e.created_at,
                  e.updated_at,
                  e.workflow_status,
                  e.visibility_scope,
                  e.is_featured,
                  e.bundle_revision,
                  u.id AS author_id,
                  u.role AS author_role,
                  u.real_name AS author_name,
                  u.nickname AS author_nickname,
                  u.avatar_url AS author_avatar_url
                FROM exhibitions e
                JOIN users u ON u.id = e.owner_id
                WHERE e.id = ?
                LIMIT 1
                """;
        List<ExhibitionResponses.ExhibitionDetailResponse> list = jdbcTemplate.query(sql, (rs, rowNum) ->
                new ExhibitionResponses.ExhibitionDetailResponse(
                        rs.getLong("id"),
                        rs.getObject("task_id", Long.class),
                        rs.getString("title"),
                        rs.getString("cover_url"),
                        rs.getString("summary"),
                        rs.getString("status"),
                        rs.getString("visibility"),
                        rs.getString("group_name"),
                        rs.getLong("owner_id"),
                        buildSimpleUser(rs.getLong("author_id"), rs.getString("author_role"),
                                rs.getString("author_name"), rs.getString("author_nickname"), rs.getString("author_avatar_url")),
                        rs.getInt("latest_version_no"),
                        rs.getInt("published_version_no"),
                        new ExhibitionResponses.ExhibitionStatsResponse(
                                rs.getInt("view_count"),
                                rs.getInt("like_count"),
                                rs.getInt("favorite_count"),
                                rs.getInt("comment_count")
                        ),
                        findExhibitionTags(rs.getLong("id")),
                        rs.getObject("created_at", LocalDateTime.class),
                        rs.getObject("updated_at", LocalDateTime.class),
                        findExhibitionMembers(rs.getLong("id")),
                        rs.getString("workflow_status"),
                        rs.getString("visibility_scope"),
                        rs.getBoolean("is_featured"),
                        rs.getInt("bundle_revision")
                ), exhibitionId);
        return list.stream().findFirst();
    }

    public List<ExhibitionResponses.ExhibitionMemberResponse> findExhibitionMembers(Long exhibitionId) {
        String sql = """
                SELECT
                  u.id AS user_id,
                  em.member_role,
                  COALESCE(u.nickname, u.real_name) AS display_name,
                  u.avatar_url,
                  em.joined_at
                FROM exhibition_members em
                JOIN users u ON u.id = em.user_id
                WHERE em.exhibition_id = ? AND em.status = 'active'
                ORDER BY FIELD(em.member_role, 'owner', 'editor', 'viewer'), em.joined_at ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ExhibitionResponses.ExhibitionMemberResponse(
                rs.getLong("user_id"),
                rs.getString("member_role"),
                rs.getString("display_name"),
                rs.getString("avatar_url"),
                rs.getObject("joined_at", LocalDateTime.class)
        ), exhibitionId);
    }

    public List<ExhibitionResponses.ExhibitionVersionResponse> findVersions(Long exhibitionId) {
        String sql = """
                SELECT
                  ev.id,
                  ev.version_no,
                  ev.save_type,
                  ev.version_note,
                  ev.canvas_width,
                  ev.canvas_height,
                  ev.canvas_background,
                  ev.zoom_ratio,
                  ev.element_count,
                  ev.version_data,
                  ev.created_at,
                  u.id AS creator_id,
                  u.role AS creator_role,
                  u.real_name AS creator_name,
                  u.nickname AS creator_nickname,
                  u.avatar_url AS creator_avatar
                FROM exhibition_versions ev
                JOIN users u ON u.id = ev.created_by
                WHERE ev.exhibition_id = ?
                ORDER BY ev.version_no DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ExhibitionResponses.ExhibitionVersionResponse(
                rs.getLong("id"),
                rs.getInt("version_no"),
                rs.getString("save_type"),
                rs.getString("version_note"),
                new ExhibitionResponses.CanvasConfigResponse(
                        rs.getInt("canvas_width"),
                        rs.getInt("canvas_height"),
                        rs.getString("canvas_background"),
                        rs.getDouble("zoom_ratio")
                ),
                rs.getInt("element_count"),
                rs.getString("version_data"),
                buildSimpleUser(
                        rs.getLong("creator_id"),
                        rs.getString("creator_role"),
                        rs.getString("creator_name"),
                        rs.getString("creator_nickname"),
                        rs.getString("creator_avatar")
                ),
                rs.getObject("created_at", LocalDateTime.class)
        ), exhibitionId);
    }

    public Optional<VersionPayload> findViewerVersionPayload(Long exhibitionId) {
        String sql = """
                SELECT
                  id,
                  exhibition_id,
                  version_no,
                  canvas_width,
                  canvas_height,
                  canvas_background,
                  zoom_ratio,
                  version_data
                FROM exhibition_versions
                WHERE exhibition_id = ?
                  AND version_no = (
                    SELECT CASE
                      WHEN published_version_no > 0 THEN published_version_no
                      ELSE latest_version_no
                    END
                    FROM exhibitions
                    WHERE id = ?
                  )
                LIMIT 1
                """;
        List<VersionPayload> list = jdbcTemplate.query(sql, (rs, rowNum) -> new VersionPayload(
                rs.getLong("id"),
                rs.getLong("exhibition_id"),
                rs.getInt("version_no"),
                rs.getInt("canvas_width"),
                rs.getInt("canvas_height"),
                rs.getString("canvas_background"),
                rs.getDouble("zoom_ratio"),
                rs.getString("version_data")
        ), exhibitionId, exhibitionId);
        return list.stream().findFirst();
    }

    public Optional<DigitalHumanPayload> findDigitalHuman(Long exhibitionId) {
        String sql = """
                SELECT
                  id,
                  exhibition_id,
                  name,
                  avatar_2d_url,
                  model_3d_url,
                  persona,
                  voice_type,
                  story_script,
                  story_timeline
                FROM digital_humans
                WHERE exhibition_id = ?
                LIMIT 1
                """;
        List<DigitalHumanPayload> list = jdbcTemplate.query(sql, (rs, rowNum) -> new DigitalHumanPayload(
                rs.getLong("id"),
                rs.getLong("exhibition_id"),
                rs.getString("name"),
                rs.getString("avatar_2d_url"),
                rs.getString("model_3d_url"),
                rs.getString("persona"),
                rs.getString("voice_type"),
                rs.getString("story_script"),
                rs.getString("story_timeline")
        ), exhibitionId);
        return list.stream().findFirst();
    }

    public List<ExhibitionResponses.DigitalHumanEquipmentResponse> findDigitalHumanEquipments(Long digitalHumanId) {
        String sql = """
                SELECT
                  dhe.id,
                  dhe.slot_code,
                  dhe.anchor_code,
                  dhe.display_order,
                  mr.id AS resource_id,
                  mr.title AS resource_title,
                  mr.museum_name,
                  dhe.resource_snapshot_json
                FROM digital_human_equipments dhe
                JOIN museum_resources mr ON mr.id = dhe.museum_resource_id
                WHERE dhe.digital_human_id = ?
                ORDER BY dhe.display_order ASC, dhe.id ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ExhibitionResponses.DigitalHumanEquipmentResponse(
                rs.getLong("id"),
                rs.getString("slot_code"),
                rs.getString("anchor_code"),
                rs.getInt("display_order"),
                rs.getLong("resource_id"),
                rs.getString("resource_title"),
                rs.getString("museum_name"),
                null
        ), digitalHumanId);
    }

    public List<ExhibitionResponses.SubmissionReviewResponse> findTeacherReviewsByExhibition(Long exhibitionId) {
        String sql = """
                SELECT
                  sr.id,
                  sr.score,
                  sr.comment_text,
                  sr.comment_audio_url,
                  sr.is_public,
                  sr.created_at,
                  u.id AS reviewer_id,
                  u.role AS reviewer_role,
                  u.real_name AS reviewer_name,
                  u.nickname AS reviewer_nickname,
                  u.avatar_url AS reviewer_avatar_url
                FROM submission_reviews sr
                JOIN task_submissions ts ON ts.id = sr.submission_id
                JOIN users u ON u.id = sr.reviewer_id
                WHERE ts.exhibition_id = ? AND sr.is_public = 1
                ORDER BY sr.created_at DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ExhibitionResponses.SubmissionReviewResponse(
                rs.getLong("id"),
                buildSimpleUser(rs.getLong("reviewer_id"), rs.getString("reviewer_role"),
                        rs.getString("reviewer_name"), rs.getString("reviewer_nickname"), rs.getString("reviewer_avatar_url")),
                rs.getObject("score", Double.class),
                rs.getString("comment_text"),
                rs.getString("comment_audio_url"),
                rs.getBoolean("is_public"),
                rs.getObject("created_at", LocalDateTime.class)
        ), exhibitionId);
    }

    public List<CommunityResponses.CommentResponse> findExhibitionComments(Long exhibitionId) {
        String sql = """
                SELECT
                  ec.id,
                  ec.exhibition_id,
                  ec.user_id,
                  ec.parent_comment_id,
                  ec.root_comment_id,
                  ec.content,
                  ec.status,
                  ec.created_at,
                  u.role,
                  u.real_name,
                  u.nickname,
                  u.avatar_url
                FROM exhibition_comments ec
                JOIN users u ON u.id = ec.user_id
                WHERE ec.exhibition_id = ? AND ec.status = 'normal'
                ORDER BY ec.created_at DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new CommunityResponses.CommentResponse(
                rs.getLong("id"),
                rs.getLong("exhibition_id"),
                rs.getLong("user_id"),
                rs.getObject("parent_comment_id", Long.class),
                rs.getObject("root_comment_id", Long.class),
                rs.getString("content"),
                rs.getString("status"),
                buildSimpleUser(rs.getLong("user_id"), rs.getString("role"),
                        rs.getString("real_name"), rs.getString("nickname"), rs.getString("avatar_url")),
                findMentionUsers(rs.getLong("id")),
                rs.getObject("created_at", LocalDateTime.class)
        ), exhibitionId);
    }

    private QuerySpec buildMyExhibitionsQuery(Long userId, Long taskId, String status, String keyword, boolean countOnly) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        if (countOnly) {
            sql.append("SELECT COUNT(DISTINCT e.id) ");
        } else {
            sql.append("""
                    SELECT DISTINCT
                      e.id,
                      e.task_id,
                      e.title,
                      e.cover_url,
                      e.summary,
                      e.status,
                      e.visibility,
                      e.group_name,
                      e.owner_id,
                      e.updated_at,
                      e.latest_version_no,
                      e.published_version_no,
                      e.view_count,
                      e.like_count,
                      e.favorite_count,
                      e.comment_count,
                      u.id AS author_id,
                      u.role AS author_role,
                      u.real_name AS author_name,
                      u.nickname AS author_nickname,
                      u.avatar_url AS author_avatar_url
                    """);
        }
        sql.append("""
                FROM exhibitions e
                JOIN users u ON u.id = e.owner_id
                LEFT JOIN exhibition_members em ON em.exhibition_id = e.id AND em.user_id = ? AND em.status = 'active'
                WHERE (e.owner_id = ? OR em.user_id IS NOT NULL)
                """);
        args.add(userId);
        args.add(userId);

        if (taskId != null) {
            sql.append(" AND e.task_id = ? ");
            args.add(taskId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND e.status = ? ");
            args.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (e.title LIKE ? OR e.summary LIKE ?) ");
            String likeKeyword = "%" + keyword.trim() + "%";
            args.add(likeKeyword);
            args.add(likeKeyword);
        }
        return new QuerySpec(sql.toString(), args);
    }

    private ExhibitionResponses.ExhibitionSummaryResponse mapExhibitionSummary(Long id,
                                                                              Long taskId,
                                                                              String title,
                                                                              String coverUrl,
                                                                              String summary,
                                                                              String status,
                                                                              String visibility,
                                                                              String groupName,
                                                                              Long ownerId,
                                                                              Long authorId,
                                                                              String authorRole,
                                                                              String authorName,
                                                                              String authorNickname,
                                                                              String authorAvatarUrl,
                                                                              Integer latestVersionNo,
                                                                              Integer publishedVersionNo,
                                                                              Integer viewCount,
                                                                              Integer likeCount,
                                                                              Integer favoriteCount,
                                                                              Integer commentCount) {
        return new ExhibitionResponses.ExhibitionSummaryResponse(
                id,
                taskId,
                title,
                coverUrl,
                summary,
                status,
                visibility,
                groupName,
                ownerId,
                buildSimpleUser(authorId, authorRole, authorName, authorNickname, authorAvatarUrl),
                latestVersionNo,
                publishedVersionNo,
                new ExhibitionResponses.ExhibitionStatsResponse(viewCount, likeCount, favoriteCount, commentCount),
                findExhibitionTags(id),
                null, null, null
        );
    }

    public List<String> findExhibitionTags(Long exhibitionId) {
        String sql = """
                SELECT et.tag_name
                FROM exhibition_tag_relations etr
                JOIN exhibition_tags et ON et.id = etr.tag_id
                WHERE etr.exhibition_id = ?
                ORDER BY et.tag_name ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("tag_name"), exhibitionId);
    }

    public List<CommonResponses.SimpleUser> findMentionUsers(Long commentId) {
        String sql = """
                SELECT
                  u.id,
                  u.role,
                  u.real_name,
                  u.nickname,
                  u.avatar_url
                FROM comment_mentions cm
                JOIN users u ON u.id = cm.mentioned_user_id
                WHERE cm.comment_id = ?
                ORDER BY u.id ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> buildSimpleUser(
                rs.getLong("id"),
                rs.getString("role"),
                rs.getString("real_name"),
                rs.getString("nickname"),
                rs.getString("avatar_url")
        ), commentId);
    }

    private CommonResponses.SimpleUser buildSimpleUser(Long id, String role, String name, String nickname, String avatarUrl) {
        return new CommonResponses.SimpleUser(id, role, name, nickname, avatarUrl);
    }

    public record TemplatePayload(Long id, String zonesConfig) {}

    public TemplatePayload findTemplateByCode(String templateCode) {
        String sql = "SELECT id, zones_config FROM exhibition_templates WHERE template_code = ? AND status = 'active' LIMIT 1";
        List<TemplatePayload> list = jdbcTemplate.query(sql, (rs, rowNum) ->
                new TemplatePayload(rs.getLong("id"), rs.getString("zones_config")), templateCode);
        return list.isEmpty() ? null : list.get(0);
    }

    public Integer getBundleRevision(Long exhibitionId) {
        String sql = "SELECT bundle_revision FROM exhibitions WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, exhibitionId);
    }

    private record QuerySpec(
            String sql,
            List<Object> args
    ) {
    }

    public record VersionPayload(
            Long id,
            Long exhibitionId,
            Integer versionNo,
            Integer canvasWidth,
            Integer canvasHeight,
            String canvasBackground,
            Double zoomRatio,
            String versionDataJson
    ) {
    }

    public record DigitalHumanPayload(
            Long id,
            Long exhibitionId,
            String name,
            String avatar2dUrl,
            String model3dUrl,
            String persona,
            String voiceType,
            String storyScript,
            String storyTimelineJson
    ) {
    }
}
