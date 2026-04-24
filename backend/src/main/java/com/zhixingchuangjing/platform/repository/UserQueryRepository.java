package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.model.response.UserResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserResponses.UserProfileResponse> findUserProfile(Long userId) {
        String sql = """
                SELECT
                  u.id,
                  u.role,
                  u.real_name,
                  u.nickname,
                  u.avatar_url,
                  u.email,
                  u.mobile,
                  u.bio,
                  s.id AS school_id,
                  s.school_name,
                  c.id AS class_id,
                  c.class_name,
                  c.grade_level,
                  c.academic_year
                FROM users u
                LEFT JOIN schools s ON s.id = u.school_id
                LEFT JOIN class_members cm ON cm.user_id = u.id AND cm.status = 'active'
                LEFT JOIN classes c ON c.id = cm.class_id
                WHERE u.id = ?
                ORDER BY c.id ASC
                LIMIT 1
                """;

        List<UserResponses.UserProfileResponse> list = jdbcTemplate.query(sql, (rs, rowNum) -> new UserResponses.UserProfileResponse(
                rs.getLong("id"),
                rs.getString("role"),
                rs.getString("real_name"),
                rs.getString("nickname"),
                rs.getString("avatar_url"),
                rs.getString("email"),
                rs.getString("mobile"),
                rs.getString("bio"),
                buildSchoolInfo(rs.getObject("school_id", Long.class), rs.getString("school_name")),
                buildClassInfo(rs.getObject("class_id", Long.class), rs.getString("class_name"),
                        rs.getString("grade_level"), rs.getString("academic_year"))
        ), userId);

        return list.stream().findFirst();
    }

    public List<UserResponses.PortfolioItemResponse> findPortfolio(Long userId) {
        String sql = """
                SELECT
                  id AS exhibition_id,
                  title,
                  cover_url,
                  published_at,
                  view_count,
                  like_count
                FROM exhibitions
                WHERE owner_id = ? AND status = 'published'
                ORDER BY published_at DESC, id DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new UserResponses.PortfolioItemResponse(
                rs.getLong("exhibition_id"),
                rs.getString("title"),
                rs.getString("cover_url"),
                rs.getObject("published_at", java.time.LocalDateTime.class),
                rs.getInt("view_count"),
                rs.getInt("like_count")
        ), userId);
    }

    public List<UserResponses.PortfolioItemResponse> findHomepagePortfolio(Long userId) {
        String sql = """
                SELECT
                  id AS exhibition_id,
                  title,
                  cover_url,
                  published_at,
                  view_count,
                  like_count
                FROM exhibitions
                WHERE owner_id = ? AND status = 'published'
                ORDER BY published_at DESC, id DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new UserResponses.PortfolioItemResponse(
                rs.getLong("exhibition_id"),
                rs.getString("title"),
                rs.getString("cover_url"),
                rs.getObject("published_at", java.time.LocalDateTime.class),
                rs.getInt("view_count"),
                rs.getInt("like_count")
        ), userId);
    }

    public UserResponses.UserHomepageStatsResponse findHomepageStats(Long userId) {
        String sql = """
                SELECT
                  COUNT(DISTINCT e.id) AS exhibition_count,
                  COALESCE(SUM(e.like_count), 0) AS like_count,
                  (
                    SELECT COUNT(1)
                    FROM submission_reviews sr
                    JOIN task_submissions ts ON ts.id = sr.submission_id
                    JOIN exhibitions ex ON ex.id = ts.exhibition_id
                    WHERE ex.owner_id = ? AND sr.score > 90
                  ) AS teacher_praise_count
                FROM exhibitions e
                WHERE e.owner_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new UserResponses.UserHomepageStatsResponse(
                rs.getInt("exhibition_count"),
                rs.getInt("like_count"),
                rs.getInt("teacher_praise_count")
        ), userId, userId);
    }

    private CommonResponses.SchoolInfo buildSchoolInfo(Long schoolId, String schoolName) {
        if (schoolId == null) {
            return null;
        }
        return new CommonResponses.SchoolInfo(schoolId, schoolName);
    }

    private CommonResponses.ClassInfo buildClassInfo(Long classId, String className, String grade, String academicYear) {
        if (classId == null) {
            return null;
        }
        return new CommonResponses.ClassInfo(classId, className, grade, academicYear);
    }
}
