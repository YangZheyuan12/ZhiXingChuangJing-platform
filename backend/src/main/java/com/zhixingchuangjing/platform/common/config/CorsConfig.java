package com.zhixingchuangjing.platform.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    /**
     * 暴露 {@link CorsConfigurationSource} Bean，使 Spring Security 6 的
     * {@code http.cors(Customizer.withDefaults())} 能自动接入；同时保留 Spring MVC
     * 对静态资源的 CORS 处理。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String allowedOrigins,
            @Value("${app.cors.allowed-origin-patterns:}") String allowedOriginPatterns) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(parseValues(allowedOrigins));
        configuration.setAllowedOriginPatterns(parseValues(allowedOriginPatterns));
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.addExposedHeader("X-Request-Id");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> parseValues(String values) {
        return Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
