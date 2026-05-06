package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.entity.MaterialPackAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialPackAssetRepository extends JpaRepository<MaterialPackAssetEntity, Long> {
    List<MaterialPackAssetEntity> findByPackId(Long packId);
    void deleteByPackId(Long packId);
    void deleteByAssetId(Long assetId);
}
