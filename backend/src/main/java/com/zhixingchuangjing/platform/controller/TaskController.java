package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.TaskRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.model.response.SubmissionResponses;
import com.zhixingchuangjing.platform.model.response.TaskResponses;
import com.zhixingchuangjing.platform.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/tasks")
public class TaskController extends BaseController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ApiResponse<TaskResponses.TaskDetailResponse> createTask(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody TaskRequests.CreateTaskRequest request) {
        return success(taskService.createTask(currentUser.getId(), currentUser.getRole(), request));
    }

    @GetMapping
    public ApiResponse<PageResponse<TaskResponses.TaskSummaryResponse>> getTasks(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return success(taskService.getTasks(currentUser.getId(), currentUser.getRole(), status, classId, keyword, page, pageSize));
    }

    @PutMapping("/{taskId}")
    public ApiResponse<Void> updateTask(@PathVariable Long taskId,
                                        @AuthenticationPrincipal SecurityUserDetails currentUser,
                                        @Valid @RequestBody TaskRequests.UpdateTaskRequest request) {
        taskService.updateTask(taskId, currentUser.getId(), currentUser.getRole(), request);
        return successMessage("任务更新成功");
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskResponses.TaskDetailResponse> getTaskDetail(@PathVariable Long taskId,
                                                                       @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(taskService.getTaskDetail(taskId, currentUser.getId(), currentUser.getRole()));
    }

    @GetMapping("/{taskId}/excellent-exhibitions")
    public ApiResponse<List<ExhibitionResponses.ExhibitionSummaryResponse>> getExcellentExhibitions(
            @PathVariable Long taskId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(taskService.getExcellentExhibitions(taskId, currentUser.getId(), currentUser.getRole()));
    }

    @GetMapping("/{taskId}/progress")
    public ApiResponse<TaskResponses.TaskProgressResponse> getTaskProgress(@PathVariable Long taskId,
                                                                           @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(taskService.getTaskProgress(taskId, currentUser.getId(), currentUser.getRole()));
    }

    @PostMapping("/{taskId}/submit")
    public ApiResponse<SubmissionResponses.SubmissionDetailResponse> submitTaskWork(
            @PathVariable Long taskId,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody TaskRequests.CreateSubmissionRequest request) {
        return success(taskService.submitTaskWork(taskId, currentUser.getId(), currentUser.getRole(), request));
    }

    @GetMapping("/{taskId}/submissions")
    public ApiResponse<List<SubmissionResponses.SubmissionDetailResponse>> getTaskSubmissions(
            @PathVariable Long taskId,
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(taskService.getTaskSubmissions(taskId, currentUser.getId(), currentUser.getRole()));
    }
}
