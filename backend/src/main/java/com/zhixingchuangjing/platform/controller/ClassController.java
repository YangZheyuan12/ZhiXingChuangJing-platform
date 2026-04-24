package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.ClassRequests;
import com.zhixingchuangjing.platform.model.response.ClassResponses;
import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.service.ClassService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/classes")
public class ClassController extends BaseController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    public ApiResponse<List<CommonResponses.ClassInfo>> getMyClasses(@AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(classService.getMyClasses(currentUser.getId()));
    }

    @GetMapping("/{classId}")
    public ApiResponse<ClassResponses.ClassDetailResponse> getClassDetail(@PathVariable Long classId,
                                                                          @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(classService.getClassDetail(classId, currentUser.getId(), currentUser.getRole()));
    }

    @GetMapping("/{classId}/members")
    public ApiResponse<List<ClassResponses.ClassMemberResponse>> getClassMembers(@PathVariable Long classId,
                                                                                 @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(classService.getClassMembers(classId, currentUser.getId(), currentUser.getRole()));
    }

    @GetMapping("/{classId}/announcements")
    public ApiResponse<List<ClassResponses.AnnouncementResponse>> getClassAnnouncements(@PathVariable Long classId,
                                                                                        @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(classService.getClassAnnouncements(classId, currentUser.getId(), currentUser.getRole()));
    }

    @PostMapping("/{classId}/announcements")
    public ApiResponse<Void> createAnnouncement(@PathVariable Long classId,
                                                @AuthenticationPrincipal SecurityUserDetails currentUser,
                                                @Valid @RequestBody ClassRequests.CreateAnnouncementRequest request) {
        classService.createAnnouncement(classId, currentUser.getId(), currentUser.getRole(), request);
        return successMessage("公告发布成功");
    }
}
