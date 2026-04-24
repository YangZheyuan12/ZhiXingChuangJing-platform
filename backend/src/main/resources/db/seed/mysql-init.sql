SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS zhixingchuangjing
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE zhixingchuangjing;

CREATE TABLE IF NOT EXISTS schools (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  school_name VARCHAR(128) NOT NULL COMMENT '学校名称',
  school_code VARCHAR(64) DEFAULT NULL COMMENT '学校编码',
  province VARCHAR(64) DEFAULT NULL COMMENT '省份',
  city VARCHAR(64) DEFAULT NULL COMMENT '城市',
  district VARCHAR(64) DEFAULT NULL COMMENT '区县',
  status VARCHAR(20) NOT NULL DEFAULT 'enabled' COMMENT '状态：enabled/disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_school_code (school_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学校表';

CREATE TABLE IF NOT EXISTS users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  school_id BIGINT UNSIGNED DEFAULT NULL COMMENT '学校ID',
  account VARCHAR(64) NOT NULL COMMENT '登录账号',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  role VARCHAR(20) NOT NULL COMMENT '角色：student/teacher/parent/admin/visitor',
  real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
  nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像',
  gender VARCHAR(10) DEFAULT NULL COMMENT '性别',
  email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  mobile VARCHAR(32) DEFAULT NULL COMMENT '手机号',
  bio VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
  last_login_at DATETIME DEFAULT NULL COMMENT '最近登录时间',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active/inactive/locked',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_account (account),
  UNIQUE KEY uk_users_email (email),
  UNIQUE KEY uk_users_mobile (mobile),
  KEY idx_users_role_status (role, status),
  KEY idx_users_school (school_id),
  CONSTRAINT fk_users_school FOREIGN KEY (school_id) REFERENCES schools (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS classes (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  school_id BIGINT UNSIGNED NOT NULL COMMENT '学校ID',
  class_name VARCHAR(128) NOT NULL COMMENT '班级名称',
  grade_level VARCHAR(32) DEFAULT NULL COMMENT '年级',
  academic_year VARCHAR(20) DEFAULT NULL COMMENT '学年',
  head_teacher_id BIGINT UNSIGNED DEFAULT NULL COMMENT '班主任ID',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active/inactive',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_classes_school_grade (school_id, grade_level),
  KEY idx_classes_head_teacher (head_teacher_id),
  CONSTRAINT fk_classes_school FOREIGN KEY (school_id) REFERENCES schools (id),
  CONSTRAINT fk_classes_head_teacher FOREIGN KEY (head_teacher_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

CREATE TABLE IF NOT EXISTS class_members (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  class_id BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  member_role VARCHAR(20) NOT NULL DEFAULT 'student' COMMENT '成员角色：student/teacher/assistant',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active/inactive',
  PRIMARY KEY (id),
  UNIQUE KEY uk_class_user (class_id, user_id),
  KEY idx_class_members_user (user_id),
  CONSTRAINT fk_class_members_class FOREIGN KEY (class_id) REFERENCES classes (id),
  CONSTRAINT fk_class_members_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级成员关系表';

CREATE TABLE IF NOT EXISTS announcements (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  class_id BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
  publisher_id BIGINT UNSIGNED NOT NULL COMMENT '发布人ID',
  title VARCHAR(128) NOT NULL COMMENT '公告标题',
  content TEXT NOT NULL COMMENT '公告内容',
  pinned TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
  published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_announcements_class_time (class_id, published_at),
  KEY idx_announcements_publisher (publisher_id),
  CONSTRAINT fk_announcements_class FOREIGN KEY (class_id) REFERENCES classes (id),
  CONSTRAINT fk_announcements_publisher FOREIGN KEY (publisher_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级公告表';

CREATE TABLE IF NOT EXISTS tasks (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  creator_id BIGINT UNSIGNED NOT NULL COMMENT '创建教师ID',
  title VARCHAR(128) NOT NULL COMMENT '任务标题',
  cover_url VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  description TEXT NOT NULL COMMENT '任务描述',
  evaluation_criteria TEXT DEFAULT NULL COMMENT '评价标准',
  start_time DATETIME DEFAULT NULL COMMENT '开始时间',
  due_time DATETIME DEFAULT NULL COMMENT '截止时间',
  status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft/published/closed/archived',
  excellent_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '优秀作品数量',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_tasks_creator_status (creator_id, status),
  KEY idx_tasks_due_time (due_time),
  CONSTRAINT fk_tasks_creator FOREIGN KEY (creator_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

CREATE TABLE IF NOT EXISTS task_target_classes (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
  class_id BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_task_class (task_id, class_id),
  KEY idx_task_target_class (class_id),
  CONSTRAINT fk_task_target_task FOREIGN KEY (task_id) REFERENCES tasks (id),
  CONSTRAINT fk_task_target_class FOREIGN KEY (class_id) REFERENCES classes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务投放班级表';

CREATE TABLE IF NOT EXISTS task_materials (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
  title VARCHAR(128) NOT NULL COMMENT '资料标题',
  material_type VARCHAR(20) NOT NULL COMMENT '类型：image/video/audio/link/document',
  file_url VARCHAR(255) DEFAULT NULL COMMENT '文件地址',
  external_url VARCHAR(255) DEFAULT NULL COMMENT '外链地址',
  description VARCHAR(255) DEFAULT NULL COMMENT '描述',
  sort_no INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_task_materials_task (task_id, sort_no),
  CONSTRAINT fk_task_materials_task FOREIGN KEY (task_id) REFERENCES tasks (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务背景资料表';

CREATE TABLE IF NOT EXISTS media_assets (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  owner_id BIGINT UNSIGNED NOT NULL COMMENT '上传人ID',
  asset_type VARCHAR(20) NOT NULL COMMENT '类型：image/video/audio/document/model',
  source_type VARCHAR(20) NOT NULL DEFAULT 'upload' COMMENT '来源：upload/platform/museum',
  file_name VARCHAR(255) NOT NULL COMMENT '文件名',
  file_url VARCHAR(255) NOT NULL COMMENT '文件地址',
  file_ext VARCHAR(20) DEFAULT NULL COMMENT '扩展名',
  mime_type VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
  file_size BIGINT UNSIGNED DEFAULT NULL COMMENT '文件大小',
  width INT UNSIGNED DEFAULT NULL COMMENT '宽',
  height INT UNSIGNED DEFAULT NULL COMMENT '高',
  duration_seconds INT UNSIGNED DEFAULT NULL COMMENT '时长',
  checksum_md5 VARCHAR(32) DEFAULT NULL COMMENT '文件MD5',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_media_assets_owner (owner_id, created_at),
  KEY idx_media_assets_type (asset_type, source_type),
  CONSTRAINT fk_media_assets_owner FOREIGN KEY (owner_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材资源表';

CREATE TABLE IF NOT EXISTS exhibitions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联任务ID',
  owner_id BIGINT UNSIGNED NOT NULL COMMENT '发起人ID',
  title VARCHAR(128) NOT NULL COMMENT '展厅标题',
  cover_url VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  summary VARCHAR(500) DEFAULT NULL COMMENT '摘要',
  group_name VARCHAR(128) DEFAULT NULL COMMENT '小组名称',
  status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft/published/archived',
  visibility VARCHAR(20) NOT NULL DEFAULT 'class' COMMENT '可见性：private/class/public',
  latest_version_no INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '最新版本号',
  published_version_no INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已发布版本号',
  featured_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否加精推荐',
  featured_reason VARCHAR(255) DEFAULT NULL COMMENT '推荐理由',
  featured_at DATETIME DEFAULT NULL COMMENT '推荐时间',
  view_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览数',
  like_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  favorite_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
  comment_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
  published_at DATETIME DEFAULT NULL COMMENT '发布时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_exhibitions_task_status (task_id, status),
  KEY idx_exhibitions_featured (featured_flag, published_at),
  KEY idx_exhibitions_owner (owner_id, updated_at),
  CONSTRAINT fk_exhibitions_task FOREIGN KEY (task_id) REFERENCES tasks (id),
  CONSTRAINT fk_exhibitions_owner FOREIGN KEY (owner_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展厅主表';

CREATE TABLE IF NOT EXISTS exhibition_members (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  exhibition_id BIGINT UNSIGNED NOT NULL COMMENT '展厅ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  member_role VARCHAR(20) NOT NULL DEFAULT 'editor' COMMENT '角色：owner/editor/viewer',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active/inactive',
  PRIMARY KEY (id),
  UNIQUE KEY uk_exhibition_user (exhibition_id, user_id),
  KEY idx_exhibition_members_user (user_id),
  CONSTRAINT fk_exhibition_members_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id),
  CONSTRAINT fk_exhibition_members_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展厅协作成员表';

CREATE TABLE IF NOT EXISTS exhibition_versions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  exhibition_id BIGINT UNSIGNED NOT NULL COMMENT '展厅ID',
  version_no INT UNSIGNED NOT NULL COMMENT '版本号',
  save_type VARCHAR(20) NOT NULL DEFAULT 'manual' COMMENT '保存类型：manual/autosave/publish',
  version_note VARCHAR(255) DEFAULT NULL COMMENT '版本说明',
  canvas_width INT UNSIGNED NOT NULL DEFAULT 1920 COMMENT '画布宽',
  canvas_height INT UNSIGNED NOT NULL DEFAULT 1080 COMMENT '画布高',
  canvas_background VARCHAR(64) DEFAULT NULL COMMENT '画布背景',
  zoom_ratio DECIMAL(6,2) NOT NULL DEFAULT 1.00 COMMENT '缩放比例',
  element_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '元素数量',
  version_data JSON NOT NULL COMMENT '版本快照JSON',
  created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_exhibition_version (exhibition_id, version_no),
  KEY idx_versions_exhibition_created (exhibition_id, created_at),
  KEY idx_versions_creator (created_by),
  CONSTRAINT fk_exhibition_versions_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id),
  CONSTRAINT fk_exhibition_versions_creator FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展厅版本快照表';

CREATE TABLE IF NOT EXISTS exhibition_components (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  version_id BIGINT UNSIGNED NOT NULL COMMENT '版本ID',
  component_key VARCHAR(64) NOT NULL COMMENT '组件唯一键',
  component_type VARCHAR(32) NOT NULL COMMENT '组件类型',
  title VARCHAR(128) DEFAULT NULL COMMENT '组件标题',
  asset_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联素材ID',
  x DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'X坐标',
  y DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Y坐标',
  width DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '宽度',
  height DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '高度',
  z_index INT NOT NULL DEFAULT 0 COMMENT '层级',
  rotation DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '旋转角度',
  style_json JSON DEFAULT NULL COMMENT '样式配置',
  content_json JSON DEFAULT NULL COMMENT '内容配置',
  interaction_json JSON DEFAULT NULL COMMENT '交互配置',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_version_component (version_id, component_key),
  KEY idx_components_type (component_type),
  KEY idx_components_asset (asset_id),
  CONSTRAINT fk_exhibition_components_version FOREIGN KEY (version_id) REFERENCES exhibition_versions (id),
  CONSTRAINT fk_exhibition_components_asset FOREIGN KEY (asset_id) REFERENCES media_assets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展厅组件明细表';

CREATE TABLE IF NOT EXISTS digital_humans (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  exhibition_id BIGINT UNSIGNED NOT NULL COMMENT '展厅ID',
  name VARCHAR(64) NOT NULL COMMENT '数字人名称',
  avatar_2d_url VARCHAR(255) DEFAULT NULL COMMENT '2D形象地址',
  model_3d_url VARCHAR(255) DEFAULT NULL COMMENT '3D模型地址',
  persona VARCHAR(255) DEFAULT NULL COMMENT '角色设定',
  voice_type VARCHAR(64) DEFAULT NULL COMMENT '音色类型',
  story_script LONGTEXT DEFAULT NULL COMMENT '故事脚本',
  story_timeline JSON DEFAULT NULL COMMENT '故事时间线',
  sort_no INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '排序',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active/inactive',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_digital_human_exhibition (exhibition_id),
  CONSTRAINT fk_digital_humans_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI数字人表';

CREATE TABLE IF NOT EXISTS museum_providers (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  provider_code VARCHAR(64) NOT NULL COMMENT '机构编码',
  provider_name VARCHAR(128) NOT NULL COMMENT '机构名称',
  api_base_url VARCHAR(255) DEFAULT NULL COMMENT 'API基础地址',
  status VARCHAR(20) NOT NULL DEFAULT 'enabled' COMMENT '状态：enabled/disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_provider_code (provider_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博物馆资源提供方表';

CREATE TABLE IF NOT EXISTS museum_resources (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  provider_id BIGINT UNSIGNED NOT NULL COMMENT '提供方ID',
  external_id VARCHAR(128) NOT NULL COMMENT '外部资源ID',
  resource_type VARCHAR(20) NOT NULL COMMENT '资源类型：image/video/audio/model/document',
  category VARCHAR(64) NOT NULL COMMENT '资源分类',
  title VARCHAR(255) NOT NULL COMMENT '资源标题',
  subtitle VARCHAR(255) DEFAULT NULL COMMENT '副标题',
  museum_name VARCHAR(128) DEFAULT NULL COMMENT '博物馆名称',
  cover_url VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  detail_url VARCHAR(255) DEFAULT NULL COMMENT '详情页地址',
  description TEXT DEFAULT NULL COMMENT '资源描述',
  tags_json JSON DEFAULT NULL COMMENT '标签JSON',
  metadata_json JSON DEFAULT NULL COMMENT '扩展元数据JSON',
  raw_payload JSON DEFAULT NULL COMMENT '原始返回JSON',
  cache_status VARCHAR(20) NOT NULL DEFAULT 'fresh' COMMENT '缓存状态：fresh/expired/error',
  synced_at DATETIME DEFAULT NULL COMMENT '同步时间',
  cache_expire_at DATETIME DEFAULT NULL COMMENT '缓存过期时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_provider_external (provider_id, external_id),
  KEY idx_museum_category (category, cache_status),
  KEY idx_museum_expire (cache_expire_at),
  CONSTRAINT fk_museum_resources_provider FOREIGN KEY (provider_id) REFERENCES museum_providers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博物馆资源缓存表';

CREATE TABLE IF NOT EXISTS digital_human_equipments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  digital_human_id BIGINT UNSIGNED NOT NULL COMMENT '数字人ID',
  slot_code VARCHAR(64) NOT NULL COMMENT '槽位编码',
  museum_resource_id BIGINT UNSIGNED NOT NULL COMMENT '博物馆资源ID',
  anchor_code VARCHAR(64) DEFAULT NULL COMMENT '故事锚点编码',
  display_order INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '显示顺序',
  resource_snapshot_json JSON NOT NULL COMMENT '绑定时资源快照',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_digital_human_slot (digital_human_id, slot_code),
  KEY idx_digital_human_resource (museum_resource_id),
  CONSTRAINT fk_digital_human_equipments_human FOREIGN KEY (digital_human_id) REFERENCES digital_humans (id),
  CONSTRAINT fk_digital_human_equipments_resource FOREIGN KEY (museum_resource_id) REFERENCES museum_resources (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数字人装备绑定表';

CREATE TABLE IF NOT EXISTS task_submissions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
  exhibition_id BIGINT UNSIGNED NOT NULL COMMENT '展厅ID',
  submitter_id BIGINT UNSIGNED NOT NULL COMMENT '提交人ID',
  version_no INT UNSIGNED NOT NULL COMMENT '提交版本号',
  submit_remark VARCHAR(255) DEFAULT NULL COMMENT '提交备注',
  submission_status VARCHAR(20) NOT NULL DEFAULT 'submitted' COMMENT '状态：submitted/reviewed/returned',
  submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  reviewed_at DATETIME DEFAULT NULL COMMENT '评审时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_submissions_task_status (task_id, submission_status),
  KEY idx_submissions_exhibition (exhibition_id),
  KEY idx_submissions_submitter (submitter_id),
  CONSTRAINT fk_task_submissions_task FOREIGN KEY (task_id) REFERENCES tasks (id),
  CONSTRAINT fk_task_submissions_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id),
  CONSTRAINT fk_task_submissions_submitter FOREIGN KEY (submitter_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务提交记录表';

CREATE TABLE IF NOT EXISTS submission_reviews (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  submission_id BIGINT UNSIGNED NOT NULL COMMENT '提交记录ID',
  reviewer_id BIGINT UNSIGNED NOT NULL COMMENT '评审教师ID',
  score DECIMAL(5,2) DEFAULT NULL COMMENT '评分',
  comment_text TEXT DEFAULT NULL COMMENT '文字点评',
  comment_audio_url VARCHAR(255) DEFAULT NULL COMMENT '语音点评地址',
  is_public TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否公开',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_submission_reviewer (submission_id, reviewer_id),
  KEY idx_submission_reviews_reviewer (reviewer_id),
  CONSTRAINT fk_submission_reviews_submission FOREIGN KEY (submission_id) REFERENCES task_submissions (id),
  CONSTRAINT fk_submission_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师评分点评表';

CREATE TABLE IF NOT EXISTS exhibition_tags (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  tag_name VARCHAR(64) NOT NULL COMMENT '标签名',
  tag_type VARCHAR(20) NOT NULL DEFAULT 'custom' COMMENT '标签类型：theme/grade/custom',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展厅标签表';

CREATE TABLE IF NOT EXISTS exhibition_tag_relations (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  exhibition_id BIGINT UNSIGNED NOT NULL COMMENT '展厅ID',
  tag_id BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_exhibition_tag (exhibition_id, tag_id),
  KEY idx_exhibition_tag_relations_tag (tag_id),
  CONSTRAINT fk_exhibition_tag_relations_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id),
  CONSTRAINT fk_exhibition_tag_relations_tag FOREIGN KEY (tag_id) REFERENCES exhibition_tags (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展厅标签关系表';

CREATE TABLE IF NOT EXISTS exhibition_comments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  exhibition_id BIGINT UNSIGNED NOT NULL COMMENT '展厅ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '评论人ID',
  parent_comment_id BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID',
  root_comment_id BIGINT UNSIGNED DEFAULT NULL COMMENT '根评论ID',
  content TEXT NOT NULL COMMENT '评论内容',
  status VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '状态：normal/deleted',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_comments_exhibition_created (exhibition_id, created_at),
  KEY idx_comments_parent (parent_comment_id),
  KEY idx_comments_root (root_comment_id),
  KEY idx_comments_user (user_id),
  CONSTRAINT fk_exhibition_comments_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id),
  CONSTRAINT fk_exhibition_comments_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_exhibition_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES exhibition_comments (id),
  CONSTRAINT fk_exhibition_comments_root FOREIGN KEY (root_comment_id) REFERENCES exhibition_comments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展厅评论表';

CREATE TABLE IF NOT EXISTS comment_mentions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  comment_id BIGINT UNSIGNED NOT NULL COMMENT '评论ID',
  mentioned_user_id BIGINT UNSIGNED NOT NULL COMMENT '@用户ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_comment_mention (comment_id, mentioned_user_id),
  KEY idx_comment_mentions_user (mentioned_user_id),
  CONSTRAINT fk_comment_mentions_comment FOREIGN KEY (comment_id) REFERENCES exhibition_comments (id),
  CONSTRAINT fk_comment_mentions_user FOREIGN KEY (mentioned_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论@关系表';

CREATE TABLE IF NOT EXISTS exhibition_interactions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  exhibition_id BIGINT UNSIGNED NOT NULL COMMENT '展厅ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  interaction_type VARCHAR(20) NOT NULL COMMENT '互动类型：like/favorite/share',
  channel VARCHAR(32) NOT NULL DEFAULT '' COMMENT '分享渠道',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_exhibition_user_interaction (exhibition_id, user_id, interaction_type, channel),
  KEY idx_exhibition_interactions_user (user_id, interaction_type),
  CONSTRAINT fk_exhibition_interactions_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id),
  CONSTRAINT fk_exhibition_interactions_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展厅互动记录表';

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '接收人ID',
  notification_type VARCHAR(32) NOT NULL COMMENT '通知类型',
  title VARCHAR(128) NOT NULL COMMENT '标题',
  content VARCHAR(255) NOT NULL COMMENT '内容',
  biz_type VARCHAR(32) DEFAULT NULL COMMENT '业务类型',
  biz_id BIGINT UNSIGNED DEFAULT NULL COMMENT '业务主键ID',
  read_status VARCHAR(20) NOT NULL DEFAULT 'unread' COMMENT '读取状态：unread/read',
  read_at DATETIME DEFAULT NULL COMMENT '已读时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notifications_user_read (user_id, read_status, created_at),
  CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知消息表';

SET FOREIGN_KEY_CHECKS = 1;

-- 初始化基础数据
INSERT INTO schools (id, school_name, school_code, province, city, district, status)
VALUES
  (1, '知行实验学校', 'ZXSY001', '陕西省', '延安市', '宝塔区', 'enabled')
ON DUPLICATE KEY UPDATE
  school_name = VALUES(school_name),
  province = VALUES(province),
  city = VALUES(city),
  district = VALUES(district),
  status = VALUES(status);

INSERT INTO users (id, school_id, account, password_hash, role, real_name, nickname, email, mobile, status)
VALUES
  (1, 1, 'admin', '$2a$10$/kFIS6mXGNQEByYxiN9iXO9pn0QowUMQMXtfN/zBmNbKKrOptI6ue', 'admin', '平台管理员', '管理员', 'admin@zxcyj.com', '13800000000', 'active'),
  (2, 1, 'teacher001', '$2a$10$/kFIS6mXGNQEByYxiN9iXO9pn0QowUMQMXtfN/zBmNbKKrOptI6ue', 'teacher', '李老师', '李老师', 'teacher001@zxcyj.com', '13800000001', 'active'),
  (3, 1, 'student001', '$2a$10$/kFIS6mXGNQEByYxiN9iXO9pn0QowUMQMXtfN/zBmNbKKrOptI6ue', 'student', '张小明', '小明', 'student001@zxcyj.com', '13800000002', 'active'),
  (4, 1, 'student002', '$2a$10$/kFIS6mXGNQEByYxiN9iXO9pn0QowUMQMXtfN/zBmNbKKrOptI6ue', 'student', '王小红', '小红', 'student002@zxcyj.com', '13800000003', 'active')
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  nickname = VALUES(nickname),
  status = VALUES(status);

INSERT INTO classes (id, school_id, class_name, grade_level, academic_year, head_teacher_id, status)
VALUES
  (101, 1, '七年级(1)班', '七年级', '2025-2026', 2, 'active')
ON DUPLICATE KEY UPDATE
  class_name = VALUES(class_name),
  grade_level = VALUES(grade_level),
  academic_year = VALUES(academic_year),
  head_teacher_id = VALUES(head_teacher_id),
  status = VALUES(status);

INSERT INTO class_members (class_id, user_id, member_role, status)
VALUES
  (101, 2, 'teacher', 'active'),
  (101, 3, 'student', 'active'),
  (101, 4, 'student', 'active')
ON DUPLICATE KEY UPDATE
  member_role = VALUES(member_role),
  status = VALUES(status);

INSERT INTO museum_providers (id, provider_code, provider_name, api_base_url, status)
VALUES
  (1, 'yanan_memorial', '延安革命纪念馆', 'https://museum.example.com/api', 'enabled'),
  (2, 'red_history_center', '红色历史数字资源中心', 'https://redhistory.example.com/api', 'enabled')
ON DUPLICATE KEY UPDATE
  provider_name = VALUES(provider_name),
  api_base_url = VALUES(api_base_url),
  status = VALUES(status);

INSERT INTO exhibition_tags (id, tag_name, tag_type)
VALUES
  (1, '长征', 'theme'),
  (2, '改革开放', 'theme'),
  (3, '七年级', 'grade'),
  (4, '红色记忆', 'theme')
ON DUPLICATE KEY UPDATE
  tag_type = VALUES(tag_type);
