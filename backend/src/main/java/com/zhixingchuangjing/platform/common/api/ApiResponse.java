package com.zhixingchuangjing.platform.common.api;

public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;
    private final String requestId;

    private ApiResponse(int code, String message, T data, String requestId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
    }

    public static <T> ApiResponse<T> success(T data, String requestId) {
        return new ApiResponse<>(0, "ok", data, requestId);
    }

    public static <T> ApiResponse<T> success(String message, T data, String requestId) {
        return new ApiResponse<>(0, message, data, requestId);
    }

    public static <T> ApiResponse<T> failure(int code, String message, T data, String requestId) {
        return new ApiResponse<>(code, message, data, requestId);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getRequestId() {
        return requestId;
    }
}
