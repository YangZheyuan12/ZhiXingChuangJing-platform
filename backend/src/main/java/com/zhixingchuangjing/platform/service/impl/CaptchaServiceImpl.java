package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.model.response.CaptchaResponse;
import com.zhixingchuangjing.platform.service.CaptchaService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final String CAPTCHA_KEY_PREFIX = "auth:captcha:";
    private static final char[] CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate stringRedisTemplate;

    public CaptchaServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public CaptchaResponse createCaptcha() {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String captchaCode = generateCode(4);
        stringRedisTemplate.opsForValue().set(buildRedisKey(captchaId), captchaCode, CAPTCHA_TTL);

        String svg = buildSvg(captchaCode);
        String imageData = "data:image/svg+xml;base64,"
                + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return new CaptchaResponse(captchaId, imageData, CAPTCHA_TTL.toSeconds());
    }

    @Override
    public void validateCaptcha(String captchaId, String captchaCode) {
        String normalizedCaptchaId = captchaId == null ? "" : captchaId.trim();
        String normalizedCaptchaCode = captchaCode == null ? "" : captchaCode.trim().toUpperCase(Locale.ROOT);

        String redisKey = buildRedisKey(normalizedCaptchaId);
        String cachedCaptcha = stringRedisTemplate.opsForValue().get(redisKey);
        stringRedisTemplate.delete(redisKey);

        if (cachedCaptcha == null || cachedCaptcha.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40021, "验证码已失效，请刷新后重试");
        }

        if (!cachedCaptcha.equalsIgnoreCase(normalizedCaptchaCode)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40022, "验证码错误");
        }
    }

    private String buildRedisKey(String captchaId) {
        return CAPTCHA_KEY_PREFIX + captchaId;
    }

    private String generateCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(CAPTCHA_CHARS[RANDOM.nextInt(CAPTCHA_CHARS.length)]);
        }
        return builder.toString();
    }

    private String buildSvg(String captchaCode) {
        StringBuilder builder = new StringBuilder();
        builder.append("""
                <svg xmlns="http://www.w3.org/2000/svg" width="132" height="44" viewBox="0 0 132 44" fill="none">
                  <rect width="132" height="44" rx="8" fill="#FFFFFF"/>
                  <rect x="0.5" y="0.5" width="131" height="43" rx="7.5" stroke="#E5E5E5"/>
                """);

        for (int index = 0; index < 5; index++) {
            int x1 = RANDOM.nextInt(132);
            int y1 = RANDOM.nextInt(44);
            int x2 = RANDOM.nextInt(132);
            int y2 = RANDOM.nextInt(44);
            builder.append("<line x1=\"").append(x1)
                    .append("\" y1=\"").append(y1)
                    .append("\" x2=\"").append(x2)
                    .append("\" y2=\"").append(y2)
                    .append("\" stroke=\"#D4D4D8\" stroke-width=\"1\"/>");
        }

        for (int index = 0; index < captchaCode.length(); index++) {
            int x = 16 + (index * 26);
            int y = 30 + RANDOM.nextInt(5);
            int rotate = RANDOM.nextInt(17) - 8;
            String color = index % 2 == 0 ? "#0F172A" : "#2563EB";
            builder.append("<text x=\"").append(x)
                    .append("\" y=\"").append(y)
                    .append("\" fill=\"").append(color)
                    .append("\" font-family=\"Arial, sans-serif\" font-size=\"22\" font-weight=\"700\" transform=\"rotate(")
                    .append(rotate).append(' ').append(x).append(' ').append(y)
                    .append(")\">")
                    .append(captchaCode.charAt(index))
                    .append("</text>");
        }

        builder.append("</svg>");
        return builder.toString();
    }
}
