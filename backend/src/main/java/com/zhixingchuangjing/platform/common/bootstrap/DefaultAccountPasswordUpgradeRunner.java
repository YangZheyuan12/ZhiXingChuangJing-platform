package com.zhixingchuangjing.platform.common.bootstrap;

import com.zhixingchuangjing.platform.common.util.PasswordHashUtils;
import com.zhixingchuangjing.platform.entity.UserEntity;
import com.zhixingchuangjing.platform.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DefaultAccountPasswordUpgradeRunner implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final List<String> DEFAULT_ACCOUNTS = List.of("admin", "teacher001", "student001");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultAccountPasswordUpgradeRunner(UserRepository userRepository,
                                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String normalizedPassword = PasswordHashUtils.normalizeTransportPassword(DEFAULT_PASSWORD);

        for (String account : DEFAULT_ACCOUNTS) {
            userRepository.findByAccount(account).ifPresent(user -> upgradeDefaultAccount(user, normalizedPassword));
        }
    }

    private void upgradeDefaultAccount(UserEntity user, String normalizedPassword) {
        if (passwordEncoder.matches(normalizedPassword, user.getPasswordHash())) {
            return;
        }

        if (passwordEncoder.matches(DEFAULT_PASSWORD, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(normalizedPassword));
            userRepository.save(user);
        }
    }
}
