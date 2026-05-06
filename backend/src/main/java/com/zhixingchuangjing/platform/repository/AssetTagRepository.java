package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.entity.AssetTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetTagRepository extends JpaRepository<AssetTagEntity, Long> {
    List<AssetTagEntity> findByAssetId(Long assetId);
    List<AssetTagEntity> findByOwnerId(Long ownerId);
    void deleteByAssetId(Long assetId);
}
