package com.zhixingchuangjing.platform.common.security;

import com.zhixingchuangjing.platform.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SecurityUserDetails implements UserDetails {

    private final Long id;
    private final String account;
    private final String password;
    private final String role;
    private final String nickname;
    private final String status;

    public SecurityUserDetails(Long id, String account, String password, String role, String nickname, String status) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.role = role;
        this.nickname = nickname;
        this.status = status;
    }

    public static SecurityUserDetails from(UserEntity user) {
        String displayName = user.getNickname() != null && !user.getNickname().isBlank()
                ? user.getNickname()
                : user.getRealName();
        return new SecurityUserDetails(
                user.getId(),
                user.getAccount(),
                user.getPasswordHash(),
                user.getRole(),
                displayName,
                user.getStatus()
        );
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return account;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"locked".equals(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "active".equals(status);
    }
}
