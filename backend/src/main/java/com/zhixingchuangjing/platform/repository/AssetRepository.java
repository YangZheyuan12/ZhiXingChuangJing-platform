package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.AssetResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AssetRepository {

    private final JdbcTemplate jdbcTemplate;

    public AssetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createAsset(Long ownerId,
                            String assetType,
                            String sourceType,
                            String fileName,
                            String fileUrl,
                            String fileExt,
                            String mimeType,
                            Long fileSize) {
        String sql = """
                INSERT INTO media_assets (
                  owner_id, asset_type, source_type, file_name, file_url, file_ext,
                  mime_type, file_size, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, ownerId);
            ps.setString(2, assetType);
            ps.setString(3, sourceType);
            ps.setString(4, fileName);
            ps.setString(5, fileUrl);
            ps.setString(6, fileExt);
            ps.setString(7, mimeType);
            ps.setObject(8, fileSize);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<AssetResponses.AssetResponse> findAssets(Long ownerId,
                                                         String assetType,
                                                         String sourceType,
                                                         int offset,
                                                         int pageSize) {
        QuerySpec spec = buildAssetQuery(ownerId, assetType, sourceType, false);
        List<Object> args = new ArrayList<>(spec.args());
        args.add(offset);
        args.add(pageSize);
        return jdbcTemplate.query(spec.sql() + " ORDER BY created_at DESC LIMIT ?, ?", (rs, rowNum) ->
                new AssetResponses.AssetResponse(
                        rs.getLong("id"),
                        rs.getString("file_name"),
                        rs.getString("file_url"),
                        rs.getString("mime_type"),
                        rs.getObject("file_size", Long.class),
                        rs.getString("asset_type"),
                        rs.getString("source_type"),
                        rs.getObject("created_at", LocalDateTime.class)
                ), args.toArray());
    }

    public long countAssets(Long ownerId, String assetType, String sourceType) {
        QuerySpec spec = buildAssetQuery(ownerId, assetType, sourceType, true);
        Long count = jdbcTemplate.queryForObject(spec.sql(), Long.class, spec.args().toArray());
        return count == null ? 0L : count;
    }

    private QuerySpec buildAssetQuery(Long ownerId, String assetType, String sourceType, boolean countOnly) {
        StringBuilder sql = new StringBuilder(countOnly ? "SELECT COUNT(1) " : """
                SELECT
                  id, file_name, file_url, mime_type, file_size, asset_type, source_type, created_at
                """);
        sql.append("FROM media_assets WHERE owner_id = ? ");
        List<Object> args = new ArrayList<>();
        args.add(ownerId);
        if (assetType != null && !assetType.isBlank()) {
            sql.append(" AND asset_type = ? ");
            args.add(assetType);
        }
        if (sourceType != null && !sourceType.isBlank()) {
            sql.append(" AND source_type = ? ");
            args.add(sourceType);
        } else {
            sql.append(" AND source_type <> ? ");
            args.add("profile");
        }
        return new QuerySpec(sql.toString(), args);
    }

    private record QuerySpec(String sql, List<Object> args) {
    }
}
