package com.zhixingchuangjing.platform.model.response;

public class LoginResponse {

    private String token;
    private String refreshToken;
    private long expiresIn;
    private LoginUserResponse user;

    public LoginResponse() {
    }

    public LoginResponse(String token, String refreshToken, long expiresIn, LoginUserResponse user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public LoginUserResponse getUser() {
        return user;
    }

    public void setUser(LoginUserResponse user) {
        this.user = user;
    }
}
