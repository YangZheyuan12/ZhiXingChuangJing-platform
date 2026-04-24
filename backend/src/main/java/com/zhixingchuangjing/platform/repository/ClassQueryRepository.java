package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.ClassResponses;
import com.zhixingchuangjing.platform.model.response.CommonResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ClassQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public ClassQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean isClassMember(Long classId, Long userId) {
        String sql = """
                SELECT COUNT(1)
                FROM class_members
                WHERE class_id = ? AND user_id = ? AND status = 'active'
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, classId, userId);
        return count != null && count > 0;
    }

    public boolean isClassTeacher(Long classId, Long userId) {
        String sql = """
                SELECT COUNT(1)
                FROM class_members
                WHERE class_id = ? AND user_id = ? AND status = 'active' AND member_role IN ('teacher', 'assistant')
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, classId, userId);
        return count != null && count > 0;
    }

    public List<CommonResponses.ClassInfo> findMyClasses(Long userId) {
        String sql = """
                SELECT c.id, c.class_name, c.grade_level, c.academic_year
                FROM class_members cm
                JOIN classes c ON c.id = cm.class_id
                WHERE cm.user_id = ? AND cm.status = 'active'
                ORDER BY c.grade_level ASC, c.class_name ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new CommonResponses.ClassInfo(
                rs.getLong("id"),
                rs.getString("class_name"),
                rs.getString("grade_level"),
                rs.getString("academic_year")
        ), userId);
    }

    public Optional<ClassResponses.ClassDetailResponse> findClassDetail(Long classId) {
        String sql = """
                SELECT
                  c.id,
                  c.class_name,
                  c.grade_level,
                  c.academic_year,
                  s.id AS school_id,
                  s.school_name,
                  u.id AS teacher_id,
                  u.role AS teacher_role,
                  u.real_name AS teacher_name,
                  u.nickname AS teacher_nickname,
                  u.avatar_url AS teacher_avatar_url,
                  (
                    SELECT COUNT(1) FROM class_members cm
                    WHERE cm.class_id = c.id AND cm.status = 'active'
                  ) AS member_count
                FROM classes c
                LEFT JOIN schools s ON s.id = c.school_id
                LEFT JOIN users u ON u.id = c.head_teacher_id
                WHERE c.id = ?
                LIMIT 1
                """;

        List<ClassResponses.ClassDetailResponse> list = jdbcTemplate.query(sql, (rs, rowNum) -> new ClassResponses.ClassDetailResponse(
                rs.getLong("id"),
                rs.getString("class_name"),
                rs.getString("grade_level"),
                rs.getString("academic_year"),
                buildSchoolInfo(rs.getObject("school_id", Long.class), rs.getString("school_name")),
                buildSimpleUser(rs.getObject("teacher_id", Long.class), rs.getString("teacher_role"),
                        rs.getString("teacher_name"), rs.getString("teacher_nickname"), rs.getString("teacher_avatar_url")),
                rs.getInt("member_count")
        ), classId);

        return list.stream().findFirst();
    }

    public List<ClassResponses.ClassMemberResponse> findClassMembers(Long classId) {
        String sql = """
                SELECT
                  u.id AS user_id,
                  cm.member_role,
                  u.real_name,
                  u.nickname,
                  u.avatar_url,
                  cm.joined_at
                FROM class_members cm
                JOIN users u ON u.id = cm.user_id
                WHERE cm.class_id = ? AND cm.status = 'active'
                ORDER BY FIELD(cm.member_role, 'teacher', 'assistant', 'student'), u.real_name ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ClassResponses.ClassMemberResponse(
                rs.getLong("user_id"),
                rs.getString("member_role"),
                rs.getString("real_name"),
                rs.getString("nickname"),
                rs.getString("avatar_url"),
                rs.getObject("joined_at", LocalDateTime.class)
        ), classId);
    }

    public List<ClassResponses.AnnouncementResponse> findAnnouncements(Long classId) {
        String sql = """
                SELECT
                  a.id,
                  a.class_id,
                  a.title,
                  a.content,
                  a.pinned,
                  a.published_at,
                  u.id AS publisher_id,
                  u.role AS publisher_role,
                  u.real_name AS publisher_name,
                  u.nickname AS publisher_nickname,
                  u.avatar_url AS publisher_avatar_url
                FROM announcements a
                JOIN users u ON u.id = a.publisher_id
                WHERE a.class_id = ?
                ORDER BY a.pinned DESC, a.published_at DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ClassResponses.AnnouncementResponse(
                rs.getLong("id"),
                rs.getLong("class_id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getBoolean("pinned"),
                buildSimpleUser(rs.getLong("publisher_id"), rs.getString("publisher_role"),
                        rs.getString("publisher_name"), rs.getString("publisher_nickname"), rs.getString("publisher_avatar_url")),
                rs.getObject("published_at", LocalDateTime.class)
        ), classId);
    }

    public void createAnnouncement(Long classId, Long publisherId, String title, String content, boolean pinned) {
        String sql = """
                INSERT INTO announcements (class_id, publisher_id, title, content, pinned, published_at, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, NOW(), NOW(), NOW())
                """;
        jdbcTemplate.update(sql, classId, publisherId, title, content, pinned);
    }

    private CommonResponses.SchoolInfo buildSchoolInfo(Long schoolId, String schoolName) {
        if (schoolId == null) {
            return null;
        }
        return new CommonResponses.SchoolInfo(schoolId, schoolName);
    }

    private CommonResponses.SimpleUser buildSimpleUser(Long id, String role, String name, String nickname, String avatarUrl) {
        if (id == null) {
            return null;
        }
        return new CommonResponses.SimpleUser(id, role, name, nickname, avatarUrl);
    }
}
