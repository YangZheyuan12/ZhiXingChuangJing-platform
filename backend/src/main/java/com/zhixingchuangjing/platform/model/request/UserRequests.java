package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UserRequests {

    private UserRequests() {
    }

    public record UpdateProfileRequest(
            @Size(max = 64, message = "昵称长度不能超过64个字符")
            String nickname,
            @Size(max = 255, message = "头像地址长度不能超过255个字符")
            String avatarUrl,
            @Size(max = 255, message = "个人简介长度不能超过255个字符")
            String bio
    ) {
    }

    public record UpdatePasswordRequest(
            @NotBlank(message = "原密码不能为空")
            @Size(min = 6, max = 128, message = "原密码长度需为6到128位")
            String oldPassword,
            @NotBlank(message = "新密码不能为空")
            @Size(min = 6, max = 128, message = "新密码长度需为6到128位")
            String newPassword
    ) {
    }
}
