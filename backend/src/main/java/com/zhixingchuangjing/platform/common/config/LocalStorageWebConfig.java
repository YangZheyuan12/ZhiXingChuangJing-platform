package com.zhixingchuangjing.platform.common.config;

import com.zhixingchuangjing.platform.common.storage.LocalStorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件存储启用时，把 {@link LocalStorageProperties#getUrlPrefix()} 映射到磁盘根目录，
 * 让前端能直接通过 {@code http://host:port/files/xxx} 访问上传文件。
 */
@Configuration
@ConditionalOnProperty(name = "app.storage.mode", havingValue = "local", matchIfMissing = true)
public class LocalStorageWebConfig implements WebMvcConfigurer {

    private final LocalStorageProperties properties;

    public LocalStorageWebConfig(LocalStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = Paths.get(properties.getRootDir()).toAbsolutePath().normalize();
        String prefix = properties.getUrlPrefix() == null ? "/files" : properties.getUrlPrefix();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        // Windows 下 Path 可能使用反斜杠，toUri() 统一成 file:/// 形式更可靠
        String location = root.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler(prefix + "**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
