package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.ZoneResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ZoneQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public ZoneQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ZoneResponses.ZoneSummaryResponse> listByExhibition(Long exhibitionId) {
        String sql = """
                SELECT id, zone_code, zone_type, title, sort_order, background_url, transition_in,
                       assigned_user_id, locked_by, locked_at, status
                FROM exhibition_zones
                WHERE exhibition_id = ?
                ORDER BY sort_order, id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ZoneResponses.ZoneSummaryResponse(
                rs.getLong("id"),
                rs.getString("zone_code"),
                rs.getString("zone_type"),
                rs.getString("title"),
                rs.getInt("sort_order"),
                rs.getString("background_url"),
                rs.getString("transition_in"),
                rs.getObject("assigned_user_id") != null ? rs.getLong("assigned_user_id") : null,
                rs.getObject("locked_by") != null ? rs.getLong("locked_by") : null,
                rs.getObject("locked_at", LocalDateTime.class),
                rs.getString("status")
        ), exhibitionId);
    }

    public ZoneResponses.ZoneResponse findById(Long zoneId) {
        String sql = """
                SELECT id, exhibition_id, zone_code, zone_type, title, subtitle, description,
                       background_url, background_style, layout_config, transition_in,
                       narration_text, narration_audio, canvas_data, sort_order,
                       assigned_user_id, locked_by, locked_at, status, created_at, updated_at
                FROM exhibition_zones
                WHERE id = ?
                """;
        List<ZoneResponses.ZoneResponse> list = jdbcTemplate.query(sql, (rs, rowNum) -> new ZoneResponses.ZoneResponse(
                rs.getLong("id"),
                rs.getLong("exhibition_id"),
                rs.getString("zone_code"),
                rs.getString("zone_type"),
                rs.getString("title"),
                rs.getString("subtitle"),
                rs.getString("description"),
                rs.getString("background_url"),
                rs.getString("background_style"),
                rs.getString("layout_config"),
                rs.getString("transition_in"),
                rs.getString("narration_text"),
                rs.getString("narration_audio"),
                rs.getString("canvas_data"),
                rs.getInt("sort_order"),
                rs.getObject("assigned_user_id") != null ? rs.getLong("assigned_user_id") : null,
                rs.getObject("locked_by") != null ? rs.getLong("locked_by") : null,
                rs.getObject("locked_at", LocalDateTime.class),
                rs.getString("status"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("updated_at", LocalDateTime.class)
        ), zoneId);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long findExhibitionIdByZoneId(Long zoneId) {
        String sql = "SELECT exhibition_id FROM exhibition_zones WHERE id = ?";
        List<Long> ids = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("exhibition_id"), zoneId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    /**
     * 查询单个展区的数字人摆放（关联数字人基础信息），不存在则返回 null。
     */
    public ZoneResponses.ZoneDigitalHumanPlacementResponse findZoneDigitalHumanPlacement(Long zoneId) {
        String sql = """
                SELECT
                  p.zone_id, p.digital_human_id,
                  p.x_percent, p.y_percent, p.scale, p.facing,
                  dh.name, dh.avatar_2d_url, dh.model_3d_url,
                  dh.persona, dh.voice_type
                FROM zone_digital_human_placements p
                JOIN digital_humans dh ON dh.id = p.digital_human_id
                WHERE p.zone_id = ?
                """;
        List<ZoneResponses.ZoneDigitalHumanPlacementResponse> list =
                jdbcTemplate.query(sql, ZONE_DIGITAL_HUMAN_PLACEMENT_ROW_MAPPER, zoneId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询指定展厅下所有展区的数字人摆放。用于 editor-bundle / viewer 一次性下发。
     */
    public List<ZoneResponses.ZoneDigitalHumanPlacementResponse> findZoneDigitalHumansByExhibition(Long exhibitionId) {
        String sql = """
                SELECT
                  p.zone_id, p.digital_human_id,
                  p.x_percent, p.y_percent, p.scale, p.facing,
                  dh.name, dh.avatar_2d_url, dh.model_3d_url,
                  dh.persona, dh.voice_type
                FROM zone_digital_human_placements p
                JOIN digital_humans dh ON dh.id = p.digital_human_id
                JOIN exhibition_zones z ON z.id = p.zone_id
                WHERE z.exhibition_id = ?
                ORDER BY z.sort_order, z.id
                """;
        return jdbcTemplate.query(sql, ZONE_DIGITAL_HUMAN_PLACEMENT_ROW_MAPPER, exhibitionId);
    }

    private static final org.springframework.jdbc.core.RowMapper<ZoneResponses.ZoneDigitalHumanPlacementResponse>
            ZONE_DIGITAL_HUMAN_PLACEMENT_ROW_MAPPER = (rs, rowNum) -> new ZoneResponses.ZoneDigitalHumanPlacementResponse(
                    rs.getLong("zone_id"),
                    rs.getLong("digital_human_id"),
                    rs.getString("name"),
                    rs.getString("avatar_2d_url"),
                    rs.getString("model_3d_url"),
                    rs.getString("persona"),
                    rs.getString("voice_type"),
                    rs.getDouble("x_percent"),
                    rs.getDouble("y_percent"),
                    rs.getDouble("scale"),
                    rs.getString("facing")
            );
}
