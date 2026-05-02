package com.zhixingchuangjing.platform.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixingchuangjing.platform.model.response.ExhibitResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ExhibitQueryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ExhibitQueryRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<ExhibitResponses.ExhibitSummaryResponse> listByZone(Long zoneId) {
        String sql = """
                SELECT id, zone_id, slot_code, title, exhibit_type, cover_url, sort_order, status
                FROM exhibition_exhibits
                WHERE zone_id = ? AND status = 'active'
                ORDER BY sort_order, id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ExhibitResponses.ExhibitSummaryResponse(
                rs.getLong("id"),
                rs.getLong("zone_id"),
                rs.getString("slot_code"),
                rs.getString("title"),
                rs.getString("exhibit_type"),
                rs.getString("cover_url"),
                rs.getInt("sort_order"),
                rs.getString("status")
        ), zoneId);
    }

    public ExhibitResponses.ExhibitResponse findById(Long exhibitId) {
        String sql = """
                SELECT id, exhibition_id, zone_id, slot_code, placement_mode, placement_json,
                       title, subtitle, exhibit_type, cover_url, media_url,
                       source_type, museum_resource_id, media_asset_id,
                       description, source_info, knowledge_points,
                       sort_order, status, created_at, updated_at
                FROM exhibition_exhibits
                WHERE id = ?
                """;
        List<ExhibitResponses.ExhibitResponse> list = jdbcTemplate.query(sql, (rs, rowNum) -> new ExhibitResponses.ExhibitResponse(
                rs.getLong("id"),
                rs.getLong("exhibition_id"),
                rs.getLong("zone_id"),
                rs.getString("slot_code"),
                rs.getString("placement_mode"),
                rs.getString("placement_json"),
                rs.getString("title"),
                rs.getString("subtitle"),
                rs.getString("exhibit_type"),
                rs.getString("cover_url"),
                rs.getString("media_url"),
                rs.getString("source_type"),
                rs.getObject("museum_resource_id") != null ? rs.getLong("museum_resource_id") : null,
                rs.getObject("media_asset_id") != null ? rs.getLong("media_asset_id") : null,
                rs.getString("description"),
                rs.getString("source_info"),
                parseJsonList(rs.getString("knowledge_points")),
                rs.getInt("sort_order"),
                rs.getString("status"),
                null, null,
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("updated_at", LocalDateTime.class)
        ), exhibitId);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long findExhibitionIdByExhibitId(Long exhibitId) {
        String sql = "SELECT exhibition_id FROM exhibition_exhibits WHERE id = ?";
        List<Long> ids = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("exhibition_id"), exhibitId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    public List<ExhibitResponses.NarrationResponse> listNarrations(Long exhibitId) {
        String sql = """
                SELECT id, exhibit_id, narration_type, content, audio_url, voice_type,
                       duration_seconds, sort_order, created_at
                FROM exhibit_narrations
                WHERE exhibit_id = ?
                ORDER BY sort_order, id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ExhibitResponses.NarrationResponse(
                rs.getLong("id"),
                rs.getLong("exhibit_id"),
                rs.getString("narration_type"),
                rs.getString("content"),
                rs.getString("audio_url"),
                rs.getString("voice_type"),
                rs.getObject("duration_seconds") != null ? rs.getInt("duration_seconds") : null,
                rs.getInt("sort_order"),
                rs.getObject("created_at", LocalDateTime.class)
        ), exhibitId);
    }

    public List<ExhibitResponses.InteractionResponse> listInteractions(Long exhibitId) {
        String sql = """
                SELECT id, exhibit_id, interaction_type, question_text, options_json,
                       correct_answer, explanation, sort_order, created_at
                FROM exhibit_interactions
                WHERE exhibit_id = ?
                ORDER BY sort_order, id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ExhibitResponses.InteractionResponse(
                rs.getLong("id"),
                rs.getLong("exhibit_id"),
                rs.getString("interaction_type"),
                rs.getString("question_text"),
                rs.getString("options_json"),
                rs.getString("correct_answer"),
                rs.getString("explanation"),
                rs.getInt("sort_order"),
                rs.getObject("created_at", LocalDateTime.class)
        ), exhibitId);
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
