package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.CommonResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SchoolQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public SchoolQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CommonResponses.SchoolInfo> findEnabledSchools() {
        return jdbcTemplate.query("""
                SELECT id, school_name
                FROM schools
                WHERE status = 'enabled'
                ORDER BY school_name ASC
                """, (rs, rowNum) -> new CommonResponses.SchoolInfo(
                rs.getLong("id"),
                rs.getString("school_name")
        ));
    }

    public boolean existsEnabledSchool(Long schoolId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM schools
                WHERE id = ? AND status = 'enabled'
                """, Integer.class, schoolId);
        return count != null && count > 0;
    }
}
