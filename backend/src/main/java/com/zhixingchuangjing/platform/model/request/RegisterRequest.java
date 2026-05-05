package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{4,32}$", message = "账号需为4到32位字母、数字或下划线")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 128, message = "密码长度需为6到128位")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128位")
    private String email;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(student|teacher)$", message = "角色仅支持学生或教师")
    private String role;

    private Long schoolId;

    @Size(max = 64, message = "教工号长度不能超过64位")
    private String teacherNo;

    @NotBlank(message = "验证码标识不能为空")
    private String captchaId;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 8, message = "验证码长度不正确")
    private String captchaCode;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getTeacherNo() {
        return teacherNo;
    }

    public void setTeacherNo(String teacherNo) {
        this.teacherNo = teacherNo;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }
}
