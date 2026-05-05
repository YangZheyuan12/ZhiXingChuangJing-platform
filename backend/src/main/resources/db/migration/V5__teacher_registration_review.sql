SET @schema_name = DATABASE();

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE users ADD COLUMN teacher_no VARCHAR(64) DEFAULT NULL COMMENT ''教师工号'' AFTER mobile',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'users' AND COLUMN_NAME = 'teacher_no'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE users ADD COLUMN review_remark VARCHAR(255) DEFAULT NULL COMMENT ''审核备注'' AFTER status',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'users' AND COLUMN_NAME = 'review_remark'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE users ADD COLUMN reviewed_by BIGINT UNSIGNED DEFAULT NULL COMMENT ''审核人ID'' AFTER review_remark',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'users' AND COLUMN_NAME = 'reviewed_by'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE users ADD COLUMN reviewed_at DATETIME DEFAULT NULL COMMENT ''审核时间'' AFTER reviewed_by',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'users' AND COLUMN_NAME = 'reviewed_at'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
