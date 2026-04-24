package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.ClassRequests;
import com.zhixingchuangjing.platform.model.response.ClassResponses;
import com.zhixingchuangjing.platform.model.response.CommonResponses;

import java.util.List;

public interface ClassService {

    List<CommonResponses.ClassInfo> getMyClasses(Long userId);

    ClassResponses.ClassDetailResponse getClassDetail(Long classId, Long userId, String role);

    List<ClassResponses.ClassMemberResponse> getClassMembers(Long classId, Long userId, String role);

    List<ClassResponses.AnnouncementResponse> getClassAnnouncements(Long classId, Long userId, String role);

    void createAnnouncement(Long classId, Long userId, String role, ClassRequests.CreateAnnouncementRequest request);
}
