package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.LoginRequest;
import com.zhixingchuangjing.platform.model.request.RegisterRequest;
import com.zhixingchuangjing.platform.model.response.CaptchaResponse;
import com.zhixingchuangjing.platform.model.response.CurrentUserResponse;
import com.zhixingchuangjing.platform.model.response.LoginResponse;
import com.zhixingchuangjing.platform.model.response.RegisterResponse;

public interface AuthService {

    CaptchaResponse getCaptcha();

    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    CurrentUserResponse getCurrentUser(Long userId);
}
