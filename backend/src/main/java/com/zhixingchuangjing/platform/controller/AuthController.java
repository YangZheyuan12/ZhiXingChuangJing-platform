package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.LoginRequest;
import com.zhixingchuangjing.platform.model.request.RegisterRequest;
import com.zhixingchuangjing.platform.model.request.UserRequests;
import com.zhixingchuangjing.platform.model.response.CaptchaResponse;
import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.model.response.CurrentUserResponse;
import com.zhixingchuangjing.platform.model.response.LoginResponse;
import com.zhixingchuangjing.platform.model.response.PasswordResetRequestResponse;
import com.zhixingchuangjing.platform.model.response.RegisterResponse;
import com.zhixingchuangjing.platform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> getCaptcha() {
        return success(authService.getCaptcha());
    }

    @GetMapping("/schools")
    public ApiResponse<java.util.List<CommonResponses.SchoolInfo>> getSchools() {
        return success(authService.getSchools());
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return success(authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return success(authService.register(request));
    }

    @PostMapping("/password-reset/request")
    public ApiResponse<PasswordResetRequestResponse> requestPasswordReset(@Valid @RequestBody UserRequests.PasswordResetRequest request) {
        return success(authService.requestPasswordReset(request));
    }

    @PostMapping("/password-reset/confirm")
    public ApiResponse<Void> confirmPasswordReset(@Valid @RequestBody UserRequests.PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return successMessage("密码重置成功");
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> getCurrentUser(@AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(authService.getCurrentUser(currentUser.getId()));
    }
}
