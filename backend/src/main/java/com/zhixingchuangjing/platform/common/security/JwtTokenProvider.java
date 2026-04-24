package com.zhixingchuangjing.platform.common.security;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserEntity user) {
        return buildToken(user, jwtProperties.getExpirationSeconds(), "access");
    }

    public String generateRefreshToken(UserEntity user) {
        return buildToken(user, jwtProperties.getRefreshExpirationSeconds(), "refresh");
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getExpirationSeconds();
    }

    public Long parseUserId(String token) {
        try {
            return Long.valueOf(parseClaims(token).getSubject());
        } catch (ExpiredJwtException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, 40103, "登录令牌已过期");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, 40104, "登录令牌无效");
        }
    }

    private String buildToken(UserEntity user, long expirationSeconds, String tokenType) {
        Instant now = Instant.now();
        String displayName = user.getNickname() != null && !user.getNickname().isBlank()
                ? user.getNickname()
                : user.getRealName();

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("account", user.getAccount())
                .claim("role", user.getRole())
                .claim("nickname", displayName)
                .claim("tokenType", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
