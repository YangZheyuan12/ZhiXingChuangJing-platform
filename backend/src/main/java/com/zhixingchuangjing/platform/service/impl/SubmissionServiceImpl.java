package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.model.request.SubmissionRequests;
import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.model.response.SubmissionResponses;
import com.zhixingchuangjing.platform.repository.SubmissionRepository;
import com.zhixingchuangjing.platform.repository.TaskQueryRepository;
import com.zhixingchuangjing.platform.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final TaskQueryRepository taskQueryRepository;

    public SubmissionServiceImpl(SubmissionRepository submissionRepository, TaskQueryRepository taskQueryRepository) {
        this.submissionRepository = submissionRepository;
        this.taskQueryRepository = taskQueryRepository;
    }

    @Override
    public SubmissionResponses.SubmissionDetailResponse getSubmissionDetail(Long submissionId, Long userId, String role) {
        SubmissionResponses.SubmissionDetailResponse detail = submissionRepository.findSubmissionDetail(submissionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40471, "提交记录不存在"));
        if (!canAccessSubmission(detail, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40351, "无权查看该提交记录");
        }
        return detail;
    }

    @Override
    @Transactional
    public ExhibitionResponses.SubmissionReviewResponse createSubmissionReview(Long submissionId,
                                                                              Long userId,
                                                                              String role,
                                                                              String nickname,
                                                                              SubmissionRequests.CreateSubmissionReviewRequest request) {
        Long taskId = submissionRepository.findTaskIdBySubmissionId(submissionId);
        if (taskId == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40471, "提交记录不存在");
        }
        if (!taskQueryRepository.canManageTaskProgress(taskId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40352, "无权点评该提交记录");
        }
        Long reviewId = submissionRepository.upsertReview(
                submissionId,
                userId,
                request.score(),
                request.commentText(),
                request.commentAudioUrl(),
                request.isPublic() == null || request.isPublic()
        );
        return new ExhibitionResponses.SubmissionReviewResponse(
                reviewId,
                new CommonResponses.SimpleUser(userId, role, nickname, nickname, null),
                request.score(),
                request.commentText(),
                request.commentAudioUrl(),
                request.isPublic() == null || request.isPublic(),
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public void approveSubmission(Long submissionId,
                                  Long userId,
                                  String role,
                                  SubmissionRequests.ApproveSubmissionRequest request) {
        Long taskId = submissionRepository.findTaskIdBySubmissionId(submissionId);
        if (taskId == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40471, "提交记录不存在");
        }
        if (!taskQueryRepository.canManageTaskProgress(taskId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40352, "无权审核该提交记录");
        }
        // 写入评分（upsertReview 内部会将 submission_status 置为 'reviewed'）
        submissionRepository.upsertReview(
                submissionId,
                userId,
                request.score(),
                request.comment(),
                null,
                true
        );
        // 覆盖为 'approved'
        submissionRepository.updateSubmissionStatus(submissionId, "approved");
        // 同步展厅 workflow_status
        Long exhibitionId = submissionRepository.findExhibitionIdBySubmissionId(submissionId);
        if (exhibitionId != null) {
            submissionRepository.updateWorkflowStatus(exhibitionId, "approved");
        }
    }

    @Override
    @Transactional
    public void returnSubmission(Long submissionId,
                                 Long userId,
                                 String role,
                                 SubmissionRequests.ReturnSubmissionRequest request) {
        Long taskId = submissionRepository.findTaskIdBySubmissionId(submissionId);
        if (taskId == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40471, "提交记录不存在");
        }
        if (!taskQueryRepository.canManageTaskProgress(taskId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40352, "无权审核该提交记录");
        }
        // 写入退回原因作为 review 评论（无评分）
        submissionRepository.upsertReview(
                submissionId,
                userId,
                null,
                request.reason(),
                null,
                true
        );
        // 覆盖为 'returned'
        submissionRepository.updateSubmissionStatus(submissionId, "returned");
        Long exhibitionId = submissionRepository.findExhibitionIdBySubmissionId(submissionId);
        if (exhibitionId != null) {
            submissionRepository.updateWorkflowStatus(exhibitionId, "returned");
        }
    }

    private boolean canAccessSubmission(SubmissionResponses.SubmissionDetailResponse detail, Long userId, String role) {
        if ("admin".equals(role)) {
            return true;
        }
        if (detail.submitter() != null && userId.equals(detail.submitter().id())) {
            return true;
        }
        return taskQueryRepository.canManageTaskProgress(detail.taskId(), userId, role);
    }
}
