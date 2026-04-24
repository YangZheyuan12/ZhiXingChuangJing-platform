package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.response.CaptchaResponse;

public interface CaptchaService {

    CaptchaResponse createCaptcha();

    void validateCaptcha(String captchaId, String captchaCode);
}
