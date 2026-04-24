package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.model.request.TaskRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.model.response.SubmissionResponses;
import com.zhixingchuangjing.platform.model.response.TaskResponses;

import java.util.List;

public interface TaskService {

    TaskResponses.TaskDetailResponse createTask(Long userId, String role, TaskRequests.CreateTaskRequest request);

    void updateTask(Long taskId, Long userId, String role, TaskRequests.UpdateTaskRequest request);

    SubmissionResponses.SubmissionDetailResponse submitTaskWork(Long taskId,
                                                               Long userId,
                                                               String role,
                                                               TaskRequests.CreateSubmissionRequest request);

    PageResponse<TaskResponses.TaskSummaryResponse> getTasks(Long userId,
                                                            String role,
                                                            String status,
                                                            Long classId,
                                                            String keyword,
                                                            Integer page,
                                                            Integer pageSize);

    TaskResponses.TaskDetailResponse getTaskDetail(Long taskId, Long userId, String role);

    List<ExhibitionResponses.ExhibitionSummaryResponse> getExcellentExhibitions(Long taskId, Long userId, String role);

    TaskResponses.TaskProgressResponse getTaskProgress(Long taskId, Long userId, String role);

    List<SubmissionResponses.SubmissionDetailResponse> getTaskSubmissions(Long taskId, Long userId, String role);
}
