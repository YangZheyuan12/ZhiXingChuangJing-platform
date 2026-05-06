package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.entity.AssetVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetVersionRepository extends JpaRepository<AssetVersionEntity, Long> {
    List<AssetVersionEntity> findByAssetIdOrderByVersionNoDesc(Long assetId);
    AssetVersionEntity findTopByAssetIdOrderByVersionNoDesc(Long assetId);
}
