package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.LoginRequest;
import com.zhixingchuangjing.platform.model.request.RegisterRequest;
import com.zhixingchuangjing.platform.model.request.UserRequests;
import com.zhixingchuangjing.platform.model.response.CaptchaResponse;
import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.model.response.CurrentUserResponse;
import com.zhixingchuangjing.platform.model.response.LoginResponse;
import com.zhixingchuangjing.platform.model.response.PasswordResetRequestResponse;
import com.zhixingchuangjing.platform.model.response.RegisterResponse;

import java.util.List;

public interface AuthService {

    CaptchaResponse getCaptcha();

    List<CommonResponses.SchoolInfo> getSchools();

    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    PasswordResetRequestResponse requestPasswordReset(UserRequests.PasswordResetRequest request);

    void confirmPasswordReset(UserRequests.PasswordResetConfirmRequest request);

    CurrentUserResponse getCurrentUser(Long userId);
}
