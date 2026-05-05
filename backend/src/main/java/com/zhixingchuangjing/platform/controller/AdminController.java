package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.UserRequests;
import com.zhixingchuangjing.platform.model.response.AdminResponses;
import com.zhixingchuangjing.platform.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController extends BaseController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/teacher-registrations")
    public ApiResponse<AdminResponses.TeacherRegistrationListResponse> getTeacherRegistrations(
            @RequestParam(required = false) String status) {
        return success(adminService.getTeacherRegistrations(status));
    }

    @PostMapping("/teacher-registrations/{userId}/approve")
    public ApiResponse<Void> approveTeacherRegistration(@PathVariable Long userId,
                                                        @AuthenticationPrincipal SecurityUserDetails currentUser,
                                                        @Valid @RequestBody UserRequests.ReviewTeacherRegistrationRequest request) {
        adminService.approveTeacherRegistration(userId, currentUser.getId(), request);
        return successMessage("教师账号已审核通过");
    }

    @PostMapping("/teacher-registrations/{userId}/reject")
    public ApiResponse<Void> rejectTeacherRegistration(@PathVariable Long userId,
                                                       @AuthenticationPrincipal SecurityUserDetails currentUser,
                                                       @Valid @RequestBody UserRequests.ReviewTeacherRegistrationRequest request) {
        adminService.rejectTeacherRegistration(userId, currentUser.getId(), request);
        return successMessage("教师账号已驳回");
    }
}
