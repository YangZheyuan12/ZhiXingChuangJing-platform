package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.model.response.SubmissionResponses;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class SubmissionRepository {

    private final JdbcTemplate jdbcTemplate;

    public SubmissionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createSubmission(Long taskId,
                                 Long exhibitionId,
                                 Long submitterId,
                                 Integer versionNo,
                                 String submitRemark) {
        String sql = """
                INSERT INTO task_submissions (
                  task_id, exhibition_id, submitter_id, version_no, submit_remark,
                  submission_status, submitted_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, 'submitted', NOW(), NOW(), NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, taskId);
            ps.setLong(2, exhibitionId);
            ps.setLong(3, submitterId);
            ps.setInt(4, versionNo);
            ps.setString(5, submitRemark);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<SubmissionResponses.SubmissionDetailResponse> findTaskSubmissions(Long taskId) {
        String sql = """
                SELECT
                  ts.id,
                  ts.task_id,
                  ts.exhibition_id,
                  ts.submitter_id,
                  ts.version_no,
                  ts.submit_remark,
                  ts.submission_status,
                  ts.submitted_at,
                  ts.reviewed_at,
                  u.role AS submitter_role,
                  u.real_name AS submitter_name,
                  u.nickname AS submitter_nickname,
                  u.avatar_url AS submitter_avatar
                FROM task_submissions ts
                JOIN users u ON u.id = ts.submitter_id
                WHERE ts.task_id = ?
                ORDER BY ts.submitted_at DESC, ts.id DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new SubmissionResponses.SubmissionDetailResponse(
                rs.getLong("id"),
                rs.getLong("task_id"),
                rs.getLong("exhibition_id"),
                buildSimpleUser(
                        rs.getLong("submitter_id"),
                        rs.getString("submitter_role"),
                        rs.getString("submitter_name"),
                        rs.getString("submitter_nickname"),
                        rs.getString("submitter_avatar")
                ),
                rs.getInt("version_no"),
                rs.getString("submit_remark"),
                rs.getString("submission_status"),
                rs.getObject("submitted_at", LocalDateTime.class),
                rs.getObject("reviewed_at", LocalDateTime.class),
                findSubmissionReviews(rs.getLong("id"))
        ), taskId);
    }

    public Optional<SubmissionResponses.SubmissionDetailResponse> findSubmissionDetail(Long submissionId) {
        String sql = """
                SELECT
                  ts.id,
                  ts.task_id,
                  ts.exhibition_id,
                  ts.submitter_id,
                  ts.version_no,
                  ts.submit_remark,
                  ts.submission_status,
                  ts.submitted_at,
                  ts.reviewed_at,
                  u.role AS submitter_role,
                  u.real_name AS submitter_name,
                  u.nickname AS submitter_nickname,
                  u.avatar_url AS submitter_avatar
                FROM task_submissions ts
                JOIN users u ON u.id = ts.submitter_id
                WHERE ts.id = ?
                LIMIT 1
                """;
        List<SubmissionResponses.SubmissionDetailResponse> list = jdbcTemplate.query(sql, (rs, rowNum) -> new SubmissionResponses.SubmissionDetailResponse(
                rs.getLong("id"),
                rs.getLong("task_id"),
                rs.getLong("exhibition_id"),
                buildSimpleUser(
                        rs.getLong("submitter_id"),
                        rs.getString("submitter_role"),
                        rs.getString("submitter_name"),
                        rs.getString("submitter_nickname"),
                        rs.getString("submitter_avatar")
                ),
                rs.getInt("version_no"),
                rs.getString("submit_remark"),
                rs.getString("submission_status"),
                rs.getObject("submitted_at", LocalDateTime.class),
                rs.getObject("reviewed_at", LocalDateTime.class),
                findSubmissionReviews(rs.getLong("id"))
        ), submissionId);
        return list.stream().findFirst();
    }

    public List<ExhibitionResponses.SubmissionReviewResponse> findSubmissionReviews(Long submissionId) {
        String sql = """
                SELECT
                  sr.id,
                  sr.score,
                  sr.comment_text,
                  sr.comment_audio_url,
                  sr.is_public,
                  sr.created_at,
                  u.id AS reviewer_id,
                  u.role AS reviewer_role,
                  u.real_name AS reviewer_name,
                  u.nickname AS reviewer_nickname,
                  u.avatar_url AS reviewer_avatar
                FROM submission_reviews sr
                JOIN users u ON u.id = sr.reviewer_id
                WHERE sr.submission_id = ?
                ORDER BY sr.created_at DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ExhibitionResponses.SubmissionReviewResponse(
                rs.getLong("id"),
                buildSimpleUser(
                        rs.getLong("reviewer_id"),
                        rs.getString("reviewer_role"),
                        rs.getString("reviewer_name"),
                        rs.getString("reviewer_nickname"),
                        rs.getString("reviewer_avatar")
                ),
                rs.getObject("score", Double.class),
                rs.getString("comment_text"),
                rs.getString("comment_audio_url"),
                rs.getBoolean("is_public"),
                rs.getObject("created_at", LocalDateTime.class)
        ), submissionId);
    }

    public Long upsertReview(Long submissionId,
                             Long reviewerId,
                             Double score,
                             String commentText,
                             String commentAudioUrl,
                             boolean isPublic) {
        Long existingId = findExistingReviewId(submissionId, reviewerId);
        if (existingId == null) {
            String sql = """
                    INSERT INTO submission_reviews (
                      submission_id, reviewer_id, score, comment_text, comment_audio_url, is_public, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, submissionId);
                ps.setLong(2, reviewerId);
                ps.setObject(3, score);
                ps.setString(4, commentText);
                ps.setString(5, commentAudioUrl);
                ps.setBoolean(6, isPublic);
                return ps;
            }, keyHolder);
            existingId = keyHolder.getKey().longValue();
        } else {
            String sql = """
                    UPDATE submission_reviews
                    SET score = ?,
                        comment_text = ?,
                        comment_audio_url = ?,
                        is_public = ?,
                        updated_at = NOW()
                    WHERE id = ?
                    """;
            jdbcTemplate.update(sql, score, commentText, commentAudioUrl, isPublic, existingId);
        }

        jdbcTemplate.update("""
                UPDATE task_submissions
                SET submission_status = 'reviewed',
                    reviewed_at = NOW(),
                    updated_at = NOW()
                WHERE id = ?
                """, submissionId);
        return existingId;
    }

    public Long findTaskIdBySubmissionId(Long submissionId) {
        List<Long> list = jdbcTemplate.query("SELECT task_id FROM task_submissions WHERE id = ? LIMIT 1",
                (rs, rowNum) -> rs.getLong("task_id"), submissionId);
        return list.isEmpty() ? null : list.get(0);
    }

    private Long findExistingReviewId(Long submissionId, Long reviewerId) {
        List<Long> ids = jdbcTemplate.query("""
                SELECT id
                FROM submission_reviews
                WHERE submission_id = ? AND reviewer_id = ?
                LIMIT 1
                """, (rs, rowNum) -> rs.getLong("id"), submissionId, reviewerId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private CommonResponses.SimpleUser buildSimpleUser(Long id,
                                                       String role,
                                                       String realName,
                                                       String nickname,
                                                       String avatarUrl) {
        return new CommonResponses.SimpleUser(id, role, realName, nickname, avatarUrl);
    }
}
