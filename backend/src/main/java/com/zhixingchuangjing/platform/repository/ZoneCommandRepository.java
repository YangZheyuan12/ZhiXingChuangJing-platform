package com.zhixingchuangjing.platform.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;

@Repository
public class ZoneCommandRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ZoneCommandRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Long createZone(Long exhibitionId, String zoneCode, String zoneType, String title,
                           String subtitle, String description, Integer sortOrder,
                           String backgroundUrl, String transitionIn) {
        String sql = """
                INSERT INTO exhibition_zones (
                  exhibition_id, zone_code, zone_type, title, subtitle, description,
                  sort_order, background_url, transition_in, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'active', NOW(), NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, exhibitionId);
            ps.setString(2, zoneCode);
            ps.setString(3, zoneType);
            ps.setString(4, title);
            ps.setString(5, subtitle);
            ps.setString(6, description);
            ps.setInt(7, sortOrder != null ? sortOrder : 0);
            ps.setString(8, backgroundUrl);
            ps.setString(9, transitionIn != null ? transitionIn : "fade");
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void updateZone(Long zoneId, String title, String subtitle, String description,
                           String backgroundUrl, String backgroundStyleJson,
                           String layoutConfigJson, String narrationText,
                           String transitionIn, Integer sortOrder) {
        String sql = """
                UPDATE exhibition_zones
                SET title = COALESCE(?, title),
                    subtitle = COALESCE(?, subtitle),
                    description = COALESCE(?, description),
                    background_url = COALESCE(?, background_url),
                    background_style = COALESCE(CAST(? AS JSON), background_style),
                    layout_config = COALESCE(CAST(? AS JSON), layout_config),
                    narration_text = COALESCE(?, narration_text),
                    transition_in = COALESCE(?, transition_in),
                    sort_order = COALESCE(?, sort_order),
                    updated_at = NOW()
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, title, subtitle, description, backgroundUrl,
                backgroundStyleJson, layoutConfigJson, narrationText, transitionIn,
                sortOrder, zoneId);
    }

    public void updateCanvasData(Long zoneId, String canvasDataJson) {
        String sql = "UPDATE exhibition_zones SET canvas_data = CAST(? AS JSON), updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, canvasDataJson, zoneId);
    }

    public void assignZone(Long zoneId, Long userId) {
        String sql = "UPDATE exhibition_zones SET assigned_user_id = ?, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, userId, zoneId);
    }

    public int lockZone(Long zoneId, Long userId) {
        String sql = """
                UPDATE exhibition_zones
                SET locked_by = ?, locked_at = NOW(), updated_at = NOW()
                WHERE id = ? AND (locked_by IS NULL OR locked_by = ?)
                """;
        return jdbcTemplate.update(sql, userId, zoneId, userId);
    }

    public int unlockZone(Long zoneId, Long userId) {
        String sql = """
                UPDATE exhibition_zones
                SET locked_by = NULL, locked_at = NULL, updated_at = NOW()
                WHERE id = ? AND locked_by = ?
                """;
        return jdbcTemplate.update(sql, zoneId, userId);
    }

    public void deleteZone(Long zoneId) {
        // 级联删除展品及其从属讲解词和互动题
        jdbcTemplate.update(
                "DELETE FROM exhibit_interactions WHERE exhibit_id IN (SELECT id FROM exhibition_exhibits WHERE zone_id = ?)",
                zoneId);
        jdbcTemplate.update(
                "DELETE FROM exhibit_narrations WHERE exhibit_id IN (SELECT id FROM exhibition_exhibits WHERE zone_id = ?)",
                zoneId);
        jdbcTemplate.update("DELETE FROM exhibition_exhibits WHERE zone_id = ?", zoneId);
        jdbcTemplate.update("DELETE FROM zone_hotspots WHERE zone_id = ?", zoneId);
        jdbcTemplate.update("DELETE FROM zone_digital_human_placements WHERE zone_id = ?", zoneId);
        jdbcTemplate.update("DELETE FROM exhibition_zones WHERE id = ?", zoneId);
    }

    /**
     * 设置指定展区的数字人摆放（若存在则更新，否则新增）。
     * 传入的 x/y/scale/facing 为 null 时会退回数据库默认值。
     */
    public void upsertZoneDigitalHumanPlacement(Long zoneId,
                                                Long digitalHumanId,
                                                Double xPercent,
                                                Double yPercent,
                                                Double scale,
                                                String facing) {
        String sql = """
                INSERT INTO zone_digital_human_placements
                  (zone_id, digital_human_id, x_percent, y_percent, scale, facing, created_at, updated_at)
                VALUES (?, ?,
                  COALESCE(?, 80.000),
                  COALESCE(?, 70.000),
                  COALESCE(?, 1.000),
                  COALESCE(?, 'left'),
                  NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                  digital_human_id = VALUES(digital_human_id),
                  x_percent        = COALESCE(VALUES(x_percent), x_percent),
                  y_percent        = COALESCE(VALUES(y_percent), y_percent),
                  scale            = COALESCE(VALUES(scale), scale),
                  facing           = COALESCE(VALUES(facing), facing),
                  updated_at       = NOW()
                """;
        jdbcTemplate.update(sql, zoneId, digitalHumanId, xPercent, yPercent, scale, facing);
    }

    public int deleteZoneDigitalHumanPlacement(Long zoneId) {
        return jdbcTemplate.update(
                "DELETE FROM zone_digital_human_placements WHERE zone_id = ?", zoneId);
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }
}
