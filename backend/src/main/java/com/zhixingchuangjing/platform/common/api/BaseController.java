package com.zhixingchuangjing.platform.common.api;

public abstract class BaseController {

    protected <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data, RequestContext.getRequestId());
    }

    protected ApiResponse<Void> successMessage(String message) {
        return ApiResponse.success(message, null, RequestContext.getRequestId());
    }
}
