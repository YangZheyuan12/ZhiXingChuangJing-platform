package com.zhixingchuangjing.platform.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TaskCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createTask(Long creatorId,
                           String title,
                           String coverUrl,
                           String description,
                           String evaluationCriteria,
                           LocalDateTime startTime,
                           LocalDateTime dueTime) {
        String sql = """
                INSERT INTO tasks (
                  creator_id, title, cover_url, description, evaluation_criteria, start_time, due_time,
                  status, excellent_count, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'published', 0, NOW(), NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, creatorId);
            ps.setString(2, title);
            ps.setString(3, coverUrl);
            ps.setString(4, description);
            ps.setString(5, evaluationCriteria);
            ps.setObject(6, startTime);
            ps.setObject(7, dueTime);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void updateTask(Long taskId,
                           String title,
                           String coverUrl,
                           String description,
                           String evaluationCriteria,
                           LocalDateTime startTime,
                           LocalDateTime dueTime) {
        String sql = """
                UPDATE tasks
                SET title = ?,
                    cover_url = ?,
                    description = ?,
                    evaluation_criteria = ?,
                    start_time = ?,
                    due_time = ?,
                    updated_at = NOW()
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, title, coverUrl, description, evaluationCriteria, startTime, dueTime, taskId);
    }

    public void addTargetClasses(Long taskId, List<Long> classIds) {
        String sql = "INSERT INTO task_target_classes (task_id, class_id, created_at) VALUES (?, ?, NOW())";
        jdbcTemplate.batchUpdate(sql, classIds, classIds.size(), (ps, classId) -> {
            ps.setLong(1, taskId);
            ps.setLong(2, classId);
        });
    }

    public void replaceTargetClasses(Long taskId, List<Long> classIds) {
        jdbcTemplate.update("DELETE FROM task_target_classes WHERE task_id = ?", taskId);
        addTargetClasses(taskId, classIds);
    }

    public void addMaterials(Long taskId, List<MaterialCommand> materials) {
        if (materials == null || materials.isEmpty()) {
            return;
        }
        String sql = """
                INSERT INTO task_materials (
                  task_id, title, material_type, file_url, external_url, description, sort_no, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
                """;
        jdbcTemplate.batchUpdate(sql, materials, materials.size(), (ps, material) -> {
            ps.setLong(1, taskId);
            ps.setString(2, material.title());
            ps.setString(3, material.materialType());
            ps.setString(4, material.fileUrl());
            ps.setString(5, material.externalUrl());
            ps.setString(6, material.description());
            ps.setInt(7, material.sortNo());
        });
    }

    public void replaceMaterials(Long taskId, List<MaterialCommand> materials) {
        jdbcTemplate.update("DELETE FROM task_materials WHERE task_id = ?", taskId);
        addMaterials(taskId, materials);
    }

    public record MaterialCommand(
            String title,
            String materialType,
            String fileUrl,
            String externalUrl,
            String description,
            int sortNo
    ) {
    }
}
