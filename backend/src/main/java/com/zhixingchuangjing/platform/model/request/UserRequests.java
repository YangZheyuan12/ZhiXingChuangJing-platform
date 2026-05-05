package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public final class UserRequests {

    private UserRequests() {
    }

    public record UpdateProfileRequest(
            @Size(max = 64, message = "昵称长度不能超过64个字符")
            String nickname,
            @Size(max = 255, message = "头像地址长度不能超过255个字符")
            String avatarUrl,
            @Email(message = "邮箱格式不正确")
            @Size(max = 128, message = "邮箱长度不能超过128个字符")
            String email,
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

    public record ReviewTeacherRegistrationRequest(
            @Size(max = 255, message = "审核备注长度不能超过255个字符")
            String remark
    ) {
    }

    public record PasswordResetRequest(
            @NotBlank(message = "账号不能为空")
            String account,
            @NotBlank(message = "邮箱不能为空")
            @Email(message = "邮箱格式不正确")
            String email
    ) {
    }

    public record PasswordResetConfirmRequest(
            @NotBlank(message = "重置令牌不能为空")
            String token,
            @NotBlank(message = "新密码不能为空")
            @Size(min = 6, max = 128, message = "新密码长度需为6到128位")
            String newPassword
    ) {
    }
}
