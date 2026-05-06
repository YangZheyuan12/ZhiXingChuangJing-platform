-- =====================================================
-- 资料库功能增强 - 数据库初始化脚本
-- =====================================================
-- 使用说明：
-- 1. 确保数据库 zhixingchuangjing 已创建
-- 2. 执行此脚本创建所有必需的表和字段
-- 3. 执行时间：约 30 秒
-- =====================================================

USE zhixingchuangjing;

-- =====================================================
-- 1. 创建素材文件夹表
-- =====================================================
CREATE TABLE IF NOT EXISTS asset_folders (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT UNSIGNED NOT NULL COMMENT '所有者 ID',
    parent_id BIGINT UNSIGNED COMMENT '父文件夹 ID',
    folder_name VARCHAR(128) NOT NULL COMMENT '文件夹名称',
    folder_path VARCHAR(512) COMMENT '文件夹路径',
    sort_no INT DEFAULT 0 COMMENT '排序号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_owner_parent (owner_id, parent_id),
    INDEX idx_folder_path (folder_path(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材文件夹表';

-- =====================================================
-- 2. 创建素材标签表
-- =====================================================
CREATE TABLE IF NOT EXISTS asset_tags (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT UNSIGNED NOT NULL COMMENT '素材 ID',
    owner_id BIGINT UNSIGNED NOT NULL COMMENT '所有者 ID',
    tag_name VARCHAR(64) NOT NULL COMMENT '标签名称',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_asset_tag (asset_id, tag_name),
    INDEX idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材标签表';

-- =====================================================
-- 3. 创建素材版本表
-- =====================================================
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
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_asset_version (asset_id, version_no),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材版本表';

-- =====================================================
-- 4. 创建素材包表
-- =====================================================
CREATE TABLE IF NOT EXISTS material_packs (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT UNSIGNED NOT NULL COMMENT '所有者 ID',
    pack_name VARCHAR(128) NOT NULL COMMENT '素材包名称',
    description VARCHAR(500) COMMENT '描述',
    asset_count INT UNSIGNED DEFAULT 0 COMMENT '素材数量',
    is_public TINYINT(1) DEFAULT 0 COMMENT '是否公开',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材包表';

-- =====================================================
-- 5. 创建素材包 - 素材关联表
-- =====================================================
CREATE TABLE IF NOT EXISTS material_pack_assets (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    pack_id BIGINT UNSIGNED NOT NULL COMMENT '素材包 ID',
    asset_id BIGINT UNSIGNED NOT NULL COMMENT '素材 ID',
    sort_no INT DEFAULT 0 COMMENT '排序号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_pack_asset (pack_id, asset_id),
    INDEX idx_pack (pack_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材包 - 素材关联表';

-- =====================================================
-- 6. 文博资源表新增字段（朝代、材质、地区）
-- =====================================================
-- 检查字段是否已存在，不存在则添加
SET @dbname = DATABASE();
SET @tablename = 'museum_resources';
SET @columnname = 'dynasty';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(64) COMMENT \'朝代\' AFTER category')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @columnname = 'material';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(64) COMMENT \'材质\' AFTER dynasty')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @columnname = 'region';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(64) COMMENT \'地区\' AFTER material')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- 7. media_assets 表新增字段
-- =====================================================
SET @tablename = 'media_assets';
SET @columnname = 'folder';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(255) COMMENT \'文件夹路径\' AFTER file_size')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @columnname = 'checksum_sha256';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(64) COMMENT \'SHA256 校验值\' AFTER checksum_md5')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- 完成提示
-- =====================================================
SELECT '✅ 数据库迁移完成！共创建 5 张新表，添加 5 个新字段。' AS result;
