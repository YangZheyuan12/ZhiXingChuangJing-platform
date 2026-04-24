package com.zhixingchuangjing.platform.common.config;

import com.zhixingchuangjing.platform.common.security.JwtProperties;
import com.zhixingchuangjing.platform.common.storage.MinioStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, MinioStorageProperties.class})
public class SecurityPropertiesConfig {
}
