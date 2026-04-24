package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.common.util.PageUtils;
import com.zhixingchuangjing.platform.model.request.TaskRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.model.response.SubmissionResponses;
import com.zhixingchuangjing.platform.model.response.TaskResponses;
import com.zhixingchuangjing.platform.repository.ClassQueryRepository;
import com.zhixingchuangjing.platform.repository.ExhibitionQueryRepository;
import com.zhixingchuangjing.platform.repository.SubmissionRepository;
import com.zhixingchuangjing.platform.repository.TaskCommandRepository;
import com.zhixingchuangjing.platform.repository.TaskQueryRepository;
import com.zhixingchuangjing.platform.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskQueryRepository taskQueryRepository;
    private final TaskCommandRepository taskCommandRepository;
    private final ClassQueryRepository classQueryRepository;
    private final SubmissionRepository submissionRepository;
    private final ExhibitionQueryRepository exhibitionQueryRepository;

    public TaskServiceImpl(TaskQueryRepository taskQueryRepository,
                           TaskCommandRepository taskCommandRepository,
                           ClassQueryRepository classQueryRepository,
                           SubmissionRepository submissionRepository,
                           ExhibitionQueryRepository exhibitionQueryRepository) {
        this.taskQueryRepository = taskQueryRepository;
        this.taskCommandRepository = taskCommandRepository;
        this.classQueryRepository = classQueryRepository;
        this.submissionRepository = submissionRepository;
        this.exhibitionQueryRepository = exhibitionQueryRepository;
    }

    @Override
    @Transactional
    public TaskResponses.TaskDetailResponse createTask(Long userId, String role, TaskRequests.CreateTaskRequest request) {
        assertTaskCreatePermission(userId, role);
        validateTaskRequest(request);

        List<Long> targetClassIds = request.targetClassIds().stream().distinct().toList();
        for (Long classId : targetClassIds) {
            if (classId == null || classQueryRepository.findClassDetail(classId).isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, 40031, "存在无效的班级信息");
            }
            if (!"admin".equals(role) && !classQueryRepository.isClassTeacher(classId, userId)) {
                throw new BusinessException(HttpStatus.FORBIDDEN, 40322, "仅可向本人管理的班级发布任务");
            }
        }

        Long taskId = taskCommandRepository.createTask(
                userId,
                request.title().trim(),
                normalizeText(request.coverUrl()),
                request.description().trim(),
                normalizeText(request.evaluationCriteria()),
                request.startTime(),
                request.dueTime()
        );
        taskCommandRepository.addTargetClasses(taskId, targetClassIds);
        taskCommandRepository.addMaterials(taskId, buildMaterialCommands(request.backgroundMaterials()));
        return getTaskDetail(taskId, userId, role);
    }

    @Override
    @Transactional
    public void updateTask(Long taskId, Long userId, String role, TaskRequests.UpdateTaskRequest request) {
        if (!taskQueryRepository.canManageTaskProgress(taskId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40324, "无权修改该任务");
        }
        validateTaskRequest(request.startTime(), request.dueTime());

        List<Long> targetClassIds = request.targetClassIds().stream().distinct().toList();
        for (Long classId : targetClassIds) {
            if (classId == null || classQueryRepository.findClassDetail(classId).isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, 40031, "存在无效的班级信息");
            }
            if (!"admin".equals(role) && !classQueryRepository.isClassTeacher(classId, userId)) {
                throw new BusinessException(HttpStatus.FORBIDDEN, 40322, "仅可向本人管理的班级发布任务");
            }
        }

        taskCommandRepository.updateTask(
                taskId,
                request.title().trim(),
                normalizeText(request.coverUrl()),
                request.description().trim(),
                normalizeText(request.evaluationCriteria()),
                request.startTime(),
                request.dueTime()
        );
        taskCommandRepository.replaceTargetClasses(taskId, targetClassIds);
        taskCommandRepository.replaceMaterials(taskId, buildMaterialCommands(request.backgroundMaterials()));
    }

    @Override
    @Transactional
    public SubmissionResponses.SubmissionDetailResponse submitTaskWork(Long taskId,
                                                                      Long userId,
                                                                      String role,
                                                                      TaskRequests.CreateSubmissionRequest request) {
        assertTaskAccess(taskId, userId, role);
        ExhibitionResponses.ExhibitionDetailResponse exhibition = exhibitionQueryRepository.findExhibitionDetail(request.exhibitionId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40431, "展厅不存在"));
        if (!request.exhibitionId().equals(exhibition.id()) || !exhibitionQueryRepository.canAccessExhibition(request.exhibitionId(), userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40325, "无权提交该展厅");
        }
        if (exhibition.taskId() == null || !taskId.equals(exhibition.taskId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40034, "该展厅未关联当前任务");
        }
        if (exhibition.latestVersionNo() == null || exhibition.latestVersionNo() <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40035, "请先保存展厅版本后再提交");
        }
        Long submissionId = submissionRepository.createSubmission(
                taskId,
                request.exhibitionId(),
                userId,
                exhibition.latestVersionNo(),
                normalizeText(request.submitRemark())
        );
        return submissionRepository.findSubmissionDetail(submissionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50012, "提交记录创建失败"));
    }

    @Override
    public PageResponse<TaskResponses.TaskSummaryResponse> getTasks(Long userId,
                                                                   String role,
                                                                   String status,
                                                                   Long classId,
                                                                   String keyword,
                                                                   Integer page,
                                                                   Integer pageSize) {
        int normalizedPage = PageUtils.normalizePage(page);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return new PageResponse<>(
                taskQueryRepository.findPagedTasks(userId, role, status, classId, keyword,
                        PageUtils.offset(normalizedPage, normalizedPageSize), normalizedPageSize),
                normalizedPage,
                normalizedPageSize,
                taskQueryRepository.countTasks(userId, role, status, classId, keyword)
        );
    }

    @Override
    public TaskResponses.TaskDetailResponse getTaskDetail(Long taskId, Long userId, String role) {
        assertTaskAccess(taskId, userId, role);
        TaskQueryRepository.TaskDetailBase base = taskQueryRepository.findTaskDetailBase(taskId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40421, "任务不存在"));
        return new TaskResponses.TaskDetailResponse(
                base.id(),
                base.title(),
                base.coverUrl(),
                base.description(),
                base.teacher(),
                base.startTime(),
                base.dueTime(),
                base.status(),
                base.evaluationCriteria(),
                taskQueryRepository.findTaskMaterials(taskId),
                taskQueryRepository.findTaskTargetClasses(taskId),
                taskQueryRepository.findExcellentExhibitions(taskId, 6)
        );
    }

    @Override
    public List<ExhibitionResponses.ExhibitionSummaryResponse> getExcellentExhibitions(Long taskId, Long userId, String role) {
        assertTaskAccess(taskId, userId, role);
        return taskQueryRepository.findExcellentExhibitions(taskId, 10);
    }

    @Override
    public TaskResponses.TaskProgressResponse getTaskProgress(Long taskId, Long userId, String role) {
        if (!taskQueryRepository.canManageTaskProgress(taskId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40320, "无权查看该任务进度");
        }
        return taskQueryRepository.findTaskProgress(taskId);
    }

    @Override
    public List<SubmissionResponses.SubmissionDetailResponse> getTaskSubmissions(Long taskId, Long userId, String role) {
        if (!taskQueryRepository.canManageTaskProgress(taskId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40320, "无权查看该任务提交记录");
        }
        return submissionRepository.findTaskSubmissions(taskId);
    }

    private void assertTaskAccess(Long taskId, Long userId, String role) {
        if (!taskQueryRepository.canAccessTask(taskId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40321, "无权访问该任务");
        }
    }

    private void assertTaskCreatePermission(Long userId, String role) {
        if ("admin".equals(role) || "teacher".equals(role)) {
            return;
        }
        throw new BusinessException(HttpStatus.FORBIDDEN, 40323, "当前角色不允许创建任务");
    }

    private void validateTaskRequest(TaskRequests.CreateTaskRequest request) {
        validateTaskRequest(request.startTime(), request.dueTime());
    }

    private void validateTaskRequest(java.time.LocalDateTime startTime, java.time.LocalDateTime dueTime) {
        if (startTime != null && dueTime.isBefore(startTime)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40032, "截止时间不能早于开始时间");
        }
    }

    private List<TaskCommandRepository.MaterialCommand> buildMaterialCommands(List<TaskRequests.TaskMaterialRequest> materials) {
        if (materials == null || materials.isEmpty()) {
            return List.of();
        }

        List<TaskCommandRepository.MaterialCommand> commands = new ArrayList<>();
        for (int index = 0; index < materials.size(); index++) {
            TaskRequests.TaskMaterialRequest material = materials.get(index);
            String materialType = material.materialType().trim().toLowerCase(Locale.ROOT);
            String url = normalizeText(material.url());
            if (url == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, 40033, "任务资料地址不能为空");
            }
            boolean externalLink = "link".equals(materialType);
            commands.add(new TaskCommandRepository.MaterialCommand(
                    material.title().trim(),
                    materialType,
                    externalLink ? null : url,
                    externalLink ? url : null,
                    normalizeText(material.description()),
                    index + 1
            ));
        }
        return commands;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
