package com.zhixingchuangjing.platform.model.response;

public record PasswordResetRequestResponse(
        long expiresInMinutes,
        String resetUrl,
        String deliveryChannel
) {
}
