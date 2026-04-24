package com.zhixingchuangjing.platform.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

@Repository
public class DigitalHumanRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DigitalHumanRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Long findExhibitionIdByDigitalHumanId(Long digitalHumanId) {
        var list = jdbcTemplate.query("""
                SELECT exhibition_id
                FROM digital_humans
                WHERE id = ?
                LIMIT 1
                """, (rs, rowNum) -> rs.getLong("exhibition_id"), digitalHumanId);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean museumResourceExists(Long museumResourceId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM museum_resources
                WHERE id = ?
                """, Integer.class, museumResourceId);
        return count != null && count > 0;
    }

    public Long addEquipment(Long digitalHumanId,
                             String slotCode,
                             Long museumResourceId,
                             String anchorCode,
                             int displayOrder) {
        String snapshotSql = """
                SELECT JSON_OBJECT(
                  'resourceId', mr.id,
                  'title', mr.title,
                  'museumName', mr.museum_name,
                  'coverUrl', mr.cover_url,
                  'detailUrl', mr.detail_url,
                  'category', mr.category
                )
                FROM museum_resources mr
                WHERE mr.id = ?
                LIMIT 1
                """;
        String snapshotJson = jdbcTemplate.queryForObject(snapshotSql, String.class, museumResourceId);

        String sql = """
                INSERT INTO digital_human_equipments (
                  digital_human_id, slot_code, museum_resource_id, anchor_code,
                  display_order, resource_snapshot_json, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, CAST(? AS JSON), NOW(), NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, digitalHumanId);
            ps.setString(2, slotCode);
            ps.setLong(3, museumResourceId);
            ps.setString(4, anchorCode);
            ps.setInt(5, displayOrder);
            ps.setString(6, snapshotJson);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public ExhibitionResponses.DigitalHumanEquipmentResponse findEquipmentDetail(Long equipmentId) {
        String sql = """
                SELECT
                  dhe.id,
                  dhe.slot_code,
                  dhe.anchor_code,
                  dhe.display_order,
                  mr.id AS resource_id,
                  mr.title AS resource_title,
                  mr.museum_name,
                  dhe.resource_snapshot_json
                FROM digital_human_equipments dhe
                JOIN museum_resources mr ON mr.id = dhe.museum_resource_id
                WHERE dhe.id = ?
                LIMIT 1
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                return null;
            }
            return new ExhibitionResponses.DigitalHumanEquipmentResponse(
                    rs.getLong("id"),
                    rs.getString("slot_code"),
                    rs.getString("anchor_code"),
                    rs.getInt("display_order"),
                    rs.getLong("resource_id"),
                    rs.getString("resource_title"),
                    rs.getString("museum_name"),
                    parseSnapshot(rs.getString("resource_snapshot_json"))
            );
        }, equipmentId);
    }

    public void deleteEquipment(Long digitalHumanId, Long equipmentId) {
        jdbcTemplate.update("""
                DELETE FROM digital_human_equipments
                WHERE id = ? AND digital_human_id = ?
                """, equipmentId, digitalHumanId);
    }

    private Map<String, Object> parseSnapshot(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(snapshotJson, new TypeReference<>() {
            });
        } catch (IOException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50021, "数字人装备快照解析失败");
        }
    }
}
