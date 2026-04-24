package com.zhixingchuangjing.platform.model.response;

public class LoginUserResponse {

    private Long id;
    private String role;
    private String nickname;

    public LoginUserResponse() {
    }

    public LoginUserResponse(Long id, String role, String nickname) {
        this.id = id;
        this.role = role;
        this.nickname = nickname;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
