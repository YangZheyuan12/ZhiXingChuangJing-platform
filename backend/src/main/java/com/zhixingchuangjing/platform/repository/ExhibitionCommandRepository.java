package com.zhixingchuangjing.platform.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ExhibitionCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public ExhibitionCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createExhibition(Long ownerId,
                                 Long taskId,
                                 String title,
                                 String coverUrl,
                                 String summary,
                                 String groupName,
                                 String visibility,
                                 Long templateId,
                                 String templateSnapshotJson) {
        String sql = """
                INSERT INTO exhibitions (
                  task_id, owner_id, title, cover_url, summary, group_name, status, visibility,
                  workflow_status, visibility_scope, bundle_revision,
                  template_id, template_snapshot_json,
                  latest_version_no, published_version_no, featured_flag,
                  view_count, like_count, favorite_count, comment_count,
                  created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, 'draft', ?,
                  'draft', 'private', 0,
                  ?, ?,
                  0, 0, 0, 0, 0, 0, 0, NOW(), NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, taskId);
            ps.setLong(2, ownerId);
            ps.setString(3, title);
            ps.setString(4, coverUrl);
            ps.setString(5, summary);
            ps.setString(6, groupName);
            ps.setString(7, visibility);
            ps.setObject(8, templateId);
            ps.setString(9, templateSnapshotJson);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void updateExhibition(Long exhibitionId,
                                 String title,
                                 String coverUrl,
                                 String summary,
                                 String visibility) {
        String sql = """
                UPDATE exhibitions
                SET title = ?,
                    cover_url = ?,
                    summary = ?,
                    visibility = ?,
                    updated_at = NOW()
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, title, coverUrl, summary, visibility, exhibitionId);
    }

    public void addOwnerMember(Long exhibitionId, Long ownerId) {
        String sql = """
                INSERT INTO exhibition_members (exhibition_id, user_id, member_role, joined_at, status)
                VALUES (?, ?, 'owner', NOW(), 'active')
                """;
        jdbcTemplate.update(sql, exhibitionId, ownerId);
    }

    public void addMembers(Long exhibitionId, List<Long> memberUserIds, String role) {
        if (memberUserIds == null || memberUserIds.isEmpty()) {
            return;
        }
        String sql = """
                INSERT INTO exhibition_members (exhibition_id, user_id, member_role, joined_at, status)
                VALUES (?, ?, ?, NOW(), 'active')
                ON DUPLICATE KEY UPDATE
                  member_role = VALUES(member_role),
                  status = 'active'
                """;
        jdbcTemplate.batchUpdate(sql, memberUserIds, memberUserIds.size(), (ps, userId) -> {
            ps.setLong(1, exhibitionId);
            ps.setLong(2, userId);
            ps.setString(3, role);
        });
    }

    public Integer getNextVersionNo(Long exhibitionId) {
        String sql = "SELECT COALESCE(MAX(version_no), 0) + 1 FROM exhibition_versions WHERE exhibition_id = ?";
        Integer nextVersionNo = jdbcTemplate.queryForObject(sql, Integer.class, exhibitionId);
        return nextVersionNo == null ? 1 : nextVersionNo;
    }

    public Long insertVersion(Long exhibitionId,
                              Integer versionNo,
                              String saveType,
                              String versionNote,
                              Integer canvasWidth,
                              Integer canvasHeight,
                              String canvasBackground,
                              Double zoomRatio,
                              Integer elementCount,
                              String versionDataJson,
                              Long createdBy) {
        String sql = """
                INSERT INTO exhibition_versions (
                  exhibition_id, version_no, save_type, version_note, canvas_width, canvas_height,
                  canvas_background, zoom_ratio, element_count, version_data, created_by, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS JSON), ?, NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, exhibitionId);
            ps.setInt(2, versionNo);
            ps.setString(3, saveType);
            ps.setString(4, versionNote);
            ps.setInt(5, canvasWidth);
            ps.setInt(6, canvasHeight);
            ps.setString(7, canvasBackground);
            ps.setDouble(8, zoomRatio);
            ps.setInt(9, elementCount);
            ps.setString(10, versionDataJson);
            ps.setLong(11, createdBy);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void updateLatestVersion(Long exhibitionId, Integer versionNo) {
        String sql = "UPDATE exhibitions SET latest_version_no = ?, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, versionNo, exhibitionId);
    }

    public int countVersionByNo(Long exhibitionId, Integer versionNo) {
        String sql = "SELECT COUNT(1) FROM exhibition_versions WHERE exhibition_id = ? AND version_no = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, exhibitionId, versionNo);
        return count == null ? 0 : count;
    }

    public void publishExhibition(Long exhibitionId, Integer versionNo, String visibility) {
        String sql = """
                UPDATE exhibitions
                SET status = 'published',
                    visibility = ?,
                    published_version_no = ?,
                    published_at = NOW(),
                    updated_at = NOW()
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, visibility, versionNo, exhibitionId);
    }

    public void updateOwner(Long exhibitionId, Long ownerId) {
        jdbcTemplate.update("UPDATE exhibitions SET owner_id = ?, updated_at = NOW() WHERE id = ?", ownerId, exhibitionId);
    }

    public int incrementBundleRevisionIfMatch(Long exhibitionId, Integer expectedRevision) {
        String sql = """
            UPDATE exhibitions
            SET bundle_revision = bundle_revision + 1, updated_at = NOW()
            WHERE id = ? AND bundle_revision = ?
            """;
        return jdbcTemplate.update(sql, exhibitionId, expectedRevision);
    }

    public Integer getCurrentBundleRevision(Long exhibitionId) {
        String sql = "SELECT bundle_revision FROM exhibitions WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, exhibitionId);
    }

    public Long upsertDigitalHuman(Long exhibitionId,
                                   String name,
                                   String avatar2dUrl,
                                   String model3dUrl,
                                   String persona,
                                   String voiceType,
                                   String storyScript,
                                   String storyTimelineJson) {
        List<Long> existingIds = jdbcTemplate.query("""
                SELECT id FROM digital_humans WHERE exhibition_id = ? LIMIT 1
                """, (rs, rowNum) -> rs.getLong("id"), exhibitionId);
        if (existingIds.isEmpty()) {
            String sql = """
                    INSERT INTO digital_humans (
                      exhibition_id, name, avatar_2d_url, model_3d_url, persona, voice_type,
                      story_script, story_timeline, sort_no, status, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, CAST(? AS JSON), 1, 'active', NOW(), NOW())
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, exhibitionId);
                ps.setString(2, name);
                ps.setString(3, avatar2dUrl);
                ps.setString(4, model3dUrl);
                ps.setString(5, persona);
                ps.setString(6, voiceType);
                ps.setString(7, storyScript);
                ps.setString(8, storyTimelineJson);
                return ps;
            }, keyHolder);
            return keyHolder.getKey().longValue();
        }

        Long digitalHumanId = existingIds.get(0);
        String sql = """
                UPDATE digital_humans
                SET name = ?,
                    avatar_2d_url = ?,
                    model_3d_url = ?,
                    persona = ?,
                    voice_type = ?,
                    story_script = ?,
                    story_timeline = CAST(? AS JSON),
                    updated_at = NOW()
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, name, avatar2dUrl, model3dUrl, persona, voiceType, storyScript, storyTimelineJson, digitalHumanId);
        return digitalHumanId;
    }
}
