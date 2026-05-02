package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.model.response.TaskResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean canAccessTask(Long taskId, Long userId, String role) {
        String sql;
        if ("admin".equals(role)) {
            sql = "SELECT COUNT(1) FROM tasks WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId);
            return count != null && count > 0;
        }

        sql = """
                SELECT COUNT(DISTINCT t.id)
                FROM tasks t
                LEFT JOIN task_target_classes ttc ON ttc.task_id = t.id
                LEFT JOIN class_members cm ON cm.class_id = ttc.class_id AND cm.user_id = ? AND cm.status = 'active'
                WHERE t.id = ?
                  AND (t.creator_id = ? OR cm.user_id IS NOT NULL)
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, taskId, userId);
        return count != null && count > 0;
    }

    public boolean canManageTaskProgress(Long taskId, Long userId, String role) {
        if ("admin".equals(role)) {
            return true;
        }
        String sql = "SELECT COUNT(1) FROM tasks WHERE id = ? AND creator_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId, userId);
        return count != null && count > 0;
    }

    public List<TaskResponses.TaskSummaryResponse> findPagedTasks(Long userId,
                                                                  String role,
                                                                  String status,
                                                                  Long classId,
                                                                  String keyword,
                                                                  int offset,
                                                                  int pageSize) {
        QuerySpec spec = buildTaskListQuery(userId, role, status, classId, keyword, false);
        List<Object> args = new ArrayList<>(spec.args());
        args.add(offset);
        args.add(pageSize);
        return jdbcTemplate.query(spec.sql() + " ORDER BY t.created_at DESC LIMIT ?, ?", (rs, rowNum) ->
                new TaskResponses.TaskSummaryResponse(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("cover_url"),
                        rs.getString("description"),
                        buildSimpleUser(rs.getLong("teacher_id"), rs.getString("teacher_role"),
                                rs.getString("teacher_name"), rs.getString("teacher_nickname"), rs.getString("teacher_avatar_url")),
                        rs.getObject("start_time", LocalDateTime.class),
                        rs.getObject("due_time", LocalDateTime.class),
                        rs.getString("status")
                ), args.toArray());
    }

    public long countTasks(Long userId, String role, String status, Long classId, String keyword) {
        QuerySpec spec = buildTaskListQuery(userId, role, status, classId, keyword, true);
        Long count = jdbcTemplate.queryForObject(spec.sql(), Long.class, spec.args().toArray());
        return count == null ? 0L : count;
    }

    public List<TaskResponses.TaskSummaryResponse> findOngoingTasks(Long userId, String role, int limit) {
        List<TaskResponses.TaskSummaryResponse> list = findPagedTasks(userId, role, "published", null, null, 0, limit);
        return list;
    }

    public Optional<TaskDetailBase> findTaskDetailBase(Long taskId) {
        String sql = """
                SELECT
                  t.id,
                  t.title,
                  t.cover_url,
                  t.description,
                  t.evaluation_criteria,
                  t.start_time,
                  t.due_time,
                  t.status,
                  u.id AS teacher_id,
                  u.role AS teacher_role,
                  u.real_name AS teacher_name,
                  u.nickname AS teacher_nickname,
                  u.avatar_url AS teacher_avatar_url
                FROM tasks t
                JOIN users u ON u.id = t.creator_id
                WHERE t.id = ?
                LIMIT 1
                """;
        List<TaskDetailBase> list = jdbcTemplate.query(sql, (rs, rowNum) -> new TaskDetailBase(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("cover_url"),
                rs.getString("description"),
                buildSimpleUser(rs.getLong("teacher_id"), rs.getString("teacher_role"),
                        rs.getString("teacher_name"), rs.getString("teacher_nickname"), rs.getString("teacher_avatar_url")),
                rs.getObject("start_time", LocalDateTime.class),
                rs.getObject("due_time", LocalDateTime.class),
                rs.getString("status"),
                rs.getString("evaluation_criteria")
        ), taskId);
        return list.stream().findFirst();
    }

    public List<TaskResponses.TaskMaterialResponse> findTaskMaterials(Long taskId) {
        String sql = """
                SELECT id, title, material_type, COALESCE(file_url, external_url) AS material_url, description
                FROM task_materials
                WHERE task_id = ?
                ORDER BY sort_no ASC, id ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new TaskResponses.TaskMaterialResponse(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("material_type"),
                rs.getString("material_url"),
                rs.getString("description")
        ), taskId);
    }

    public List<CommonResponses.ClassInfo> findTaskTargetClasses(Long taskId) {
        String sql = """
                SELECT c.id, c.class_name, c.grade_level, c.academic_year
                FROM task_target_classes ttc
                JOIN classes c ON c.id = ttc.class_id
                WHERE ttc.task_id = ?
                ORDER BY c.grade_level ASC, c.class_name ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new CommonResponses.ClassInfo(
                rs.getLong("id"),
                rs.getString("class_name"),
                rs.getString("grade_level"),
                rs.getString("academic_year")
        ), taskId);
    }

    public List<ExhibitionResponses.ExhibitionSummaryResponse> findExcellentExhibitions(Long taskId, int limit) {
        String sql = """
                SELECT
                  e.id,
                  e.task_id,
                  e.title,
                  e.cover_url,
                  e.summary,
                  e.status,
                  e.visibility,
                  e.group_name,
                  e.owner_id,
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
                FROM exhibitions e
                JOIN users u ON u.id = e.owner_id
                WHERE e.task_id = ? AND e.featured_flag = 1
                ORDER BY e.featured_at DESC, e.published_at DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ExhibitionResponses.ExhibitionSummaryResponse(
                rs.getLong("id"),
                rs.getObject("task_id", Long.class),
                rs.getString("title"),
                rs.getString("cover_url"),
                rs.getString("summary"),
                rs.getString("status"),
                rs.getString("visibility"),
                rs.getString("group_name"),
                rs.getLong("owner_id"),
                buildSimpleUser(rs.getLong("author_id"), rs.getString("author_role"),
                        rs.getString("author_name"), rs.getString("author_nickname"), rs.getString("author_avatar_url")),
                rs.getInt("latest_version_no"),
                rs.getInt("published_version_no"),
                new ExhibitionResponses.ExhibitionStatsResponse(
                        rs.getInt("view_count"),
                        rs.getInt("like_count"),
                        rs.getInt("favorite_count"),
                        rs.getInt("comment_count")
                ),
                List.of(),
                null, null, null
        ), taskId, limit);
    }

    public TaskResponses.TaskProgressResponse findTaskProgress(Long taskId) {
        String summarySql = """
                SELECT
                  COALESCE(SUM(CASE WHEN submission_status = 'submitted' THEN 1 ELSE 0 END), 0) AS submitted_count,
                  COALESCE(SUM(CASE WHEN submission_status = 'reviewed' THEN 1 ELSE 0 END), 0) AS reviewed_count
                FROM task_submissions
                WHERE task_id = ?
                """;
        TaskProgressSummary summary = jdbcTemplate.queryForObject(summarySql, (rs, rowNum) -> new TaskProgressSummary(
                rs.getInt("submitted_count"),
                rs.getInt("reviewed_count")
        ), taskId);

        String groupsSql = """
                SELECT
                  e.id AS exhibition_id,
                  e.group_name,
                  owner.real_name AS leader_name,
                  (
                    SELECT COUNT(1) FROM exhibition_members em
                    WHERE em.exhibition_id = e.id AND em.status = 'active'
                  ) AS member_count,
                  LEAST(COALESCE(ev.element_count, 0) * 10, 100) AS progress_percent,
                  ts.submission_status,
                  e.updated_at
                FROM exhibitions e
                LEFT JOIN users owner ON owner.id = e.owner_id
                LEFT JOIN exhibition_versions ev ON ev.exhibition_id = e.id AND ev.version_no = e.latest_version_no
                LEFT JOIN task_submissions ts ON ts.exhibition_id = e.id AND ts.task_id = e.task_id
                WHERE e.task_id = ?
                ORDER BY e.updated_at DESC
                """;
        List<TaskResponses.TaskProgressGroupResponse> groups = jdbcTemplate.query(groupsSql, (rs, rowNum) ->
                new TaskResponses.TaskProgressGroupResponse(
                        rs.getLong("exhibition_id"),
                        rs.getString("group_name"),
                        rs.getString("leader_name"),
                        rs.getInt("member_count"),
                        rs.getInt("progress_percent"),
                        rs.getString("submission_status"),
                        rs.getObject("updated_at", LocalDateTime.class)
                ), taskId);

        return new TaskResponses.TaskProgressResponse(
                taskId,
                summary == null ? 0 : summary.submittedCount(),
                summary == null ? 0 : summary.reviewedCount(),
                groups
        );
    }

    private QuerySpec buildTaskListQuery(Long userId, String role, String status, Long classId, String keyword, boolean countOnly) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();

        if (countOnly) {
            sql.append("SELECT COUNT(DISTINCT t.id) ");
        } else {
            sql.append("""
                    SELECT DISTINCT
                      t.id,
                      t.title,
                      t.cover_url,
                      t.description,
                      t.start_time,
                      t.due_time,
                      t.created_at,
                      t.status,
                      u.id AS teacher_id,
                      u.role AS teacher_role,
                      u.real_name AS teacher_name,
                      u.nickname AS teacher_nickname,
                      u.avatar_url AS teacher_avatar_url
                    """);
        }

        sql.append("""
                FROM tasks t
                JOIN users u ON u.id = t.creator_id
                LEFT JOIN task_target_classes ttc ON ttc.task_id = t.id
                LEFT JOIN class_members cm ON cm.class_id = ttc.class_id AND cm.user_id = ? AND cm.status = 'active'
                WHERE 1 = 1
                """);
        args.add(userId);

        if (!"admin".equals(role)) {
            sql.append(" AND (t.creator_id = ? OR cm.user_id IS NOT NULL) ");
            args.add(userId);
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND t.status = ? ");
            args.add(status);
        }
        if (classId != null) {
            sql.append(" AND ttc.class_id = ? ");
            args.add(classId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (t.title LIKE ? OR t.description LIKE ?) ");
            String likeKeyword = "%" + keyword.trim() + "%";
            args.add(likeKeyword);
            args.add(likeKeyword);
        }

        return new QuerySpec(sql.toString(), args);
    }

    private CommonResponses.SimpleUser buildSimpleUser(Long id, String role, String name, String nickname, String avatarUrl) {
        return new CommonResponses.SimpleUser(id, role, name, nickname, avatarUrl);
    }

    public record TaskDetailBase(
            Long id,
            String title,
            String coverUrl,
            String description,
            CommonResponses.SimpleUser teacher,
            LocalDateTime startTime,
            LocalDateTime dueTime,
            String status,
            String evaluationCriteria
    ) {
    }

    private record TaskProgressSummary(
            int submittedCount,
            int reviewedCount
    ) {
    }

    private record QuerySpec(
            String sql,
            List<Object> args
    ) {
    }
}
