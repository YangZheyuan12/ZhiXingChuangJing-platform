package com.zhixingchuangjing.platform.common.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 本地磁盘存储配置。对应 application.yml 下 {@code app.storage.local.*}。
 */
@ConfigurationProperties(prefix = "app.storage.local")
public class LocalStorageProperties {

    /**
     * 文件落盘根目录。支持相对/绝对路径；相对路径以后端进程工作目录为基准。
     */
    private String rootDir = "./storage";

    /**
     * 静态资源 URL 前缀，会与 {@link #publicBaseUrl} 拼接后返回给前端。
     */
    private String urlPrefix = "/files";

    /**
     * 公开访问的基础 URL（形如 {@code http://localhost:8080}）。
     * 留空则返回相对路径，一般部署场景必须填写。
     */
    private String publicBaseUrl = "";

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }
}
