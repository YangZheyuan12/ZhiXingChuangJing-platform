package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.entity.MaterialPackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialPackRepository extends JpaRepository<MaterialPackEntity, Long> {
    List<MaterialPackEntity> findByOwnerId(Long ownerId);
}
