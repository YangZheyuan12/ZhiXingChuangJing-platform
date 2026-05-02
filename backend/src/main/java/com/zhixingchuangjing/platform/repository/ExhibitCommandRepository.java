package com.zhixingchuangjing.platform.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class ExhibitCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public ExhibitCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createExhibit(Long exhibitionId, Long zoneId, String slotCode,
                              String placementMode, String placementJson,
                              String title, String subtitle, String exhibitType,
                              String coverUrl, String mediaUrl,
                              String sourceType, Long museumResourceId, Long mediaAssetId,
                              String description, String sourceInfoJson,
                              String knowledgePointsJson, Integer sortOrder) {
        String sql = """
                INSERT INTO exhibition_exhibits (
                  exhibition_id, zone_id, slot_code, placement_mode, placement_json,
                  title, subtitle, exhibit_type, cover_url, media_url,
                  source_type, museum_resource_id, media_asset_id,
                  description, source_info, knowledge_points,
                  sort_order, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, CAST(? AS JSON),
                  ?, ?, ?, ?, ?,
                  ?, ?, ?,
                  ?, CAST(? AS JSON), CAST(? AS JSON),
                  ?, 'active', NOW(), NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, exhibitionId);
            ps.setLong(2, zoneId);
            ps.setString(3, slotCode);
            ps.setString(4, placementMode != null ? placementMode : "slot");
            ps.setString(5, placementJson);
            ps.setString(6, title);
            ps.setString(7, subtitle);
            ps.setString(8, exhibitType != null ? exhibitType : "image");
            ps.setString(9, coverUrl);
            ps.setString(10, mediaUrl);
            ps.setString(11, sourceType != null ? sourceType : "upload");
            ps.setObject(12, museumResourceId);
            ps.setObject(13, mediaAssetId);
            ps.setString(14, description);
            ps.setString(15, sourceInfoJson);
            ps.setString(16, knowledgePointsJson);
            ps.setInt(17, sortOrder != null ? sortOrder : 0);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void updateExhibit(Long exhibitId, String slotCode, String placementMode,
                              String placementJson, String title, String subtitle,
                              String exhibitType, String coverUrl, String mediaUrl,
                              String sourceType, Long museumResourceId, Long mediaAssetId,
                              String description, String sourceInfoJson,
                              String knowledgePointsJson, Integer sortOrder, String status) {
        String sql = """
                UPDATE exhibition_exhibits
                SET slot_code = COALESCE(?, slot_code),
                    placement_mode = COALESCE(?, placement_mode),
                    placement_json = COALESCE(CAST(? AS JSON), placement_json),
                    title = COALESCE(?, title),
                    subtitle = COALESCE(?, subtitle),
                    exhibit_type = COALESCE(?, exhibit_type),
                    cover_url = COALESCE(?, cover_url),
                    media_url = COALESCE(?, media_url),
                    source_type = COALESCE(?, source_type),
                    museum_resource_id = COALESCE(?, museum_resource_id),
                    media_asset_id = COALESCE(?, media_asset_id),
                    description = COALESCE(?, description),
                    source_info = COALESCE(CAST(? AS JSON), source_info),
                    knowledge_points = COALESCE(CAST(? AS JSON), knowledge_points),
                    sort_order = COALESCE(?, sort_order),
                    status = COALESCE(?, status),
                    updated_at = NOW()
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, slotCode, placementMode, placementJson,
                title, subtitle, exhibitType, coverUrl, mediaUrl,
                sourceType, museumResourceId, mediaAssetId,
                description, sourceInfoJson, knowledgePointsJson,
                sortOrder, status, exhibitId);
    }

    public Long upsertNarration(Long exhibitId, Long narrationId,
                                String narrationType, String content, String audioUrl,
                                String voiceType, Integer durationSeconds, Integer sortOrder) {
        if (narrationId != null && narrationId > 0) {
            String sql = """
                    UPDATE exhibit_narrations
                    SET narration_type = COALESCE(?, narration_type),
                        content = COALESCE(?, content),
                        audio_url = COALESCE(?, audio_url),
                        voice_type = COALESCE(?, voice_type),
                        duration_seconds = COALESCE(?, duration_seconds),
                        sort_order = COALESCE(?, sort_order),
                        updated_at = NOW()
                    WHERE id = ? AND exhibit_id = ?
                    """;
            jdbcTemplate.update(sql, narrationType, content, audioUrl, voiceType,
                    durationSeconds, sortOrder, narrationId, exhibitId);
            return narrationId;
        }
        String sql = """
                INSERT INTO exhibit_narrations (
                  exhibit_id, narration_type, content, audio_url, voice_type,
                  duration_seconds, sort_order, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, exhibitId);
            ps.setString(2, narrationType != null ? narrationType : "text");
            ps.setString(3, content);
            ps.setString(4, audioUrl);
            ps.setString(5, voiceType);
            ps.setObject(6, durationSeconds);
            ps.setInt(7, sortOrder != null ? sortOrder : 0);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Long upsertInteraction(Long exhibitId, Long interactionId,
                                  String interactionType, String questionText,
                                  String optionsJson, String correctAnswer,
                                  String explanation, Integer sortOrder) {
        if (interactionId != null && interactionId > 0) {
            String sql = """
                    UPDATE exhibit_interactions
                    SET interaction_type = COALESCE(?, interaction_type),
                        question_text = COALESCE(?, question_text),
                        options_json = COALESCE(CAST(? AS JSON), options_json),
                        correct_answer = COALESCE(?, correct_answer),
                        explanation = COALESCE(?, explanation),
                        sort_order = COALESCE(?, sort_order)
                    WHERE id = ? AND exhibit_id = ?
                    """;
            jdbcTemplate.update(sql, interactionType, questionText, optionsJson,
                    correctAnswer, explanation, sortOrder, interactionId, exhibitId);
            return interactionId;
        }
        String sql = """
                INSERT INTO exhibit_interactions (
                  exhibit_id, interaction_type, question_text, options_json,
                  correct_answer, explanation, sort_order, created_at
                ) VALUES (?, ?, ?, CAST(? AS JSON), ?, ?, ?, NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, exhibitId);
            ps.setString(2, interactionType);
            ps.setString(3, questionText);
            ps.setString(4, optionsJson);
            ps.setString(5, correctAnswer);
            ps.setString(6, explanation);
            ps.setInt(7, sortOrder != null ? sortOrder : 0);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void deleteExhibit(Long exhibitId) {
        jdbcTemplate.update("DELETE FROM exhibit_interactions WHERE exhibit_id = ?", exhibitId);
        jdbcTemplate.update("DELETE FROM exhibit_narrations WHERE exhibit_id = ?", exhibitId);
        jdbcTemplate.update("DELETE FROM exhibition_exhibits WHERE id = ?", exhibitId);
    }

    public void deleteNarration(Long narrationId) {
        jdbcTemplate.update("DELETE FROM exhibit_narrations WHERE id = ?", narrationId);
    }

    public void deleteInteraction(Long interactionId) {
        jdbcTemplate.update("DELETE FROM exhibit_interactions WHERE id = ?", interactionId);
    }
}
