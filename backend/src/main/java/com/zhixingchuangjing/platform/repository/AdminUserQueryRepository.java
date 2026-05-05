package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.AdminResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdminUserQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminUserQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminResponses.TeacherRegistrationItemResponse> findTeacherRegistrations(String status) {
        String normalizedStatus = (status == null || status.isBlank()) ? "pending" : status.trim().toLowerCase();
        return jdbcTemplate.query("""
                SELECT u.id,
                       u.account,
                       u.role,
                       u.real_name,
                       u.nickname,
                       u.school_id,
                       s.school_name,
                       u.teacher_no,
                       u.status,
                       u.review_remark,
                       u.created_at
                FROM users u
                LEFT JOIN schools s ON s.id = u.school_id
                WHERE u.role = 'teacher' AND u.status = ?
                ORDER BY u.created_at ASC
                """, (rs, rowNum) -> new AdminResponses.TeacherRegistrationItemResponse(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("role"),
                rs.getString("real_name"),
                rs.getString("nickname"),
                rs.getObject("school_id", Long.class),
                rs.getString("school_name"),
                rs.getString("teacher_no"),
                rs.getString("status"),
                rs.getString("review_remark"),
                rs.getTimestamp("created_at").toLocalDateTime()
        ), normalizedStatus);
    }
}
