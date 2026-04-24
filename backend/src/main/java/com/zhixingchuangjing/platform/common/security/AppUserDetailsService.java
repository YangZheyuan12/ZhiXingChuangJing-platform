package com.zhixingchuangjing.platform.common.security;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.entity.UserEntity;
import com.zhixingchuangjing.platform.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByAccount(username)
                .map(SecurityUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    }

    public SecurityUserDetails loadUserById(Long userId) {
        UserEntity user = userRepository.findByIdAndStatus(userId, "active")
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, 40102, "登录状态已失效"));
        return SecurityUserDetails.from(user);
    }
}
