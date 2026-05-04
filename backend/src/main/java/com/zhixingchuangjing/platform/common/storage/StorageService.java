package com.zhixingchuangjing.platform.common.storage;

import java.io.InputStream;

/**
 * 素材存储抽象。上层无需关心底层是 MinIO / 本地文件系统 / OSS。
 */
public interface StorageService {

    /**
     * 上传文件并返回可公开访问的 URL。
     *
     * @param inputStream   文件流（调用方负责关闭 MultipartFile 的生命周期，此处只读取内容）
     * @param contentLength 内容长度（字节）
     * @param contentType   MIME 类型，允许 null
     * @param objectName    对象键：相对路径，形如 {@code assets/42/xxxx.png}
     * @return 可直接用于前端 <img src> / <video src> 的完整 URL
     */
    String upload(InputStream inputStream,
                  long contentLength,
                  String contentType,
                  String objectName);
}
