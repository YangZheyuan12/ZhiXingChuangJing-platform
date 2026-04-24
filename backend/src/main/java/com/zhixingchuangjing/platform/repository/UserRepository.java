package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByAccount(String account);

    Optional<UserEntity> findByIdAndStatus(Long id, String status);
}
