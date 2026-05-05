package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.UserRequests;
import com.zhixingchuangjing.platform.model.response.AdminResponses;

public interface AdminService {

    AdminResponses.TeacherRegistrationListResponse getTeacherRegistrations(String status);

    void approveTeacherRegistration(Long userId, Long adminUserId, UserRequests.ReviewTeacherRegistrationRequest request);

    void rejectTeacherRegistration(Long userId, Long adminUserId, UserRequests.ReviewTeacherRegistrationRequest request);
}
