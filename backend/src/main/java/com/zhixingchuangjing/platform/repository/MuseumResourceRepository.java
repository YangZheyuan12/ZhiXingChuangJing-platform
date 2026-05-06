package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.entity.MuseumResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MuseumResourceRepository extends JpaRepository<MuseumResourceEntity, Long>, JpaSpecificationExecutor<MuseumResourceEntity> {
    List<MuseumResourceEntity> findByCategory(String category);
    List<MuseumResourceEntity> findByDynasty(String dynasty);
    List<MuseumResourceEntity> findByMaterial(String material);
    List<MuseumResourceEntity> findByRegion(String region);
    Optional<MuseumResourceEntity> findByExternalId(String externalId);
}
