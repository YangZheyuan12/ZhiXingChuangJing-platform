package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.model.request.ClassRequests;
import com.zhixingchuangjing.platform.model.response.ClassResponses;
import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.repository.ClassQueryRepository;
import com.zhixingchuangjing.platform.service.ClassService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassServiceImpl implements ClassService {

    private final ClassQueryRepository classQueryRepository;

    public ClassServiceImpl(ClassQueryRepository classQueryRepository) {
        this.classQueryRepository = classQueryRepository;
    }

    @Override
    public List<CommonResponses.ClassInfo> getMyClasses(Long userId) {
        return classQueryRepository.findMyClasses(userId);
    }

    @Override
    public ClassResponses.ClassDetailResponse getClassDetail(Long classId, Long userId, String role) {
        assertClassAccess(classId, userId, role);
        return classQueryRepository.findClassDetail(classId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40411, "班级不存在"));
    }

    @Override
    public List<ClassResponses.ClassMemberResponse> getClassMembers(Long classId, Long userId, String role) {
        assertClassAccess(classId, userId, role);
        return classQueryRepository.findClassMembers(classId);
    }

    @Override
    public List<ClassResponses.AnnouncementResponse> getClassAnnouncements(Long classId, Long userId, String role) {
        assertClassAccess(classId, userId, role);
        return classQueryRepository.findAnnouncements(classId);
    }

    @Override
    public void createAnnouncement(Long classId, Long userId, String role, ClassRequests.CreateAnnouncementRequest request) {
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40310, "仅教师或管理员可以发布公告");
        }
        if (!"admin".equals(role) && !classQueryRepository.isClassTeacher(classId, userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40311, "当前用户不是该班级教师");
        }
        classQueryRepository.createAnnouncement(classId, userId, request.title(), request.content(), Boolean.TRUE.equals(request.pinned()));
    }

    private void assertClassAccess(Long classId, Long userId, String role) {
        if ("admin".equals(role)) {
            return;
        }
        if (!classQueryRepository.isClassMember(classId, userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40312, "无权访问该班级");
        }
    }
}
