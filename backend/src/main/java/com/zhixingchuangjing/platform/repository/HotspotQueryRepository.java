package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.EditorBundleResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HotspotQueryRepository {
    private static final String BASE_COLUMNS = """
            h.id, h.zone_id, h.target_zone_id, h.hotspot_type, h.label, h.icon,
            h.x_percent, h.y_percent, h.w_percent, h.h_percent,
            h.style_json, h.action_config, h.sort_order
            """;

    private static final RowMapper<EditorBundleResponses.HotspotResponse> ROW_MAPPER = (rs, rowNum) ->
            new EditorBundleResponses.HotspotResponse(
                    rs.getLong("id"), rs.getLong("zone_id"),
                    rs.getObject("target_zone_id") != null ? rs.getLong("target_zone_id") : null,
                    rs.getString("hotspot_type"), rs.getString("label"), rs.getString("icon"),
                    rs.getDouble("x_percent"), rs.getDouble("y_percent"),
                    rs.getObject("w_percent") != null ? rs.getDouble("w_percent") : null,
                    rs.getObject("h_percent") != null ? rs.getDouble("h_percent") : null,
                    rs.getString("style_json"), rs.getString("action_config"),
                    rs.getInt("sort_order")
            );

    private final JdbcTemplate jdbcTemplate;

    public HotspotQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EditorBundleResponses.HotspotResponse> listByExhibition(Long exhibitionId) {
        String sql = "SELECT " + BASE_COLUMNS + """
                FROM zone_hotspots h
                JOIN exhibition_zones z ON z.id = h.zone_id
                WHERE z.exhibition_id = ?
                ORDER BY h.zone_id, h.sort_order, h.id
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, exhibitionId);
    }

    public List<EditorBundleResponses.HotspotResponse> listByZone(Long zoneId) {
        String sql = "SELECT " + BASE_COLUMNS + """
                FROM zone_hotspots h
                WHERE h.zone_id = ?
                ORDER BY h.sort_order, h.id
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, zoneId);
    }

    public EditorBundleResponses.HotspotResponse findById(Long id) {
        String sql = "SELECT " + BASE_COLUMNS + " FROM zone_hotspots h WHERE h.id = ?";
        List<EditorBundleResponses.HotspotResponse> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 返回热点所属展厅 id；如果热点不存在返回 null。用于 Controller 校验热点与路径 exhibitionId 是否一致。
     */
    public Long findExhibitionIdByHotspotId(Long hotspotId) {
        String sql = """
                SELECT z.exhibition_id FROM zone_hotspots h
                JOIN exhibition_zones z ON z.id = h.zone_id
                WHERE h.id = ?
                """;
        List<Long> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("exhibition_id"), hotspotId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 校验给定 zoneId 属于 exhibitionId。
     */
    public boolean zoneBelongsToExhibition(Long zoneId, Long exhibitionId) {
        String sql = "SELECT COUNT(1) FROM exhibition_zones WHERE id = ? AND exhibition_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, zoneId, exhibitionId);
        return count != null && count > 0;
    }
}
