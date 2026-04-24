package com.zhixingchuangjing.platform.model.response;

public class CurrentUserResponse {

    private Long id;
    private String role;
    private String realName;
    private String nickname;
    private String avatarUrl;
    private Long schoolId;

    public CurrentUserResponse() {
    }

    public CurrentUserResponse(Long id, String role, String realName, String nickname, String avatarUrl, Long schoolId) {
        this.id = id;
        this.role = role;
        this.realName = realName;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.schoolId = schoolId;
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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }
}
