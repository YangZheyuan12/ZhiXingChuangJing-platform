package com.zhixingchuangjing.platform.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HotspotCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public HotspotCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createHotspot(Long zoneId, Long targetZoneId, String hotspotType,
                              String label, String icon,
                              Double xPercent, Double yPercent,
                              Double wPercent, Double hPercent,
                              Integer sortOrder) {
        String sql = """
                INSERT INTO zone_hotspots (
                  zone_id, target_zone_id, hotspot_type, label, icon,
                  x_percent, y_percent, w_percent, h_percent, sort_order,
                  created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;
        jdbcTemplate.update(sql, zoneId, targetZoneId, hotspotType, label, icon,
                xPercent, yPercent, wPercent, hPercent, sortOrder != null ? sortOrder : 0);
    }
}
