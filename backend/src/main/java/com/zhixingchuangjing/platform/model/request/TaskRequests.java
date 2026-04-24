package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class TaskRequests {

    private TaskRequests() {
    }

    public record TaskMaterialRequest(
            @NotBlank(message = "资料标题不能为空")
            @Size(max = 128, message = "资料标题长度不能超过128个字符")
            String title,
            @NotBlank(message = "资料类型不能为空")
            String materialType,
            @Size(max = 255, message = "资料地址长度不能超过255个字符")
            String url,
            @Size(max = 255, message = "资料描述长度不能超过255个字符")
            String description
    ) {
    }

    public record CreateTaskRequest(
            @NotBlank(message = "任务标题不能为空")
            @Size(max = 128, message = "任务标题长度不能超过128个字符")
            String title,
            @Size(max = 255, message = "封面地址长度不能超过255个字符")
            String coverUrl,
            @NotBlank(message = "任务描述不能为空")
            String description,
            @Valid
            List<TaskMaterialRequest> backgroundMaterials,
            String evaluationCriteria,
            LocalDateTime startTime,
            @NotNull(message = "截止时间不能为空")
            LocalDateTime dueTime,
            @NotEmpty(message = "至少选择一个投放班级")
            List<Long> targetClassIds
    ) {
    }

    public record UpdateTaskRequest(
            @NotBlank(message = "任务标题不能为空")
            @Size(max = 128, message = "任务标题长度不能超过128个字符")
            String title,
            @Size(max = 255, message = "封面地址长度不能超过255个字符")
            String coverUrl,
            @NotBlank(message = "任务描述不能为空")
            String description,
            @Valid
            List<TaskMaterialRequest> backgroundMaterials,
            String evaluationCriteria,
            LocalDateTime startTime,
            @NotNull(message = "截止时间不能为空")
            LocalDateTime dueTime,
            @NotEmpty(message = "至少选择一个投放班级")
            List<Long> targetClassIds
    ) {
    }

    public record CreateSubmissionRequest(
            @NotNull(message = "展厅ID不能为空")
            Long exhibitionId,
            @Size(max = 255, message = "提交备注长度不能超过255个字符")
            String submitRemark
    ) {
    }
}
