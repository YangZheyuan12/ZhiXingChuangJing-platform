package com.zhixingchuangjing.platform.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;

@Repository
public class HotspotCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public HotspotCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 创建热点并返回新 id。
     * styleJson / actionConfig 为可选 JSON 字符串。
     */
    public Long createHotspot(Long zoneId, Long targetZoneId, String hotspotType,
                              String label, String icon,
                              Double xPercent, Double yPercent,
                              Double wPercent, Double hPercent,
                              String styleJson, String actionConfig,
                              Integer sortOrder) {
        String sql = """
                INSERT INTO zone_hotspots (
                  zone_id, target_zone_id, hotspot_type, label, icon,
                  x_percent, y_percent, w_percent, h_percent,
                  style_json, action_config, sort_order,
                  created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, zoneId);
            if (targetZoneId == null) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, targetZoneId);
            }
            ps.setString(3, hotspotType);
            ps.setString(4, label);
            ps.setString(5, icon);
            // x/y NOT NULL 无默认值 —— 必须由调用方保证非空
            ps.setDouble(6, xPercent != null ? xPercent : 0.0);
            ps.setDouble(7, yPercent != null ? yPercent : 0.0);
            // w/h NOT NULL 默认 8.00 —— null 时填默认
            ps.setDouble(8, wPercent != null ? wPercent : 8.0);
            ps.setDouble(9, hPercent != null ? hPercent : 8.0);
            ps.setString(10, styleJson);
            ps.setString(11, actionConfig);
            ps.setInt(12, sortOrder != null ? sortOrder : 0);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to retrieve generated hotspot id");
        }
        return key.longValue();
    }

    /**
     * 更新热点；所有参数非空则覆盖，null 视为保持原值（COALESCE 策略）。
     * 注意 style_json / action_config 传 null 保持原值；如需清空，请传空字符串 ""。
     */
    public int updateHotspot(Long id,
                             Long targetZoneId, String hotspotType,
                             String label, String icon,
                             Double xPercent, Double yPercent,
                             Double wPercent, Double hPercent,
                             String styleJson, String actionConfig,
                             Integer sortOrder) {
        String sql = """
                UPDATE zone_hotspots
                SET target_zone_id = COALESCE(?, target_zone_id),
                    hotspot_type   = COALESCE(?, hotspot_type),
                    label          = COALESCE(?, label),
                    icon           = COALESCE(?, icon),
                    x_percent      = COALESCE(?, x_percent),
                    y_percent      = COALESCE(?, y_percent),
                    w_percent      = COALESCE(?, w_percent),
                    h_percent      = COALESCE(?, h_percent),
                    style_json     = COALESCE(?, style_json),
                    action_config  = COALESCE(?, action_config),
                    sort_order     = COALESCE(?, sort_order)
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql,
                targetZoneId, hotspotType, label, icon,
                xPercent, yPercent, wPercent, hPercent,
                styleJson, actionConfig, sortOrder,
                id);
    }

    public int deleteHotspot(Long id) {
        return jdbcTemplate.update("DELETE FROM zone_hotspots WHERE id = ?", id);
    }

    public int deleteByZone(Long zoneId) {
        return jdbcTemplate.update("DELETE FROM zone_hotspots WHERE zone_id = ?", zoneId);
    }

    private static void setNullableDouble(PreparedStatement ps, int index, Double value) throws java.sql.SQLException {
        if (value == null) {
            ps.setNull(index, Types.DOUBLE);
        } else {
            ps.setDouble(index, value);
        }
    }
}
