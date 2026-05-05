package com.zhixingchuangjing.platform.service;

public interface EmailService {

    boolean isConfigured();

    void sendPasswordResetEmail(String toEmail, String account, String resetUrl);
}
