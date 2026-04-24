package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.common.util.PasswordHashUtils;
import com.zhixingchuangjing.platform.entity.UserEntity;
import com.zhixingchuangjing.platform.model.request.UserRequests;
import com.zhixingchuangjing.platform.model.response.UserResponses;
import com.zhixingchuangjing.platform.repository.UserQueryRepository;
import com.zhixingchuangjing.platform.repository.UserRepository;
import com.zhixingchuangjing.platform.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           UserQueryRepository userQueryRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userQueryRepository = userQueryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponses.UserProfileResponse getMyProfile(Long userId) {
        return userQueryRepository.findUserProfile(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40401, "用户不存在"));
    }

    @Override
    public UserResponses.UserHomepageResponse getUserHomepage(Long targetUserId) {
        UserResponses.UserProfileResponse profile = userQueryRepository.findUserProfile(targetUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40401, "用户不存在"));
        return new UserResponses.UserHomepageResponse(
                profile,
                userQueryRepository.findHomepageStats(targetUserId),
                userQueryRepository.findHomepagePortfolio(targetUserId)
        );
    }

    @Override
    public java.util.List<UserResponses.PortfolioItemResponse> getMyPortfolio(Long userId) {
        return userQueryRepository.findPortfolio(userId);
    }

    @Override
    @Transactional
    public UserResponses.UserProfileResponse updateMyProfile(Long userId, UserRequests.UpdateProfileRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40401, "用户不存在"));
        user.setNickname(normalizeProfileText(request.nickname(), 64));
        user.setAvatarUrl(normalizeProfileText(request.avatarUrl(), 255));
        user.setBio(normalizeProfileText(request.bio(), 255));
        userRepository.save(user);
        return getMyProfile(userId);
    }

    @Override
    @Transactional
    public void updateMyPassword(Long userId, UserRequests.UpdatePasswordRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40401, "用户不存在"));
        String normalizedOldPassword = PasswordHashUtils.normalizeTransportPassword(request.oldPassword());
        String normalizedNewPassword = PasswordHashUtils.normalizeTransportPassword(request.newPassword());

        boolean matched = passwordEncoder.matches(normalizedOldPassword, user.getPasswordHash());
        if (!matched && !PasswordHashUtils.isSha256Hex(request.oldPassword())) {
            matched = passwordEncoder.matches(request.oldPassword(), user.getPasswordHash());
        }

        if (!matched) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40010, "原密码错误");
        }
        if (normalizedOldPassword.equals(normalizedNewPassword)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40011, "新密码不能与原密码相同");
        }
        user.setPasswordHash(passwordEncoder.encode(normalizedNewPassword));
        userRepository.save(user);
    }

    private String normalizeProfileText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40012, "个人资料字段长度超出限制");
        }
        return normalized;
    }
}
