package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.common.security.JwtTokenProvider;
import com.zhixingchuangjing.platform.common.util.PasswordHashUtils;
import com.zhixingchuangjing.platform.entity.UserEntity;
import com.zhixingchuangjing.platform.model.request.LoginRequest;
import com.zhixingchuangjing.platform.model.request.RegisterRequest;
import com.zhixingchuangjing.platform.model.response.CaptchaResponse;
import com.zhixingchuangjing.platform.model.response.CurrentUserResponse;
import com.zhixingchuangjing.platform.model.response.LoginUserResponse;
import com.zhixingchuangjing.platform.model.response.LoginResponse;
import com.zhixingchuangjing.platform.model.response.RegisterResponse;
import com.zhixingchuangjing.platform.repository.UserRepository;
import com.zhixingchuangjing.platform.service.AuthService;
import com.zhixingchuangjing.platform.service.CaptchaService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CaptchaService captchaService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           CaptchaService captchaService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.captchaService = captchaService;
    }

    @Override
    public CaptchaResponse getCaptcha() {
        return captchaService.createCaptcha();
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByAccount(request.getAccount())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, 40101, "账号或密码错误"));

        if (!"active".equals(user.getStatus())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40301, "账号已被禁用或锁定");
        }

        String normalizedPassword = PasswordHashUtils.normalizeTransportPassword(request.getPassword());
        boolean matched = passwordEncoder.matches(normalizedPassword, user.getPasswordHash());
        if (!matched && !PasswordHashUtils.isSha256Hex(request.getPassword())) {
            matched = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
            if (matched) {
                user.setPasswordHash(passwordEncoder.encode(normalizedPassword));
            }
        }

        if (!matched) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, 40101, "账号或密码错误");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return new LoginResponse(
                jwtTokenProvider.generateAccessToken(user),
                jwtTokenProvider.generateRefreshToken(user),
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                new LoginUserResponse(user.getId(), user.getRole(), buildDisplayName(user))
        );
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        captchaService.validateCaptcha(request.getCaptchaId(), request.getCaptchaCode());

        String account = request.getAccount().trim();
        if (userRepository.findByAccount(account).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, 40901, "账号已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity();
        user.setAccount(account);
        user.setPasswordHash(passwordEncoder.encode(PasswordHashUtils.normalizeTransportPassword(request.getPassword())));
        user.setRole("student");
        user.setRealName(account);
        user.setNickname(account);
        user.setStatus("active");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        UserEntity savedUser = userRepository.save(user);
        return new RegisterResponse(savedUser.getId(), savedUser.getAccount(), savedUser.getRole());
    }

    @Override
    public CurrentUserResponse getCurrentUser(Long userId) {
        UserEntity user = userRepository.findByIdAndStatus(userId, "active")
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, 40102, "登录状态已失效"));

        return new CurrentUserResponse(
                user.getId(),
                user.getRole(),
                user.getRealName(),
                buildDisplayName(user),
                user.getAvatarUrl(),
                user.getSchoolId()
        );
    }

    private String buildDisplayName(UserEntity user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return user.getRealName();
    }
}
