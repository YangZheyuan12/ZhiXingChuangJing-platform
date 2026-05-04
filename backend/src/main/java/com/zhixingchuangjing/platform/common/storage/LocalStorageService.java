package com.zhixingchuangjing.platform.common.storage;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地磁盘存储实现。
 *
 * <p>默认启用：当未显式配置 {@code app.storage.mode} 或其值为 {@code local} 时生效。
 * 文件落在 {@link LocalStorageProperties#getRootDir()} 目录下，
 * 通过 {@code LocalStorageWebConfig} 映射为静态资源供前端直接访问。</p>
 */
@Service
@ConditionalOnProperty(name = "app.storage.mode", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    private final LocalStorageProperties properties;
    private final Path rootPath;

    public LocalStorageService(LocalStorageProperties properties) {
        this.properties = properties;
        this.rootPath = Paths.get(properties.getRootDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootPath);
            log.info("本地存储根目录已就绪: {}", rootPath);
        } catch (Exception ex) {
            log.error("创建本地存储根目录失败 rootDir={}", rootPath, ex);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50013,
                    "初始化本地存储目录失败: " + ex.getMessage());
        }
    }

    @Override
    public String upload(InputStream inputStream,
                         long contentLength,
                         String contentType,
                         String objectName) {
        String safeName = sanitize(objectName);
        Path target = rootPath.resolve(safeName).normalize();
        // 防止路径穿越
        if (!target.startsWith(rootPath)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40064, "非法的文件路径");
        }
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            return buildFileUrl(safeName);
        } catch (Exception ex) {
            log.error("本地存储写文件失败 target={} contentType={} size={}",
                    target, contentType, contentLength, ex);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50010,
                    "素材文件存储失败: " + ex.getMessage());
        }
    }

    private String sanitize(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40065, "文件名不能为空");
        }
        String v = objectName.replace('\\', '/');
        while (v.startsWith("/")) {
            v = v.substring(1);
        }
        return v;
    }

    private String buildFileUrl(String objectName) {
        String prefix = properties.getUrlPrefix() == null ? "/files" : properties.getUrlPrefix();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        String base = properties.getPublicBaseUrl() == null ? "" : properties.getPublicBaseUrl().trim();
        if (!base.isEmpty() && base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + prefix + "/" + objectName;
    }
}
