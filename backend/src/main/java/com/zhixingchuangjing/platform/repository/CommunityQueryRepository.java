package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CommunityQueryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ExhibitionQueryRepository exhibitionQueryRepository;

    public CommunityQueryRepository(JdbcTemplate jdbcTemplate, ExhibitionQueryRepository exhibitionQueryRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.exhibitionQueryRepository = exhibitionQueryRepository;
    }

    public List<ExhibitionResponses.ExhibitionSummaryResponse> findPagedCommunityExhibitions(String keyword,
                                                                                            String grade,
                                                                                            String theme,
                                                                                            String tag,
                                                                                            String sortBy,
                                                                                            int offset,
                                                                                            int pageSize) {
        QuerySpec spec = buildCommunityQuery(keyword, grade, theme, tag, sortBy, false);
        List<Object> args = new ArrayList<>(spec.args());
        args.add(offset);
        args.add(pageSize);
        return jdbcTemplate.query(spec.sql() + " LIMIT ?, ?", (rs, rowNum) ->
                new ExhibitionResponses.ExhibitionSummaryResponse(
                        rs.getLong("id"),
                        rs.getObject("task_id", Long.class),
                        rs.getString("title"),
                        rs.getString("cover_url"),
                        rs.getString("summary"),
                        rs.getString("status"),
                        rs.getString("visibility"),
                        rs.getString("group_name"),
                        rs.getLong("owner_id"),
                        new com.zhixingchuangjing.platform.model.response.CommonResponses.SimpleUser(
                                rs.getLong("author_id"),
                                rs.getString("author_role"),
                                rs.getString("author_name"),
                                rs.getString("author_nickname"),
                                rs.getString("author_avatar_url")
                        ),
                        rs.getInt("latest_version_no"),
                        rs.getInt("published_version_no"),
                        new ExhibitionResponses.ExhibitionStatsResponse(
                                rs.getInt("view_count"),
                                rs.getInt("like_count"),
                                rs.getInt("favorite_count"),
                                rs.getInt("comment_count")
                        ),
                        exhibitionQueryRepository.findExhibitionTags(rs.getLong("id"))
                ), args.toArray());
    }

    public long countCommunityExhibitions(String keyword, String grade, String theme, String tag, String sortBy) {
        QuerySpec spec = buildCommunityQuery(keyword, grade, theme, tag, sortBy, true);
        Long count = jdbcTemplate.queryForObject(spec.sql(), Long.class, spec.args().toArray());
        return count == null ? 0L : count;
    }

    private QuerySpec buildCommunityQuery(String keyword, String grade, String theme, String tag, String sortBy, boolean countOnly) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        if (countOnly) {
            sql.append("SELECT COUNT(DISTINCT e.id) ");
        } else {
            sql.append("""
                    SELECT DISTINCT
                      e.id,
                      e.task_id,
                      e.title,
                      e.cover_url,
                      e.summary,
                      e.status,
                      e.visibility,
                      e.group_name,
                      e.owner_id,
                      e.published_at,
                      e.latest_version_no,
                      e.published_version_no,
                      e.view_count,
                      e.like_count,
                      e.favorite_count,
                      e.comment_count,
                      u.id AS author_id,
                      u.role AS author_role,
                      u.real_name AS author_name,
                      u.nickname AS author_nickname,
                      u.avatar_url AS author_avatar_url
                    """);
        }
        sql.append("""
                FROM exhibitions e
                JOIN users u ON u.id = e.owner_id
                LEFT JOIN task_target_classes ttc ON ttc.task_id = e.task_id
                LEFT JOIN classes c ON c.id = ttc.class_id
                LEFT JOIN exhibition_tag_relations etr ON etr.exhibition_id = e.id
                LEFT JOIN exhibition_tags et ON et.id = etr.tag_id
                WHERE e.status = 'published' AND e.visibility = 'public'
                """);

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (e.title LIKE ? OR e.summary LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (grade != null && !grade.isBlank()) {
            sql.append(" AND c.grade_level = ? ");
            args.add(grade);
        }
        if (theme != null && !theme.isBlank()) {
            sql.append(" AND et.tag_name = ? ");
            args.add(theme);
        }
        if (tag != null && !tag.isBlank()) {
            sql.append(" AND et.tag_name = ? ");
            args.add(tag);
        }

        if (!countOnly) {
            if ("likes".equals(sortBy)) {
                sql.append(" ORDER BY e.like_count DESC, e.published_at DESC ");
            } else if ("hot".equals(sortBy)) {
                sql.append(" ORDER BY (e.like_count + e.favorite_count + e.comment_count) DESC, e.published_at DESC ");
            } else {
                sql.append(" ORDER BY e.published_at DESC, e.id DESC ");
            }
        }

        return new QuerySpec(sql.toString(), args);
    }

    private record QuerySpec(
            String sql,
            List<Object> args
    ) {
    }
}
