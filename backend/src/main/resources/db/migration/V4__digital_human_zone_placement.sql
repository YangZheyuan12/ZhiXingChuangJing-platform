-- V4__digital_human_zone_placement.sql
-- 数字人多角色支持 + 展区摆放
--
-- 变更说明：
--   1. digital_humans 解除 (exhibition_id) 唯一约束 → 一个展厅可以有多个数字人角色
--   2. 新增 zone_digital_human_placements：展区与角色的 1:1 摆放关系（含坐标 + 朝向 + 缩放）
--      讲解词仍来自 exhibition_zones.narration_text 和 exhibits.narrations，本表只记位置
--
-- 注意：本脚本目前由人工执行，不是幂等的。重复执行会因索引/表已存在而报错，请先回滚。

-- 1. 解除 digital_humans 的展厅唯一约束（保留普通索引便于按展厅查询）
ALTER TABLE digital_humans
  DROP INDEX uk_digital_human_exhibition,
  ADD KEY idx_digital_humans_exhibition (exhibition_id);

-- 2. 新建展区数字人摆放关系表（每个展区最多 1 个角色）
CREATE TABLE IF NOT EXISTS zone_digital_human_placements (
  zone_id          BIGINT UNSIGNED NOT NULL                COMMENT '展区ID（主键确保 1:1）',
  digital_human_id BIGINT UNSIGNED NOT NULL                COMMENT '数字人ID',
  x_percent        DECIMAL(6,3)    NOT NULL DEFAULT 80.000 COMMENT '中心点 X 百分比 (0~100)',
  y_percent        DECIMAL(6,3)    NOT NULL DEFAULT 70.000 COMMENT '中心点 Y 百分比 (0~100)',
  scale            DECIMAL(5,3)    NOT NULL DEFAULT 1.000  COMMENT '缩放系数 (0.1~5.0)',
  facing           VARCHAR(8)      NOT NULL DEFAULT 'left' COMMENT '朝向：left/right',
  created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (zone_id),
  KEY idx_zone_dh_placement_dh (digital_human_id),
  CONSTRAINT fk_zone_dh_placement_zone   FOREIGN KEY (zone_id)          REFERENCES exhibition_zones (id) ON DELETE CASCADE,
  CONSTRAINT fk_zone_dh_placement_human  FOREIGN KEY (digital_human_id) REFERENCES digital_humans (id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展区数字人摆放表（zone : 数字人 = 1 : 1）';
