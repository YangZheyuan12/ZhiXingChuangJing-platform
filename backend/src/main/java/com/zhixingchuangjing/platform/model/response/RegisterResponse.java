package com.zhixingchuangjing.platform.model.response;

public record RegisterResponse(
        Long userId,
        String account,
        String role
) {
}
