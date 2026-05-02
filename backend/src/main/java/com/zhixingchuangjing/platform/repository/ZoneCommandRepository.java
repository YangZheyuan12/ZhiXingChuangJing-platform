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
        jdbcTemplate.update("DELETE FROM zone_hotspots WHERE zone_id = ?", zoneId);
        jdbcTemplate.update("DELETE FROM exhibition_zones WHERE id = ?", zoneId);
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
