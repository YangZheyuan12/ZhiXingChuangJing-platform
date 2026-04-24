package com.zhixingchuangjing.platform.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.model.response.MuseumResponses;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class MuseumRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public MuseumRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<MuseumResponses.MuseumResourceResponse> findResources(String providerCode,
                                                                      String category,
                                                                      String keyword,
                                                                      int offset,
                                                                      int pageSize) {
        QuerySpec spec = buildResourceQuery(providerCode, category, keyword, false);
        List<Object> args = new ArrayList<>(spec.args());
        args.add(offset);
        args.add(pageSize);
        return jdbcTemplate.query(spec.sql() + " ORDER BY mr.updated_at DESC LIMIT ?, ?", (rs, rowNum) ->
                new MuseumResponses.MuseumResourceResponse(
                        rs.getLong("id"),
                        rs.getString("provider_code"),
                        rs.getString("title"),
                        rs.getString("category"),
                        rs.getString("museum_name"),
                        rs.getString("cover_url"),
                        rs.getString("detail_url"),
                        rs.getString("description"),
                        parseMetadata(rs.getString("metadata_json"))
                ), args.toArray());
    }

    public long countResources(String providerCode, String category, String keyword) {
        QuerySpec spec = buildResourceQuery(providerCode, category, keyword, true);
        Long count = jdbcTemplate.queryForObject(spec.sql(), Long.class, spec.args().toArray());
        return count == null ? 0L : count;
    }

    public MuseumResponses.MuseumResourceResponse findResourceDetail(Long resourceId) {
        String sql = """
                SELECT
                  mr.id,
                  mp.provider_code,
                  mr.title,
                  mr.category,
                  mr.museum_name,
                  mr.cover_url,
                  mr.detail_url,
                  mr.description,
                  mr.metadata_json
                FROM museum_resources mr
                JOIN museum_providers mp ON mp.id = mr.provider_id
                WHERE mr.id = ?
                LIMIT 1
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                return null;
            }
            return new MuseumResponses.MuseumResourceResponse(
                    rs.getLong("id"),
                    rs.getString("provider_code"),
                    rs.getString("title"),
                    rs.getString("category"),
                    rs.getString("museum_name"),
                    rs.getString("cover_url"),
                    rs.getString("detail_url"),
                    rs.getString("description"),
                    parseMetadata(rs.getString("metadata_json"))
            );
        }, resourceId);
    }

    public Long findProviderId(String providerCode) {
        var ids = jdbcTemplate.query("""
                SELECT id
                FROM museum_providers
                WHERE provider_code = ?
                LIMIT 1
                """, (rs, rowNum) -> rs.getLong("id"), providerCode);
        return ids.isEmpty() ? null : ids.get(0);
    }

    public void upsertResource(Long providerId,
                               String externalId,
                               String resourceType,
                               String category,
                               String title,
                               String museumName,
                               String coverUrl,
                               String detailUrl,
                               String description,
                               String metadataJson) {
        String sql = """
                INSERT INTO museum_resources (
                  provider_id, external_id, resource_type, category, title, museum_name,
                  cover_url, detail_url, description, metadata_json, raw_payload,
                  cache_status, synced_at, cache_expire_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS JSON), CAST(? AS JSON), 'fresh', NOW(), ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                  title = VALUES(title),
                  museum_name = VALUES(museum_name),
                  cover_url = VALUES(cover_url),
                  detail_url = VALUES(detail_url),
                  description = VALUES(description),
                  metadata_json = VALUES(metadata_json),
                  raw_payload = VALUES(raw_payload),
                  cache_status = 'fresh',
                  synced_at = NOW(),
                  cache_expire_at = VALUES(cache_expire_at),
                  updated_at = NOW()
                """;
        jdbcTemplate.update(sql, providerId, externalId, resourceType, category, title, museumName,
                coverUrl, detailUrl, description, metadataJson, metadataJson, LocalDateTime.now().plusDays(7));
    }

    private QuerySpec buildResourceQuery(String providerCode, String category, String keyword, boolean countOnly) {
        StringBuilder sql = new StringBuilder(countOnly ? "SELECT COUNT(1) " : """
                SELECT
                  mr.id,
                  mp.provider_code,
                  mr.title,
                  mr.category,
                  mr.museum_name,
                  mr.cover_url,
                  mr.detail_url,
                  mr.description,
                  mr.metadata_json
                """);
        sql.append("""
                FROM museum_resources mr
                JOIN museum_providers mp ON mp.id = mr.provider_id
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        if (providerCode != null && !providerCode.isBlank()) {
            sql.append(" AND mp.provider_code = ? ");
            args.add(providerCode);
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND mr.category = ? ");
            args.add(category);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (mr.title LIKE ? OR mr.description LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        return new QuerySpec(sql.toString(), args);
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<>() {
            });
        } catch (IOException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50022, "文博资源元数据解析失败");
        }
    }

    private record QuerySpec(String sql, List<Object> args) {
    }
}
