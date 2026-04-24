package com.zhixingchuangjing.platform.model.response;

public record CaptchaResponse(
        String captchaId,
        String imageData,
        long expiresInSeconds
) {
}
