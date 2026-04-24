package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.NotificationRequests;
import com.zhixingchuangjing.platform.model.response.NotificationResponses;
import com.zhixingchuangjing.platform.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/notifications")
public class NotificationController extends BaseController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponses.NotificationResponse>> getNotifications(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @RequestParam(required = false) String readStatus,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return success(notificationService.getNotifications(currentUser.getId(), readStatus, page, pageSize));
    }

    @PostMapping("/read")
    public ApiResponse<Void> readNotifications(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody NotificationRequests.ReadNotificationsRequest request) {
        notificationService.readNotifications(currentUser.getId(), request);
        return successMessage("通知已标记为已读");
    }
}
