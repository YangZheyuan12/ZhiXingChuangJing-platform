package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final String senderAddress;
    private final String senderName;

    public EmailServiceImpl(JavaMailSender javaMailSender,
                            @Value("${spring.mail.username:}") String senderAddress,
                            @Value("${app.auth.password-reset.sender-name}") String senderName) {
        this.javaMailSender = javaMailSender;
        this.senderAddress = senderAddress;
        this.senderName = senderName;
    }

    @Override
    public boolean isConfigured() {
        return senderAddress != null && !senderAddress.isBlank();
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String account, String resetUrl) {
        if (!isConfigured()) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50031, "邮件服务未配置，请联系管理员");
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(senderAddress, senderName);
            helper.setTo(toEmail);
            helper.setSubject("知行创境密码重置");
            helper.setText(buildPasswordResetContent(account, resetUrl), false);
            javaMailSender.send(message);
        } catch (MessagingException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50032, "密码重置邮件发送失败");
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50033, "邮件服务调用失败");
        }
    }

    private String buildPasswordResetContent(String account, String resetUrl) {
        return """
                你好，%s：

                你正在申请重置知行创境平台密码。
                请在 30 分钟内打开以下链接完成重置：
                %s

                如果这不是你的操作，请忽略本邮件。
                """.formatted(account, resetUrl);
    }
}
