-- 资料库功能增强 - 数据库迁移脚本
-- 执行时间：2026-05-06

-- 1. 文博资源表新增字段（朝代、材质、地区）
ALTER TABLE museum_resources 
ADD COLUMN dynasty VARCHAR(64) COMMENT '朝代' AFTER category,
ADD COLUMN material VARCHAR(64) COMMENT '材质' AFTER dynasty,
ADD COLUMN region VARCHAR(64) COMMENT '地区' AFTER material;

-- 2. 素材文件夹表
CREATE TABLE IF NOT EXISTS asset_folders (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT UNSIGNED NOT NULL COMMENT '所有者 ID',
    parent_id BIGINT UNSIGNED COMMENT '父文件夹 ID',
    folder_name VARCHAR(128) NOT NULL COMMENT '文件夹名称',
    folder_path VARCHAR(512) COMMENT '文件夹路径',
    sort_no INT DEFAULT 0 COMMENT '排序号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_owner_parent (owner_id, parent_id),
    INDEX idx_folder_path (folder_path(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材文件夹表';

-- 3. 素材标签表
CREATE TABLE IF NOT EXISTS asset_tags (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT UNSIGNED NOT NULL COMMENT '素材 ID',
    owner_id BIGINT UNSIGNED NOT NULL COMMENT '所有者 ID',
    tag_name VARCHAR(64) NOT NULL COMMENT '标签名称',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_asset_tag (asset_id, tag_name),
    INDEX idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材标签表';

-- 4. 素材版本表
CREATE TABLE IF NOT EXISTS asset_versions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT UNSIGNED NOT NULL COMMENT '素材 ID',
    version_no INT UNSIGNED NOT NULL COMMENT '版本号',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_url VARCHAR(255) NOT NULL COMMENT '文件地址',
    file_size BIGINT UNSIGNED COMMENT '文件大小',
    checksum_md5 VARCHAR(32) COMMENT 'MD5 校验值',
    version_note VARCHAR(255) COMMENT '版本备注',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_asset_version (asset_id, version_no),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材版本表';

-- 5. 素材包表
CREATE TABLE IF NOT EXISTS material_packs (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT UNSIGNED NOT NULL COMMENT '所有者 ID',
    pack_name VARCHAR(128) NOT NULL COMMENT '素材包名称',
    description VARCHAR(500) COMMENT '描述',
    asset_count INT UNSIGNED DEFAULT 0 COMMENT '素材数量',
    is_public TINYINT(1) DEFAULT 0 COMMENT '是否公开',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材包表';

-- 6. 素材包 - 素材关联表
CREATE TABLE IF NOT EXISTS material_pack_assets (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    pack_id BIGINT UNSIGNED NOT NULL COMMENT '素材包 ID',
    asset_id BIGINT UNSIGNED NOT NULL COMMENT '素材 ID',
    sort_no INT DEFAULT 0 COMMENT '排序号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pack_asset (pack_id, asset_id),
    INDEX idx_pack (pack_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材包 - 素材关联表';

-- 7. media_assets 表新增字段
ALTER TABLE media_assets 
ADD COLUMN folder VARCHAR(255) COMMENT '文件夹路径' AFTER file_size,
ADD COLUMN checksum_sha256 VARCHAR(64) COMMENT 'SHA256 校验值' AFTER checksum_md5;

-- 8. 添加外键约束（可选，根据实际情况）
-- ALTER TABLE asset_folders ADD CONSTRAINT fk_folder_owner FOREIGN KEY (owner_id) REFERENCES users(id);
-- ALTER TABLE asset_tags ADD CONSTRAINT fk_tag_asset FOREIGN KEY (asset_id) REFERENCES media_assets(id);
-- ALTER TABLE asset_versions ADD CONSTRAINT fk_version_asset FOREIGN KEY (asset_id) REFERENCES media_assets(id);
-- ALTER TABLE material_packs ADD CONSTRAINT fk_pack_owner FOREIGN KEY (owner_id) REFERENCES users(id);
-- ALTER TABLE material_pack_assets ADD CONSTRAINT fk_pack_asset_pack FOREIGN KEY (pack_id) REFERENCES material_packs(id);
-- ALTER TABLE material_pack_assets ADD CONSTRAINT fk_pack_asset_asset FOREIGN KEY (asset_id) REFERENCES media_assets(id);
