package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.EditorBundleResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HotspotQueryRepository {
    private final JdbcTemplate jdbcTemplate;

    public HotspotQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EditorBundleResponses.HotspotResponse> listByExhibition(Long exhibitionId) {
        String sql = """
            SELECT h.id, h.zone_id, h.target_zone_id, h.hotspot_type, h.label, h.icon,
                   h.x_percent, h.y_percent, h.w_percent, h.h_percent,
                   h.style_json, h.action_config, h.sort_order
            FROM zone_hotspots h
            JOIN exhibition_zones z ON z.id = h.zone_id
            WHERE z.exhibition_id = ?
            ORDER BY h.zone_id, h.sort_order, h.id
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new EditorBundleResponses.HotspotResponse(
            rs.getLong("id"), rs.getLong("zone_id"),
            rs.getObject("target_zone_id") != null ? rs.getLong("target_zone_id") : null,
            rs.getString("hotspot_type"), rs.getString("label"), rs.getString("icon"),
            rs.getDouble("x_percent"), rs.getDouble("y_percent"),
            rs.getObject("w_percent") != null ? rs.getDouble("w_percent") : null,
            rs.getObject("h_percent") != null ? rs.getDouble("h_percent") : null,
            rs.getString("style_json"), rs.getString("action_config"),
            rs.getInt("sort_order")
        ), exhibitionId);
    }
}
