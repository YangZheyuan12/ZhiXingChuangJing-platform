package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.common.security.JwtTokenProvider;
import com.zhixingchuangjing.platform.common.util.PasswordHashUtils;
import com.zhixingchuangjing.platform.entity.UserEntity;
import com.zhixingchuangjing.platform.model.request.LoginRequest;
import com.zhixingchuangjing.platform.model.request.RegisterRequest;
import com.zhixingchuangjing.platform.model.request.UserRequests;
import com.zhixingchuangjing.platform.model.response.CaptchaResponse;
import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.model.response.CurrentUserResponse;
import com.zhixingchuangjing.platform.model.response.LoginUserResponse;
import com.zhixingchuangjing.platform.model.response.LoginResponse;
import com.zhixingchuangjing.platform.model.response.PasswordResetRequestResponse;
import com.zhixingchuangjing.platform.model.response.RegisterResponse;
import com.zhixingchuangjing.platform.repository.UserRepository;
import com.zhixingchuangjing.platform.repository.SchoolQueryRepository;
import com.zhixingchuangjing.platform.service.AuthService;
import com.zhixingchuangjing.platform.service.CaptchaService;
import com.zhixingchuangjing.platform.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final String PASSWORD_RESET_KEY_PREFIX = "auth:password-reset:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CaptchaService captchaService;
    private final SchoolQueryRepository schoolQueryRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final EmailService emailService;
    private final Duration passwordResetTokenTtl;
    private final String passwordResetUrlBase;
    private final boolean allowDevPasswordResetUrl;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           CaptchaService captchaService,
                           SchoolQueryRepository schoolQueryRepository,
                           StringRedisTemplate stringRedisTemplate,
                           EmailService emailService,
                           @Value("${app.auth.password-reset.token-ttl-minutes}") long passwordResetTokenTtlMinutes,
                           @Value("${app.auth.password-reset.reset-url-base}") String passwordResetUrlBase,
                           @Value("${app.auth.password-reset.allow-dev-reset-url:false}") boolean allowDevPasswordResetUrl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.captchaService = captchaService;
        this.schoolQueryRepository = schoolQueryRepository;
        this.stringRedisTemplate = stringRedisTemplate;
        this.emailService = emailService;
        this.passwordResetTokenTtl = Duration.ofMinutes(passwordResetTokenTtlMinutes);
        this.passwordResetUrlBase = passwordResetUrlBase;
        this.allowDevPasswordResetUrl = allowDevPasswordResetUrl;
    }

    @Override
    public CaptchaResponse getCaptcha() {
        return captchaService.createCaptcha();
    }

    @Override
    public List<CommonResponses.SchoolInfo> getSchools() {
        return schoolQueryRepository.findEnabledSchools();
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByAccount(request.getAccount())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, 40101, "账号或密码错误"));

        if (!"active".equals(user.getStatus())) {
            throw buildStatusException(user);
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
        String role = request.getRole().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();
        Long schoolId = request.getSchoolId();
        String teacherNo = normalizeOptionalText(request.getTeacherNo());
        if (userRepository.findByAccount(account).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, 40901, "账号已存在");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, 40902, "邮箱已被使用");
        }
        validateRegisterRequest(role, schoolId, teacherNo);

        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity();
        user.setAccount(account);
        user.setPasswordHash(passwordEncoder.encode(PasswordHashUtils.normalizeTransportPassword(request.getPassword())));
        user.setRole(role);
        user.setSchoolId(schoolId);
        user.setTeacherNo(teacherNo);
        user.setEmail(email);
        user.setRealName(account);
        user.setNickname(account);
        user.setStatus("teacher".equals(role) ? "pending" : "active");
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

    @Override
    @Transactional
    public PasswordResetRequestResponse requestPasswordReset(UserRequests.PasswordResetRequest request) {
        String account = request.account().trim();
        String email = request.email().trim().toLowerCase();
        UserEntity user = userRepository.findByAccount(account)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40403, "账号不存在"));

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40028, "该账号未绑定邮箱，暂时无法找回密码");
        }
        if (!email.equalsIgnoreCase(user.getEmail())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40029, "账号与邮箱不匹配");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        String resetUrl = buildPasswordResetUrl(token);
        stringRedisTemplate.opsForValue().set(buildPasswordResetKey(token), String.valueOf(user.getId()), passwordResetTokenTtl);
        if (emailService.isConfigured()) {
            emailService.sendPasswordResetEmail(email, user.getAccount(), resetUrl);
            return new PasswordResetRequestResponse(passwordResetTokenTtl.toMinutes(), null, "email");
        }
        if (allowDevPasswordResetUrl) {
            return new PasswordResetRequestResponse(passwordResetTokenTtl.toMinutes(), resetUrl, "dev");
        }
        throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50031, "邮件服务未配置，请联系管理员");
    }

    @Override
    @Transactional
    public void confirmPasswordReset(UserRequests.PasswordResetConfirmRequest request) {
        String redisKey = buildPasswordResetKey(request.token().trim());
        String userIdValue = stringRedisTemplate.opsForValue().get(redisKey);
        if (userIdValue == null || userIdValue.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40030, "重置链接已失效或不存在");
        }

        Long userId;
        try {
            userId = Long.valueOf(userIdValue);
        } catch (NumberFormatException ex) {
            stringRedisTemplate.delete(redisKey);
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40031, "重置令牌无效");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40401, "用户不存在"));
        String normalizedPassword = PasswordHashUtils.normalizeTransportPassword(request.newPassword());
        user.setPasswordHash(passwordEncoder.encode(normalizedPassword));
        userRepository.save(user);
        stringRedisTemplate.delete(redisKey);
    }

    private String buildDisplayName(UserEntity user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return user.getRealName();
    }

    private void validateRegisterRequest(String role, Long schoolId, String teacherNo) {
        if ("teacher".equals(role)) {
            if (schoolId == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, 40023, "教师注册必须选择学校");
            }
            if (teacherNo == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, 40024, "教师注册必须填写教工号");
            }
            if (!schoolQueryRepository.existsEnabledSchool(schoolId)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, 40025, "所选学校不存在或已停用");
            }
            return;
        }

        if (schoolId != null || teacherNo != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40026, "学生注册无需填写教师认证信息");
        }
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private BusinessException buildStatusException(UserEntity user) {
        return switch (user.getStatus()) {
            case "pending" -> new BusinessException(HttpStatus.FORBIDDEN, 40302, "教师账号待审核，请联系管理员审批后登录");
            case "rejected" -> new BusinessException(HttpStatus.FORBIDDEN, 40303, "教师账号审核未通过，请联系管理员");
            default -> new BusinessException(HttpStatus.FORBIDDEN, 40301, "账号已被禁用或锁定");
        };
    }

    private String buildPasswordResetKey(String token) {
        return PASSWORD_RESET_KEY_PREFIX + token;
    }

    private String buildPasswordResetUrl(String token) {
        String separator = passwordResetUrlBase.contains("?") ? "&" : "?";
        return passwordResetUrlBase + separator + "token=" + token;
    }
}
