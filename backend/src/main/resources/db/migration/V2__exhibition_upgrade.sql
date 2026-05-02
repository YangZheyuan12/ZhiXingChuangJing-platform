-- V2__exhibition_upgrade.sql
-- 数字展馆升级迁移 — 修改已有表 + 新增表

-- 1. exhibitions 表增加字段
ALTER TABLE exhibitions
  ADD COLUMN template_id       BIGINT UNSIGNED DEFAULT NULL COMMENT '展馆模板ID' AFTER task_id,
  ADD COLUMN subject           VARCHAR(64)     DEFAULT NULL COMMENT '学科' AFTER group_name,
  ADD COLUMN grade_level       VARCHAR(32)     DEFAULT NULL COMMENT '年级' AFTER subject,
  ADD COLUMN ai_enabled        TINYINT(1)      NOT NULL DEFAULT 1 COMMENT 'AI功能开关' AFTER grade_level,
  ADD COLUMN comment_mode      VARCHAR(20)     NOT NULL DEFAULT 'free' COMMENT 'free/template/disabled' AFTER ai_enabled,
  ADD COLUMN workflow_status   VARCHAR(20)     NOT NULL DEFAULT 'draft' COMMENT 'draft/submitted/reviewing/returned/approved/published/archived' AFTER comment_mode,
  ADD COLUMN visibility_scope  VARCHAR(20)     NOT NULL DEFAULT 'private' COMMENT 'private/class/school/public' AFTER workflow_status,
  ADD COLUMN is_featured       TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否加精推荐' AFTER visibility_scope,
  ADD COLUMN published_version_id BIGINT UNSIGNED DEFAULT NULL COMMENT '当前公开发布版本ID' AFTER is_featured,
  ADD COLUMN bundle_revision   INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT 'editor-bundle乐观锁版本号' AFTER published_version_id,
  ADD COLUMN template_snapshot_json JSON DEFAULT NULL COMMENT '创建时模板快照(仅记录)' AFTER bundle_revision;

-- 2. 从旧字段迁移数据到新字段
UPDATE exhibitions SET workflow_status = status WHERE status IN ('draft','published','archived');
UPDATE exhibitions SET visibility_scope = visibility WHERE visibility IN ('private','class','public');
UPDATE exhibitions SET is_featured = featured_flag;

-- 3. exhibition_versions 增加 version_type
ALTER TABLE exhibition_versions
  ADD COLUMN version_type VARCHAR(20) NOT NULL DEFAULT 'manual' COMMENT 'autosave/manual/submitted/published' AFTER save_type;

-- 4. 新增 exhibition_zones 表
CREATE TABLE IF NOT EXISTS exhibition_zones (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  exhibition_id   BIGINT UNSIGNED NOT NULL,
  zone_code       VARCHAR(64)     NOT NULL COMMENT '编码如entrance/gallery-left',
  zone_type       VARCHAR(32)     NOT NULL COMMENT 'entrance/gallery/closeup/exit/timeline_node/map_point',
  title           VARCHAR(128)    NOT NULL,
  subtitle        VARCHAR(255)    DEFAULT NULL,
  description     TEXT            DEFAULT NULL,
  background_url  VARCHAR(255)    DEFAULT NULL COMMENT '2.5D场景背景图',
  background_style JSON           DEFAULT NULL COMMENT 'CSS样式(视差/动画)',
  layout_config   JSON            DEFAULT NULL COMMENT '展位槽配置',
  transition_in   VARCHAR(32)     NOT NULL DEFAULT 'fade',
  narration_text  TEXT            DEFAULT NULL COMMENT '展区讲解词',
  narration_audio VARCHAR(255)    DEFAULT NULL,
  canvas_data     JSON            DEFAULT NULL COMMENT 'Fabric画布JSON',
  sort_order      INT UNSIGNED    NOT NULL DEFAULT 0,
  assigned_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '认领人(协作分工)',
  locked_by       BIGINT UNSIGNED DEFAULT NULL COMMENT '编辑锁(短期互斥)',
  locked_at       DATETIME        DEFAULT NULL,
  status          VARCHAR(20)     NOT NULL DEFAULT 'active',
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_exhibition_zone (exhibition_id, zone_code),
  CONSTRAINT fk_zones_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展区表';

-- 5. 新增 zone_hotspots 表
CREATE TABLE IF NOT EXISTS zone_hotspots (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  zone_id         BIGINT UNSIGNED NOT NULL,
  target_zone_id  BIGINT UNSIGNED DEFAULT NULL,
  hotspot_type    VARCHAR(32)     NOT NULL COMMENT 'navigation/exhibit_popup/external_link/narration_trigger',
  label           VARCHAR(64)     DEFAULT NULL,
  icon            VARCHAR(32)     DEFAULT NULL COMMENT 'arrow-left/arrow-right/zoom-in/info/play',
  x_percent       DECIMAL(5,2)    NOT NULL,
  y_percent       DECIMAL(5,2)    NOT NULL,
  w_percent       DECIMAL(5,2)    NOT NULL DEFAULT 8.00,
  h_percent       DECIMAL(5,2)    NOT NULL DEFAULT 8.00,
  style_json      JSON            DEFAULT NULL,
  action_config   JSON            DEFAULT NULL COMMENT '外链URL/弹窗内容等',
  sort_order      INT UNSIGNED    NOT NULL DEFAULT 0,
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_hotspots_zone FOREIGN KEY (zone_id) REFERENCES exhibition_zones (id),
  CONSTRAINT fk_hotspots_target FOREIGN KEY (target_zone_id) REFERENCES exhibition_zones (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展区热点表';

-- 6. 新增 exhibition_exhibits 表
CREATE TABLE IF NOT EXISTS exhibition_exhibits (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  exhibition_id       BIGINT UNSIGNED NOT NULL,
  zone_id             BIGINT UNSIGNED NOT NULL,
  slot_code           VARCHAR(64)     DEFAULT NULL COMMENT '展位槽编码(slot模式)',
  placement_mode      VARCHAR(16)     NOT NULL DEFAULT 'slot' COMMENT '放置模式: slot(槽位)/free(自由定位)',
  placement_json      JSON            DEFAULT NULL COMMENT '自由定位坐标: {"x":20,"y":30,"w":25,"h":40,"zIndex":1}',
  title               VARCHAR(128)    NOT NULL,
  subtitle            VARCHAR(255)    DEFAULT NULL,
  exhibit_type        VARCHAR(32)     NOT NULL DEFAULT 'image' COMMENT 'image/video/audio/document/model/text',
  cover_url           VARCHAR(255)    DEFAULT NULL,
  media_url           VARCHAR(255)    DEFAULT NULL,
  source_type         VARCHAR(32)     NOT NULL DEFAULT 'upload' COMMENT 'museum/upload/ai_generated',
  museum_resource_id  BIGINT UNSIGNED DEFAULT NULL,
  media_asset_id      BIGINT UNSIGNED DEFAULT NULL,
  description         TEXT            DEFAULT NULL,
  source_info         JSON            DEFAULT NULL COMMENT '{"museum":"延安纪念馆","artifactName":"红军帽","era":"1935"}',
  knowledge_points    JSON            DEFAULT NULL COMMENT '["长征精神","革命历史"]',
  sort_order          INT UNSIGNED    NOT NULL DEFAULT 0,
  status              VARCHAR(20)     NOT NULL DEFAULT 'active',
  created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_exhibits_zone (zone_id, sort_order),
  CONSTRAINT fk_exhibits_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id),
  CONSTRAINT fk_exhibits_zone FOREIGN KEY (zone_id) REFERENCES exhibition_zones (id),
  CONSTRAINT fk_exhibits_museum FOREIGN KEY (museum_resource_id) REFERENCES museum_resources (id),
  CONSTRAINT fk_exhibits_asset FOREIGN KEY (media_asset_id) REFERENCES media_assets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展品表';

-- 7. 新增 exhibit_narrations 表
CREATE TABLE IF NOT EXISTS exhibit_narrations (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  exhibit_id        BIGINT UNSIGNED NOT NULL,
  narration_type    VARCHAR(32)     NOT NULL DEFAULT 'text' COMMENT 'text/audio/ai_generated',
  content           TEXT            NOT NULL,
  audio_url         VARCHAR(255)    DEFAULT NULL,
  voice_type        VARCHAR(64)     DEFAULT NULL,
  duration_seconds  INT UNSIGNED    DEFAULT NULL,
  sort_order        INT UNSIGNED    NOT NULL DEFAULT 0,
  created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_narrations_exhibit FOREIGN KEY (exhibit_id) REFERENCES exhibition_exhibits (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展品讲解表';

-- 8. 新增 exhibit_interactions 表
CREATE TABLE IF NOT EXISTS exhibit_interactions (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  exhibit_id        BIGINT UNSIGNED NOT NULL,
  interaction_type  VARCHAR(32)     NOT NULL COMMENT 'quiz/open_question/poll',
  question_text     VARCHAR(500)    NOT NULL,
  options_json      JSON            DEFAULT NULL,
  correct_answer    VARCHAR(64)     DEFAULT NULL,
  explanation       TEXT            DEFAULT NULL,
  sort_order        INT UNSIGNED    NOT NULL DEFAULT 0,
  created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_interactions_exhibit FOREIGN KEY (exhibit_id) REFERENCES exhibition_exhibits (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展品互动题表';

-- 9. 新增 exhibition_templates 表
CREATE TABLE IF NOT EXISTS exhibition_templates (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  template_code     VARCHAR(64)     NOT NULL,
  template_name     VARCHAR(128)    NOT NULL,
  template_type     VARCHAR(32)     NOT NULL COMMENT 'basic_gallery/immersive_2.5d/timeline/map_exploration',
  difficulty_level  VARCHAR(20)     NOT NULL DEFAULT 'beginner' COMMENT 'beginner/intermediate/advanced',
  description       TEXT            DEFAULT NULL,
  preview_url       VARCHAR(255)    DEFAULT NULL,
  zones_config      JSON            NOT NULL COMMENT '预设展区结构(背景/槽位/热点)',
  suitable_subjects JSON            DEFAULT NULL,
  suitable_grades   JSON            DEFAULT NULL,
  status            VARCHAR(20)     NOT NULL DEFAULT 'active',
  created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_template_code (template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展馆模板表';

-- 10. 新增 peer_review_templates 表
CREATE TABLE IF NOT EXISTS peer_review_templates (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  task_id       BIGINT UNSIGNED NOT NULL,
  question_text VARCHAR(500)    NOT NULL,
  question_type VARCHAR(32)     NOT NULL DEFAULT 'text' COMMENT 'text/rating/choice',
  options_json  JSON            DEFAULT NULL,
  sort_order    INT UNSIGNED    NOT NULL DEFAULT 0,
  created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_prt_task FOREIGN KEY (task_id) REFERENCES tasks (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互评模板表';

-- 11. 新增 peer_reviews 表
CREATE TABLE IF NOT EXISTS peer_reviews (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  exhibition_id BIGINT UNSIGNED NOT NULL,
  reviewer_id   BIGINT UNSIGNED NOT NULL,
  peer_review_template_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联互评模板问题ID',
  answer_text   TEXT            DEFAULT NULL,
  rating        DECIMAL(3,1)    DEFAULT NULL,
  created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_pr_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id),
  CONSTRAINT fk_pr_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id),
  CONSTRAINT fk_pr_template FOREIGN KEY (peer_review_template_id) REFERENCES peer_review_templates (id),
  UNIQUE KEY uk_peer_review_once (exhibition_id, reviewer_id, peer_review_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同伴互评表';

-- 12. 新增 exhibition_submissions 表
CREATE TABLE IF NOT EXISTS exhibition_submissions (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  task_id               BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
  exhibition_id         BIGINT UNSIGNED NOT NULL COMMENT '展厅ID',
  submitter_id          BIGINT UNSIGNED NOT NULL COMMENT '提交人ID',
  submitted_version_id  BIGINT UNSIGNED NOT NULL COMMENT '提交时冻结的版本ID',
  status                VARCHAR(20)     NOT NULL DEFAULT 'submitted' COMMENT 'submitted/reviewing/returned/approved',
  submit_count          INT UNSIGNED    NOT NULL DEFAULT 1 COMMENT '第几次提交',
  submitted_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_at           DATETIME        DEFAULT NULL,
  reviewer_id           BIGINT UNSIGNED DEFAULT NULL COMMENT '审核教师ID',
  return_reason         TEXT            DEFAULT NULL COMMENT '退回原因',
  created_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_submissions_task (task_id, status),
  KEY idx_submissions_exhibition (exhibition_id),
  UNIQUE KEY uk_submission_round (task_id, exhibition_id, submit_count),
  CONSTRAINT fk_sub_task FOREIGN KEY (task_id) REFERENCES tasks (id),
  CONSTRAINT fk_sub_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id),
  CONSTRAINT fk_sub_submitter FOREIGN KEY (submitter_id) REFERENCES users (id),
  CONSTRAINT fk_sub_version FOREIGN KEY (submitted_version_id) REFERENCES exhibition_versions (id),
  CONSTRAINT fk_sub_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展厅提交记录表';

-- 13. 新增 submission_reviews 表
CREATE TABLE IF NOT EXISTS submission_reviews (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  submission_id     BIGINT UNSIGNED NOT NULL COMMENT '提交记录ID',
  reviewer_id       BIGINT UNSIGNED NOT NULL COMMENT '评审教师ID',
  scores_json       JSON            NOT NULL COMMENT '多维评分',
  overall_score     DECIMAL(5,2)    DEFAULT NULL COMMENT '综合得分',
  overall_comment   TEXT            DEFAULT NULL COMMENT '总评语',
  ai_suggestion_json JSON           DEFAULT NULL COMMENT 'AI评价建议原始数据',
  action            VARCHAR(20)     NOT NULL COMMENT 'approve/return',
  created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_reviews_submission (submission_id),
  CONSTRAINT fk_review_submission FOREIGN KEY (submission_id) REFERENCES exhibition_submissions (id),
  CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提交评审记录表';

-- 14. 新增 exhibition_activity_logs 表
CREATE TABLE IF NOT EXISTS exhibition_activity_logs (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  exhibition_id BIGINT UNSIGNED NOT NULL,
  user_id       BIGINT UNSIGNED NOT NULL,
  action        VARCHAR(64)     NOT NULL COMMENT 'zone_created/exhibit_added/version_saved/...',
  zone_id       BIGINT UNSIGNED DEFAULT NULL,
  detail        JSON            DEFAULT NULL,
  created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_logs_exhibition (exhibition_id, created_at),
  CONSTRAINT fk_logs_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展厅操作日志表';

-- 15. 初始模板数据：2.5D 漫游模板
INSERT INTO exhibition_templates (template_code, template_name, template_type, difficulty_level, description, zones_config) VALUES
('immersive_2.5d_default', '2.5D 沉浸式展馆', 'immersive_2.5d', 'intermediate',
 '含入口大厅、三个主展区和出口的沉浸式漫游展馆，适合文物展/文化馆/研学成果展示。',
 JSON_OBJECT(
   'zones', JSON_ARRAY(
     JSON_OBJECT('zoneCode','entrance','zoneType','entrance','title','入口大厅',
       'backgroundUrl','/templates/immersive/entrance.jpg','transitionIn','fade',
       'layoutConfig', JSON_OBJECT('slots', JSON_ARRAY(
         JSON_OBJECT('code','title','x',30,'y',25,'w',40,'h',12,'label','展厅标题'),
         JSON_OBJECT('code','intro','x',25,'y',45,'w',50,'h',20,'label','简介')
       )),
       'hotspots', JSON_ARRAY(
         JSON_OBJECT('type','navigation','targetZoneCode','gallery-left','icon','arrow-right','x',85,'y',50,'w',8,'h',12)
       )
     ),
     JSON_OBJECT('zoneCode','gallery-left','zoneType','gallery','title','左展区',
       'backgroundUrl','/templates/immersive/gallery-left.jpg','transitionIn','slide-left',
       'layoutConfig', JSON_OBJECT('slots', JSON_ARRAY(
         JSON_OBJECT('code','exhibit-1','x',10,'y',20,'w',35,'h',55,'label','展品1'),
         JSON_OBJECT('code','exhibit-2','x',55,'y',20,'w',35,'h',55,'label','展品2')
       )),
       'hotspots', JSON_ARRAY(
         JSON_OBJECT('type','navigation','targetZoneCode','entrance','icon','arrow-left','x',5,'y',50,'w',8,'h',12),
         JSON_OBJECT('type','navigation','targetZoneCode','gallery-center','icon','arrow-right','x',87,'y',50,'w',8,'h',12)
       )
     ),
     JSON_OBJECT('zoneCode','gallery-center','zoneType','gallery','title','中央展区',
       'backgroundUrl','/templates/immersive/gallery-center.jpg','transitionIn','fade',
       'layoutConfig', JSON_OBJECT('slots', JSON_ARRAY(
         JSON_OBJECT('code','exhibit-main','x',20,'y',15,'w',60,'h',65,'label','主展品'),
         JSON_OBJECT('code','exhibit-side-l','x',5,'y',30,'w',12,'h',40,'label','侧展品左'),
         JSON_OBJECT('code','exhibit-side-r','x',83,'y',30,'w',12,'h',40,'label','侧展品右')
       )),
       'hotspots', JSON_ARRAY(
         JSON_OBJECT('type','navigation','targetZoneCode','gallery-left','icon','arrow-left','x',5,'y',50,'w',8,'h',12),
         JSON_OBJECT('type','navigation','targetZoneCode','gallery-right','icon','arrow-right','x',87,'y',50,'w',8,'h',12)
       )
     ),
     JSON_OBJECT('zoneCode','gallery-right','zoneType','gallery','title','右展区',
       'backgroundUrl','/templates/immersive/gallery-right.jpg','transitionIn','slide-right',
       'layoutConfig', JSON_OBJECT('slots', JSON_ARRAY(
         JSON_OBJECT('code','exhibit-3','x',10,'y',20,'w',35,'h',55,'label','展品3'),
         JSON_OBJECT('code','exhibit-4','x',55,'y',20,'w',35,'h',55,'label','展品4')
       )),
       'hotspots', JSON_ARRAY(
         JSON_OBJECT('type','navigation','targetZoneCode','gallery-center','icon','arrow-left','x',5,'y',50,'w',8,'h',12),
         JSON_OBJECT('type','navigation','targetZoneCode','exit','icon','arrow-right','x',87,'y',50,'w',8,'h',12)
       )
     ),
     JSON_OBJECT('zoneCode','exit','zoneType','exit','title','出口',
       'backgroundUrl','/templates/immersive/exit.jpg','transitionIn','fade',
       'layoutConfig', JSON_OBJECT('slots', JSON_ARRAY(
         JSON_OBJECT('code','summary','x',20,'y',20,'w',60,'h',25,'label','总结'),
         JSON_OBJECT('code','credits','x',25,'y',55,'w',50,'h',30,'label','鸣谢')
       )),
       'hotspots', JSON_ARRAY(
         JSON_OBJECT('type','navigation','targetZoneCode','gallery-right','icon','arrow-left','x',5,'y',50,'w',8,'h',12)
       )
     )
   )
 )
);
