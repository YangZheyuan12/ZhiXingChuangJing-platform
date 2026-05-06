package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.entity.AssetFolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetFolderRepository extends JpaRepository<AssetFolderEntity, Long> {
    List<AssetFolderEntity> findByOwnerId(Long ownerId);
    List<AssetFolderEntity> findByOwnerIdAndParentId(Long ownerId, Long parentId);
    void deleteById(Long id);
}
