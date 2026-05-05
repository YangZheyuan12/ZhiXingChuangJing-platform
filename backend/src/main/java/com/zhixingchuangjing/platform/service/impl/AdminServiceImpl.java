package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.entity.UserEntity;
import com.zhixingchuangjing.platform.model.request.UserRequests;
import com.zhixingchuangjing.platform.model.response.AdminResponses;
import com.zhixingchuangjing.platform.repository.AdminUserQueryRepository;
import com.zhixingchuangjing.platform.repository.UserRepository;
import com.zhixingchuangjing.platform.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final AdminUserQueryRepository adminUserQueryRepository;
    private final UserRepository userRepository;

    public AdminServiceImpl(AdminUserQueryRepository adminUserQueryRepository,
                            UserRepository userRepository) {
        this.adminUserQueryRepository = adminUserQueryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AdminResponses.TeacherRegistrationListResponse getTeacherRegistrations(String status) {
        return new AdminResponses.TeacherRegistrationListResponse(
                adminUserQueryRepository.findTeacherRegistrations(status)
        );
    }

    @Override
    @Transactional
    public void approveTeacherRegistration(Long userId, Long adminUserId, UserRequests.ReviewTeacherRegistrationRequest request) {
        UserEntity teacher = loadPendingTeacher(userId);
        teacher.setStatus("active");
        teacher.setReviewRemark(normalizeRemark(request.remark()));
        teacher.setReviewedBy(adminUserId);
        teacher.setReviewedAt(LocalDateTime.now());
        userRepository.save(teacher);
    }

    @Override
    @Transactional
    public void rejectTeacherRegistration(Long userId, Long adminUserId, UserRequests.ReviewTeacherRegistrationRequest request) {
        UserEntity teacher = loadPendingTeacher(userId);
        teacher.setStatus("rejected");
        teacher.setReviewRemark(normalizeRemark(request.remark()));
        teacher.setReviewedBy(adminUserId);
        teacher.setReviewedAt(LocalDateTime.now());
        userRepository.save(teacher);
    }

    private UserEntity loadPendingTeacher(Long userId) {
        UserEntity teacher = userRepository.findByIdAndRole(userId, "teacher")
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40402, "教师注册记录不存在"));
        if (!"pending".equals(teacher.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40027, "当前教师账号不处于待审核状态");
        }
        return teacher;
    }

    private String normalizeRemark(String remark) {
        if (remark == null) {
            return null;
        }
        String normalized = remark.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
