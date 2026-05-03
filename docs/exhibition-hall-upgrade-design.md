# 数字展馆升级设计文档

> **文档版本**：v1.2 | **日期**：2025-05-02 | **技术路线**：HTML 场景 + Fabric 展品层（方案 C）| **范围**：全量设计
>
> v1.1 变更：补 submission/review 领域模型、状态三维拆分、展品与 Fabric 边界定义、自动保存与版本分离、P0 瘦身、AI 约束/缓存/降级、协作心跳与本地草稿、坐标体系硬规范、编辑器整包接口、模板实例化快照
>
> v1.2 变更：Viewer 读取源规则表、published_version_id、workflow_status 同步规则、自动保存覆盖结构化表单、editor-bundle 乐观锁、热点 P0 一致性、AI 优先级对齐、approved/published 状态迁移规则、自由创作发布链路、运行时规则附录

---

## 1. 产品定位

> 数字展厅是面向中小学项目式学习的 2.5D 沉浸式创作与展示空间。教师布置任务，学生调用博物馆资源和 AI 工具搭建展厅、编写讲解、展示发布，实现"任务驱动—创作—展示—评价"闭环。

**五大核心特征**：有课程任务、有空间感、有参观路径、有讲解系统、有互动反馈。

**与当前实现的本质差异**：

| 当前（Fabric 平面画布） | 目标（HTML 场景 + Fabric） |
|---|---|
| 扁平 Fabric JSON | 展馆→展区→展品 树形结构 |
| 一块大画布自由放元素 | 多展区场景切换编辑 |
| 静态渲染画布 | 热点导航+场景过渡+讲解触发 |
| 版式模板（平面排版） | 空间模板（展馆布局） |

---

## 2. 架构总览

### 2.1 三层渲染架构（每个展区）

```
Layer 3: 热点覆盖层 (HTML absolute) — 导航箭头/展品点击区/数字人触发点
Layer 2: Fabric 画布层 (透明背景)   — 自由布置的文本/图片/装饰元素
Layer 1: 场景背景层 (HTML/CSS)      — 2.5D 场景插画 + CSS 动画/视差
```

### 2.2 场景切换机制

```
保存当前展区画布 → 销毁 Fabric → CSS 过渡动画(fade/slide/zoom) → 加载目标展区背景 → 初始化新 Fabric → 渲染热点
```

过渡类型：`fade`、`slide-left`、`slide-right`、`zoom-in`。

### 2.3 系统分层

- **应用层**：学生端 / 教师端 / 展馆浏览端 / 社区 / 管理后台
- **业务服务层**：认证 / 任务 / 展厅CRUD / 资源库 / 评论 / 审核发布
- **AI 服务层**：讲解词生成 / 大纲生成 / TTS / 问答 / 评价建议
- **数据层**：MySQL + MinIO/OSS + Redis + JSON快照
- **外部接入**：博物馆API / AI大模型 / TTS / 内容审核

### 2.4 坐标体系与响应式规则（硬规范）

| 规则 | 说明 |
|------|------|
| 固定舞台比例 | **16:9**，逻辑尺寸 1920×1080 |
| 背景渲染 | `contain + letterbox`（上下黑边），不用 `cover`，避免裁切导致热点错位 |
| 坐标基准 | 热点、槽位、Fabric 均基于同一套 16:9 stage 坐标（百分比） |
| 编辑器 | **PC 端优先**，不承诺移动端编辑 |
| 浏览器 | PC / Pad / Mobile 适配，小屏等比缩放，不拉伸 |

---

## 3. 数据模型设计

### 3.1 实体关系

```
Exhibition ─< ExhibitionZone ─< ZoneHotspot
                    │
                    └─< ExhibitionExhibit ─< ExhibitNarration
                                │             └─< ExhibitInteraction
                                ├── museum_resource_id → MuseumResource
                                └── media_asset_id → MediaAsset

Exhibition ── template_id → ExhibitionTemplate
Exhibition ── task_id → Task
Exhibition ─< ExhibitionMember (已有)
Exhibition ─< ExhibitionVersion (已有，扩展)
Exhibition ─< DigitalHuman (已有)
Exhibition ─< ExhibitionComment (已有)
Exhibition ─< PeerReview (新增)
Exhibition ─< ExhibitionSubmission (新增) ─< SubmissionReview (新增)
```

### 3.2 新增表：exhibition_zones

```sql
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
  assigned_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '认领人(协作)',
  locked_by       BIGINT UNSIGNED DEFAULT NULL COMMENT '编辑锁',
  locked_at       DATETIME        DEFAULT NULL,
  status          VARCHAR(20)     NOT NULL DEFAULT 'active',
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_exhibition_zone (exhibition_id, zone_code),
  CONSTRAINT fk_zones_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id)
) COMMENT='展区表';
```

**`layout_config` 示例**（坐标为百分比，相对 1920×1080）：
```json
{"slots": [
  {"code": "wall-left",   "x": 10, "y": 20, "w": 25, "h": 40, "label": "左墙展板"},
  {"code": "wall-center", "x": 38, "y": 15, "w": 24, "h": 50, "label": "中央展柜"},
  {"code": "wall-right",  "x": 65, "y": 20, "w": 25, "h": 40, "label": "右墙展板"}
]}
```

### 3.3 新增表：zone_hotspots

```sql
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
) COMMENT='展区热点表';
```

### 3.4 新增表：exhibition_exhibits

```sql
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
) COMMENT='展品表';
```

### 3.5 新增表：exhibit_narrations / exhibit_interactions

```sql
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
) COMMENT='展品讲解表';

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
) COMMENT='展品互动题表';
```

### 3.6 新增表：exhibition_templates

```sql
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
) COMMENT='展馆模板表';
```

**`zones_config` 示例**（2.5D 漫游模板）：
```json
{"zones": [
  {"zoneCode":"entrance","zoneType":"entrance","title":"入口大厅",
   "backgroundUrl":"/templates/immersive/entrance.jpg","transitionIn":"fade",
   "layoutConfig":{"slots":[
     {"code":"title","x":30,"y":25,"w":40,"h":12,"label":"展厅标题"},
     {"code":"intro","x":25,"y":45,"w":50,"h":20,"label":"简介"}
   ]},
   "hotspots":[
     {"type":"navigation","targetZoneCode":"gallery-left","icon":"arrow-right","x":85,"y":50,"w":8,"h":12}
   ]},
  {"zoneCode":"gallery-left","zoneType":"gallery","title":"左展区", "...": "..."}
]}
```

### 3.7 修改已有表

**exhibitions 表增加字段**：
```sql
ALTER TABLE exhibitions
  ADD COLUMN template_id       BIGINT UNSIGNED DEFAULT NULL COMMENT '展馆模板ID' AFTER task_id,
  ADD COLUMN subject           VARCHAR(64)     DEFAULT NULL AFTER group_name,
  ADD COLUMN grade_level       VARCHAR(32)     DEFAULT NULL AFTER subject,
  ADD COLUMN ai_enabled        TINYINT(1)      NOT NULL DEFAULT 1 AFTER grade_level,
  ADD COLUMN comment_mode      VARCHAR(20)     NOT NULL DEFAULT 'free' COMMENT 'free/template/disabled' AFTER ai_enabled,
  ADD COLUMN workflow_status   VARCHAR(20)     NOT NULL DEFAULT 'draft' COMMENT 'draft/submitted/reviewing/returned/approved/published/archived',
  ADD COLUMN visibility_scope  VARCHAR(20)     NOT NULL DEFAULT 'private' COMMENT 'private/class/school/public',
  ADD COLUMN is_featured       TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否加精推荐',
  ADD COLUMN published_version_id BIGINT UNSIGNED DEFAULT NULL COMMENT '当前公开发布版本ID';
```

**状态三维拆分说明**（替代原 `status` + `visibility` 混合设计）：

| 维度 | 字段 | 取值 | 说明 |
|------|------|------|------|
| 工作流 | `workflow_status` | draft/submitted/reviewing/returned/approved/published/archived | 展厅生命周期 |
| 可见范围 | `visibility_scope` | private/class/school/public | 发布后谁能看 |
| 推荐标记 | `is_featured` | 0/1 | 教师/管理员标记精选 |

三者正交，互不影响。例如：已审核通过但仅班级可见、公开但非精选、精选但仅校内推荐，均可表达。

**exhibition_versions 增加 `version_type` 字段**：
```sql
ALTER TABLE exhibition_versions
  ADD COLUMN version_type VARCHAR(20) NOT NULL DEFAULT 'manual' COMMENT 'autosave/manual/submitted/published';
```

**exhibition_versions 版本数据格式 v2**：
```json
{
  "formatVersion": 2,
  "zones": {
    "entrance":     {"fabricJson": {"version":"7.3.1","objects":[...]}, "hotspots": [...]},
    "gallery-left": {"fabricJson": {"version":"7.3.1","objects":[...]}, "hotspots": [...]}
  },
  "globalConfig": {"defaultTransition":"fade","digitalHumanPosition":"bottom-right"}
}
```
兼容：无 `formatVersion` 字段按旧版单画布解析，加载到第一个展区。

### 3.8 新增表：互评相关

```sql
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
) COMMENT='互评模板表';

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
) COMMENT='同伴互评表';
```

### 3.9 新增表：提交审核模型

> **核心原则**：展厅是作品对象，提交是业务动作。一个展厅可多次提交同一任务（退回后重交，`submit_count + 1`），审核必须冻结版本。
>
> **自由创作链路**：`task_id = NULL` 时表示非任务作业的社区创作，不走教师审核。若要公开发布，走管理员/内容审核流程，发布后 `visibility_scope=public`。

```sql
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
) COMMENT='展厅提交记录表';

CREATE TABLE IF NOT EXISTS submission_reviews (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  submission_id     BIGINT UNSIGNED NOT NULL COMMENT '提交记录ID',
  reviewer_id       BIGINT UNSIGNED NOT NULL COMMENT '评审教师ID',
  scores_json       JSON            NOT NULL COMMENT '{"content_accuracy":90,"theme_completeness":80,...}',
  overall_score     DECIMAL(5,2)    DEFAULT NULL COMMENT '综合得分',
  overall_comment   TEXT            DEFAULT NULL COMMENT '总评语',
  ai_suggestion_json JSON           DEFAULT NULL COMMENT 'AI评价建议原始数据',
  action            VARCHAR(20)     NOT NULL COMMENT 'approve/return',
  created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_reviews_submission (submission_id),
  CONSTRAINT fk_review_submission FOREIGN KEY (submission_id) REFERENCES exhibition_submissions (id),
  CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id)
) COMMENT='提交评审记录表';

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
) COMMENT='展厅操作日志表';
```

---

## 4. 展馆模板系统

### 4.1 四种模板类型

| 类型 | 编码 | 展区结构 | 适用 |
|---|---|---|---|
| 基础图文 | `basic_gallery` | 一页一展区，类似展板 | 低年级快速作业 |
| 2.5D 漫游 | `immersive_2.5d` | 入口→展区×3→出口，热点导航 | 文物展/文化馆 |
| 时间轴 | `timeline` | 时间节点序列 | 历史事件/人物生平 |
| 地图探索 | `map_exploration` | 中心地图+地点展区 | 地理/人文/研学 |

### 4.2 难度分级

- **beginner**：套模板填内容，不可改布局 → 小学
- **intermediate**：可调整展区顺序、增删展区 → 初中
- **advanced**：自定义路线/热点/讲解逻辑 → 高中/社团

### 4.3 2.5D 漫游模板预设结构

```
[入口大厅] →→ [左展区] →→ [中央展区]
                                ↓↓
             [总结出口] ←← [右展区]
```

5 个展区，每个展区 2-3 个展品槽位。场景背景图需按主题提供 2-3 套配色。

### 4.4 模板实例化规则

> **核心原则**：创建展厅时，模板“拷贝成实例”，不动态依赖模板本体。

创建展厅时：
1. 读取模板的 `zones_config` JSON
2. 展开为实际的 `exhibition_zones` + `zone_hotspots` 记录
3. 后续修改模板不影响已创建的展厅
4. `exhibitions.template_id` 仅做来源记录，不做运行时依赖

补充字段（可选，快照记录）：
```sql
ALTER TABLE exhibitions
  ADD COLUMN template_snapshot_json JSON DEFAULT NULL COMMENT '创建时模板快照(仅记录)';
```

### 4.5 背景样式白名单

`exhibition_zones.background_style` 仅允许受控字段，不开放任意 CSS：

| 字段 | 类型 | 说明 |
|------|------|------|
| `parallax` | boolean | 是否启用视差滚动 |
| `animation` | enum | none/subtle-float/slow-pan |
| `opacity` | number(0-1) | 背景亮度 |
| `overlay` | string | 覆盖层颜色如 rgba(0,0,0,0.1) |

---

## 5. 展馆编辑器改造

### 5.1 编辑器布局

```
┌─────────────────────────────────────────────────────────────────┐
│ 顶栏: 返回│展厅名│自动保存│撤销/重做│保存│预览│发布             │
├────────┬──────────────────────────────────────────┬─────────────┤
│ 左侧栏  │            场景画布区                    │ 右侧栏      │
│┌──────┐│  Layer3:热点  Layer2:Fabric  Layer1:背景  │┌──────────┐│
││展区   ││                                          ││展区/展品  ││
││导航   ││                                          ││/热点/    ││
│├──────┤│                                          ││画布属性  ││
││展品   ││                                          │└──────────┘│
││列表   ││                                          │            │
│├──────┤│                                          │            │
││素材库 ││                                          │            │
│├──────┤│                                          │            │
││图层   ││                                          │            │
│└──────┘│                                          │            │
├────────┴──────────────────────────────────────────┴─────────────┤
│ 底部展区条: [入口大厅] [左展区] [中央展区] [右展区] [出口] [+]   │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 组件树

```
Editor.vue
├── EditorHeader.vue
├── EditorLeftPanel.vue
│   ├── ZoneNavigator.vue (新增 - 展区列表/增删/排序)
│   ├── ExhibitList.vue (新增 - 当前展区展品列表)
│   ├── AssetPicker.vue (已有)
│   ├── LayerPanel.vue (已有, per-zone)
│   └── TemplatePicker.vue (改造为展馆模板)
├── EditorCanvas.vue (新增 - 场景渲染)
│   ├── SceneBackground.vue (Layer1)
│   ├── FabricCanvasLayer.vue (Layer2)
│   ├── HotspotOverlay.vue + HotspotMarker.vue (Layer3)
│   └── ExhibitSlotOverlay.vue + SlotMarker.vue (槽位高亮)
├── EditorRightPanel.vue
│   ├── ZonePropertiesPanel.vue (新增)
│   ├── ExhibitPropertiesPanel.vue (新增 - 含讲解词/互动题编辑)
│   ├── HotspotPropertiesPanel.vue (新增, **P1 预留**，P0 不实现)
│   ├── CanvasObjectProperties (已有逻辑)
│   └── TextStylePanel.vue (已有)
└── ZoneStrip.vue (新增 - 底部展区缩略图条)
```

### 5.3 核心 Composables

| Composable | 说明 | 状态 |
|---|---|---|
| `useZoneManager` | 展区 CRUD/切换/排序/编辑锁 | 新增 |
| `useExhibitManager` | 展品 CRUD/拖入/属性编辑 | 新增 |
| `useHotspotManager` | 热点 CRUD/拖拽定位 (**P1 预留**，P0 仅读取模板预置热点) | 新增 |
| `useSceneRenderer` | 场景分层渲染/Fabric生命周期/切换动画 | 新增 |
| `useCanvasHistory` | per-zone 撤销重做 | 改造 |
| `useCanvasAutosave` | 全展厅自动保存(收集所有zone) | 改造 |
| `useCanvasShortcuts` | 扩展展区切换快捷键 | 改造 |
| `useAlignmentGuides` | 不变 | 已有 |

### 5.4 展区切换流程

```
1. saveCurrentZoneCanvas() → 保存当前 Fabric JSON 到内存/zone.canvas_data
2. destroyScene() → 销毁 Fabric Canvas
3. CSS transition (0.4s fade/slide)
4. initScene(targetZone) → 设背景图 → 新建 Fabric(透明) → loadCanvasJson → 渲染热点
5. 更新右侧面板为目标展区属性
```

### 5.5 展品与 Fabric 职责边界（硬规则）

> **展品是业务对象，Fabric 只负责装饰/标注/自由文本。**

| 层 | 负责 | 存储 |
|---|---|---|
| **结构化层**（exhibition_exhibits） | 展品标题/来源/讲解词/知识点/互动题/展区/槽位 | 业务表 |
| **Fabric 层**（canvas_data） | 自由说明文字/装饰贴纸/辅助图形/箭头/标签 | JSON |

**好处**：
- 展品详情弹窗直接从业务表取数据，不依赖 Fabric
- AI 讲解直接拿展品结构数据
- 不会出现“画布上删了，但数据库还有展品”的双写问题
- 资源库接入天然结构化

**自由放置展品的处理**：展品仍然是 `exhibition_exhibits` 记录（`placement_mode=free`），只是位置信息存在 `placement_json` 而非槽位编码。

### 5.6 保存与版本语义分层

| 操作 | 存储目标 | 是否创建 version 记录 |
|------|----------|------------------------|
| **自动保存**（debounce 10s） | 更新当前展区草稿态（画布 + 结构化表单） | 否 |
| **手动保存草稿** | 创建 version（`version_type=manual`） | 是 |
| **提交审核** | 创建 version（`version_type=submitted`）+ 创建 submission | 是，冻结 |
| **发布** | 创建 version（`version_type=published`） | 是，不可变 |

**自动保存流程**：
```
debounce 10s 触发 → PUT /api/v1/exhibitions/{id}/draft-bundle
← { zoneCode, canvasData, exhibits[], zoneMetadata }
→ 后端事务性更新 exhibition_zones.canvas_data + 展品结构化字段，不创建 version
```

> **范围说明**：自动保存不仅覆盖画布 JSON，还包括当前编辑中的展品标题/描述/讲解词/互动题/展区标题等结构化表单。界面统一显示“已自动保存”，不区分画布与表单。

**手动保存流程**：
```
用户点“保存草稿” → 收集所有展区 canvas_data → 构建 version_data(formatVersion:2)
→ POST /exhibitions/{id}/versions {version_type:"manual"}
```

### 5.7 编辑器整包接口

为避免大量零散 CRUD，增加“一次拿全量 / 一次事务保存”接口：

```
GET  /api/v1/exhibitions/{id}/editor-bundle
→ { exhibition, zones[], exhibits[], hotspots[], digitalHuman, template, revision }

PUT  /api/v1/exhibitions/{id}/editor-bundle
← { revision, zones[], exhibits[], hotspots[], canvasDataMap: {zoneCode: fabricJson} }
→ 后端事务性保存所有变更
```

**乐观锁 / 并发保护**：

- `revision` 为自增版本号，每次成功保存后 +1
- PUT 时后端比对客户端携带的 `revision` 与当前值：
  - 一致 → 保存成功，返回新 `revision`
  - 不一致 → 返回 `409 Conflict`，前端提示“展厅内容已被其他成员更新，请刷新后重试”
- 字段建议放在 `exhibitions` 表：`bundle_revision INT UNSIGNED NOT NULL DEFAULT 0`
- `draft-bundle`（自动保存）不参与乐观锁，仅整包保存/手动保存需要校验 `revision`

编辑器启动时调用 GET，保存时调用 PUT，减少网络往返和部分失败风险。

### 5.8 展品编辑流程

- **从素材库拖入** → 选择槽位或自由放置 → 创建 exhibit 记录（placement_mode=slot/free）→ 右侧展品属性面板
- **从文博资源选择** → 自动填充 source_info/cover/description → 可触发 AI 生成讲解词
- **AI 辅助** → 输入关键词 → AI 生成展品标题/描述/讲解词/互动题建议

---

## 6. 展馆参观模式

### 6.1 组件结构

```
ExhibitionViewer.vue (全屏沉浸)
├── ViewerTopBar.vue (极简: 返回/标题/作者)
├── SceneContainer.vue
│   ├── SceneBackground (背景+视差+动画)
│   ├── FabricCanvasLayer (只读)
│   ├── ExhibitClickAreas (展品hover高亮+可点击)
│   ├── HotspotButtons (导航热点按钮)
│   └── DigitalHumanWidget (AI讲解员)
├── ExhibitDetailModal.vue (展品详情弹窗)
│   ├── 大图/视频播放
│   ├── 来源信息/知识点
│   ├── 讲解词+语音播放
│   └── 互动答题
├── NavigationControls.vue (前进/后退/小地图)
├── MiniMap.vue (展区缩略图+当前位置)
└── CommentDrawer.vue (评论抽屉: 教师点评/互评/自由评论)
```

### 6.2 参观路径

```
进入 → 入口展区(sort_order=0) → 数字人欢迎 → 用户自由探索:
  ├── 点击热点 → 切换展区(带过渡)
  ├── 点击展品 → 弹出详情(大图/讲解/答题)
  ├── 点击数字人 → 播放当前展区讲解
  ├── 前进/后退箭头 → 顺序导航
  └── 小地图 → 跳转任意展区
→ 到达出口 → 总结/反思/评论/分享
```

### 6.3 展品详情弹窗

```
┌──────────────────────────────────────┐
│ ✕                       展品详情     │
│ ┌──────────────────────────────────┐ │
│ │        展品大图/视频              │ │
│ └──────────────────────────────────┘ │
│ 红军的斗笠                           │
│ 来源：延安革命纪念馆·革命文物·1935年  │
│ 这顶斗笠见证了红军长征...             │
│ 📖 知识点: 长征精神 / 革命传统        │
│ 🎙 讲解              [▶ 播放讲解]    │
│ "同学们，请看这顶斗笠..."            │
│ ❓ 互动                              │
│ 红军长征经过几个省？                  │
│ ○A.8 ○B.11 ●C.14 ○D.18             │
└──────────────────────────────────────┘
```

### 6.4 小地图

展示展区结构缩略图 + 当前位置高亮 + 已参观标记 + 点击跳转。

---

## 7. AI 数字人系统

### 7.1 AI 能力矩阵

| 类别 | 场景 | 功能 | 优先级 |
|------|------|------|--------|
| 创作辅助 | 编辑器 | 讲解词生成/大纲生成/润色 | **P0** |
| 参观讲解 | 观众浏览 | 固定讲解(文本气泡)+展品跟随(文本) | **P0**（无TTS/无问答） |
| TTS 语音 | 观众浏览 | 讲解词语音合成+数字人语音播放 | **P1** |
| AI 问答 | 观众浏览 | 限定上下文的展厅内问答 | **P2** |
| 教学评价 | 教师端 | 评分建议/亮点提取/反馈生成 | **P1** |

### 7.2 创作辅助 AI（编辑器内）

**AI 生成讲解词**：
```
POST /api/v1/ai/narration/generate
← {exhibitTitle, exhibitDescription, knowledgePoints, targetGrade, style, maxLength}
→ {narration: "同学们...", suggestions: ["建议补充..."]}
```
style 可选：`narrative`(叙述)、`academic`(学术)、`storytelling`(故事)、`conversational`(对话)。

**AI 生成展厅大纲**：
```
POST /api/v1/ai/outline/generate
← {theme, templateType, gradeLevel, museumResources?}
→ {title, subtitle, zones: [{title, suggestedExhibits, narrative}]}
```
根据主题和模板类型生成完整的展区规划建议。

**AI 润色文本**：
```
POST /api/v1/ai/text/polish
← {text, context, targetGrade, style}
→ {polished: "..."}
```

### 7.3 参观讲解 AI（观众浏览）

**三种讲解模式**：

| 模式 | 触发方式 | 数据来源 | 优先级 |
|------|----------|----------|--------|
| 固定讲解 | 点击数字人 | `exhibition_zones.narration_text` → 文本气泡 | P0（文本），P1（+TTS） |
| 展品跟随 | 打开展品详情时自动 | `exhibit_narrations.content` → 文本气泡 | P0（文本），P1（+TTS） |
| 问答模式 | 用户输入问题 | AI 对话（限定展厅内容为上下文） | **P2** |

**数字人 UI**：位于场景右下角，P0 两种状态（待机/讲解中），P1 增加等待提问状态。P0 讲解时仅文字逐字显示，P1 增加语音同步播放。

**TTS 语音合成**：
```
POST /api/v1/ai/tts/synthesize
← {text, voiceType, speed}
→ {audioUrl, duration}
```
结果缓存到 `narration_audio` / `exhibit_narrations.audio_url`。

**展厅内 AI 问答**：
```
POST /api/v1/ai/qa
← {question, exhibitionId, currentZoneId, context:"auto"}
→ {answer, sources:["展品:红军路线图"], outOfScope:false}
```
`outOfScope: true` 时回复"请围绕当前展厅内容提问哦。"

### 7.4 教学评价 AI

```
POST /api/v1/ai/evaluation/suggest
← {exhibitionId, evaluationCriteria}
→ {overallScore:85, dimensions:[{name,score,comment}], highlights:[], suggestions:[]}
```

### 7.5 AI 工程约束（必须遵守）

#### 1）上下文约束

展厅内问答必须强约束在：
- 当前展厅的展区/展品/讲解词数据
- 关联的博物馆资源库内容
- 超出范围时返回 `outOfScope: true`，不允许大模型自由发挥

System Prompt 模板：
```
你是{exhibitionTitle}的AI讲解员。仅围绕以下展品信息回答：
{exhibits[].title + description + knowledgePoints}
如果问题超出范围，回复“请围绕当前展厅内容提问哦。”
```

#### 2）缓存策略

| 场景 | 缓存 Key | TTL | 说明 |
|------|-----------|-----|------|
| 讲解词生成 | `narration:{hash(title+desc+grade+style)}` | 7d | 粒度到展品+风格 |
| TTS 语音 | `tts:{hash(text+voiceType+speed)}` | 30d | 结果存 OSS，URL 入库 |
| AI 评价建议 | `eval:{exhibitionId}:{version}` | 1d | 版本级缓存 |

#### 3）降级方案

AI 服务不可用时，系统必须正常运行：

| 功能 | 降级行为 |
|------|----------|
| 讲解词生成 | 学生手动写讲解词（编辑器文本框） |
| TTS 语音 | 数字人只显示文本气泡，不播音频 |
| AI 问答 | 显示“AI 讲解员暂时不在，请阅读展品说明” |
| AI 评价 | 教师手动评分（隐藏AI建议按钮） |

---

## 8. 教师端设计

### 8.1 教师工作台概览

教师登录后首页（DashboardView 按角色差异化）：
- **今日概览**：待审核数 / 进行中任务 / 本周提交 / 活跃度趋势
- **快捷入口**：发布任务 / 审核作品 / 查看班级 / 资源中心
- **待办事项**：`[审核] 张小明 - 长征精神展厅` 等

### 8.2 页面清单

| 页面 | 路由 | 状态 |
|------|------|------|
| 任务中心 | `/tasks` | 已有，教师可创建 |
| 任务发布 | `/tasks/create` | 已有，需增模板/资源包/评分标准 |
| 班级作品管理 | `/tasks/:taskId/submissions` | **新增** |
| 作品审核评价 | `/submissions/:id/review` | **新增** |
| 资源分发 | `/tasks/:taskId/resources` | **新增** |
| 数据看板 | `/tasks/:taskId/analytics` | **新增** |

### 8.3 班级作品管理页

展示任务下所有小组提交状态：截止日期、已提交/已审核计数、按状态筛选（草稿/已提交/审核中/已通过），每行展示小组名+展厅标题+状态+操作（预览/审核）。

### 8.4 作品审核评价页

左侧嵌入 ExhibitionViewer 预览展厅，右侧评价面板：

**评分维度**（7 维度）：

| 维度 | 权重 |
|------|------|
| 内容准确性 | 20% |
| 主题完整性 | 15% |
| 逻辑结构 | 15% |
| 审美表达 | 15% |
| 互动设计 | 15% |
| 小组协作 | 10% |
| 创新性 | 10% |

每个维度可打分 0-100 + 文字评语。支持 [AI评价建议] 一键生成评分草稿。

操作按钮：`[通过]` `[退回修改]` `[推荐展示]`

### 8.5 后端 API 扩展

```
GET  /api/v1/tasks/{taskId}/submissions        → 教师查看所有提交
POST /api/v1/submissions/{id}/review            → 评分 {scores, comment, action}
POST /api/v1/submissions/{id}/return            → 退回 {reason}
POST /api/v1/exhibitions/{id}/feature           → 推荐 {reason}
GET  /api/v1/tasks/{taskId}/analytics           → 统计数据
```

---

## 9. 协作系统

### 9.1 协作模型

**非实时协同**，采用"展区认领 + 编辑锁 + 版本管理"：

```
展厅
├── 组长(owner) → 管理/提交审核/合并
├── 成员A(editor) → 认领"左展区"
├── 成员B(editor) → 认领"右展区"
└── 成员C(viewer) → 只读
```

### 9.2 编辑锁规则

- 进入展区编辑时获取锁（`exhibition_zones.locked_by`）
- 锁超时 10 分钟自动释放
- **心跳续锁**：前端每 30s 发送 `PUT /zones/{zoneId}/heartbeat`，后端刷新 `locked_at`。心跳停止超过 2 分钟则自动释放锁，避免用户还在编辑但锁过期
- 其他成员看到“该展区正在被 XXX 编辑”
- 组长可强制解锁

### 9.3 本地草稿恢复

学生网络不稳时容易丢失编辑内容，必须有本地备份：

- 存储介质：`IndexedDB`（优先）或 `localStorage`
- 存储内容：当前展区的 `canvas_data` + 展品列表快照
- 记录 key：`draft:{exhibitionId}:{zoneCode}:{timestamp}`
- 重新打开编辑器时，检测到本地草稿比服务端新则提示“检测到未保存的草稿，是否恢复？”
- 成功保存到服务器后清理本地草稿

### 9.4 操作日志

`exhibition_activity_logs` 表记录所有操作：`zone_created` / `exhibit_added` / `version_saved` / `zone_edited` 等，带用户ID、展区ID、操作详情 JSON。

### 9.5 版本对比

基于 `exhibition_versions` 已有的版本列表，增加版本对比功能：
- 按展区对比两个版本的 Fabric JSON diff
- 高亮新增/删除/修改的元素
- P2 优先级，初期可只展示版本列表和恢复

---

## 10. 资源库系统

### 10.1 四种资源来源

| 来源 | 说明 | 集成方式 |
|------|------|----------|
| 官方文博资源 | 延安纪念馆等 API | `museum_resources` 缓存表 + 定时同步 |
| 教师资源包 | 教师为任务上传的参考资料 | `task_materials` 已有 |
| 学生上传 | 学生自己的图片/视频/音频 | `media_assets` 已有 |
| AI 生成 | AI 生成的图片/文本/讲解词 | AI API + 存入 `media_assets` |

### 10.2 资源入展流程

```
资源库浏览 → 选择资源 → 自动创建 exhibit 记录
  ├── 文博资源 → 自动填充 source_info/知识点/封面
  ├── 教师资源 → 自动填充标题/描述
  ├── 学生上传 → 手动填写展品信息
  └── AI 生成 → AI 返回内容直接填入
```

### 10.3 资源标注

所有来自文博资源的展品，观众浏览时自动显示来源标注：
- 博物馆名称
- 文物原名
- 时代/分类
- 数据来源标识

---

## 11. 发布与社区

### 11.1 发布流程

```
学生在编辑器保存 → 从任务提交 → 教师审核 → 通过(approved)
                                │               └─→ 自动发布 或 手动发布 → published
                                └── 退回(returned) → 学生修改 → 重新提交
```

**approved → published 迁移规则**：

- 任务可配置 `auto_publish_after_approval`：
  - `true`：审核通过后自动创建 published version，设置 `published_version_id`，状态转为 `published`
  - `false`：审核通过后停留在 `approved`，由教师/管理员手动点“发布”
- 自由创作（`task_id = NULL`）走管理员审核流程，模式相同

### 11.2 可见性与状态（三维模型）

> 已在 3.7 节定义。此处补充产品级说明。

| 维度 | 字段 | 产品含义 |
|------|------|----------|
| `workflow_status` | draft→submitted→reviewing→approved/returned→published | 展厅生命周期，决定“能做什么” |
| `visibility_scope` | private/class/school/public | 发布后“谁能看”，与工作流无关 |
| `is_featured` | 0/1 | 教师/管理员标记精选，与发布无关 |

**常见组合示例**：
- 已审核通过 + 班级可见 + 非精选
- 已发布 + 公开 + 精选推荐
- 已发布 + 校内 + 精选（校内推荐作品）

### 11.3 社区互动

- **评论**：`exhibition_comments` 已有，需在 Viewer 中增加发评论 UI
- **点赞/收藏**：`exhibition_interactions` 已有，需在 Viewer 中增加按钮
- **互评**：教师配置互评模板 → 学生按模板评价他人展厅
- **分享**：生成分享链接 / 二维码

---

## 12. 前端路由与布局

### 12.1 新增/改造路由

```typescript
// 展厅相关路由
{ path: '/exhibitions',                    component: ExhibitionListView },
{ path: '/exhibitions/create',             component: ExhibitionCreateView },  // 改造：增加模板选择步骤
{ path: '/exhibitions/:id',                component: ExhibitionDetailView },
{ path: '/exhibitions/:id/editor',         component: Editor },               // 改造：多展区编辑器
{ path: '/exhibitions/:id/view',           component: ExhibitionViewer },      // 改造：沉浸式参观

// 教师端新增路由
{ path: '/tasks/:taskId/submissions',      component: TaskSubmissionsView },   // 新增
{ path: '/submissions/:id/review',         component: SubmissionReviewView },  // 新增
{ path: '/tasks/:taskId/resources',        component: TaskResourcesView },     // 新增
{ path: '/tasks/:taskId/analytics',        component: TaskAnalyticsView },     // 新增
```

### 12.2 角色权限

| 路由 | student | teacher | admin |
|------|---------|---------|-------|
| 创建展厅 | ✅ | ✅ | ✅ |
| 编辑展厅 | ✅(own/member) | ✅ | ✅ |
| 浏览展厅 | ✅(visibility) | ✅ | ✅ |
| 提交审核 | ✅(owner) | - | - |
| 审核评分 | - | ✅(task creator) | ✅ |
| 推荐展示 | - | ✅ | ✅ |
| 班级作品管理 | - | ✅ | ✅ |

---

## 13. 实现优先级与分期

### 13.1 P0（MVP 核心链路）— 预估 4-6 周

> 目标：跑通“教师发任务→学生选模板创建展厅→多展区编辑→AI讲解→提交→教师审核评分→发布浏览”完整闭环。
>
> **P0 瘦身原则**：热点由模板预置，学生不编辑；展品点击区从槽位自动生成；数字人固定右下角。

| # | 任务 | 涉及 |
|---|------|------|
| 1 | DB 迁移：新增 8 张表（含 submission/review）+ 修改 exhibitions/versions | 后端 |
| 2 | 展区/展品 CRUD API + editor-bundle 整包接口 | 后端 |
| 3 | 展馆模板数据初始化（单一 2.5D 模板，固定 5 展区）+ 模板实例化逻辑 | 后端+资源 |
| 4 | 创建展厅流程改造：增加模板选择步骤 | 前端 |
| 5 | Editor.vue 改造：三层渲染+展区切换+展品编辑（热点=模板预置，不可编辑） | 前端（核心） |
| 6 | ExhibitionViewer 改造：场景浏览+展品弹窗（热点导航从模板自动生成） | 前端 |
| 7 | AI 讲解词生成 API + 编辑器集成（含降级） | 前端+后端 |
| 8 | 基础数字人讲解（固定右下角+文本气泡+展品跟随，无TTS） | 前端 |
| 9 | 提交审核流程（submission/review全链路）+ 教师审核评价页 | 前端+后端 |
| 10 | 版本保存格式升级(v2)+兼容旧版+自动保存与版本分离 | 前后端 |

### 13.2 P1（体验增强）— 预估 3-4 周

| # | 任务 |
|---|------|
| 1 | **热点可视化编辑器**（展区热点拖拽/属性面板/碰撞检测） |
| 2 | 文博资源深度集成（自动标注来源、元数据展示） |
| 3 | 点赞/收藏/评论功能完善（Viewer 内发评论抽屉） |
| 4 | 模板化互评系统 |
| 5 | 团队协作完整 UI：展区认领 + 编辑锁心跳 + 操作日志 + 本地草稿恢复 |
| 6 | AI 语音合成（TTS）+ 数字人语音讲解 |
| 7 | 教师数据看板（完成率/浏览量/参与情况） |
| 8 | 资源分发功能（教师为任务配置资源包） |
| 9 | 数字人每展区自定义位置 |
| 10 | 展品轮播/画廊模式 |

### 13.3 P2（长期优化）

| # | 任务 |
|---|------|
| 1 | 更多展馆模板（时间轴/地图探索） |
| 2 | 数字人 AI 问答（限定上下文） |
| 3 | 版本对比（per-zone diff） |
| 4 | 浏览热力图 |
| 5 | 高级 AI 评价建议 |
| 6 | 校内/区域展厅聚合 |
| 7 | 实时协同编辑（WebSocket） |

### 13.4 关键注意事项

- **场景背景图**是 2.5D 效果的核心，初期至少需要一套完整的 5 张场景图（入口+3展区+出口）
- **版本格式兼容**必须第一优先处理，避免破坏已有展厅数据
- **编辑器改造风险最高**，建议先完成 `EditorCanvas` + `useZoneManager` + `useSceneRenderer` 再逐步集成其他面板
- **AI API 需要真实接入**，建议预留 mock 模式用于开发测试
- **音频自动播放受浏览器限制**：数字人欢迎语不能自动播报，必须先显示欢迎气泡，用户点“开始参观”后才允许音频播放
- **模板实例化必须在创建时完成**，不允许运行时动态依赖模板本体（见 4.4）
- **P0 不做热点自由编辑**，由模板预置导航热点，展品点击区从槽位自动生成，降低前端复杂度
- **P0 热点表仅用于导航**：`zone_hotspots.hotspot_type` 在 P0 仅支持 `navigation` / `external_link` / `narration_trigger`，展品点击区由槽位自动生成 overlay，不走 hotspot 表。`exhibit_popup` 类型预留给 P1+ 的自定义热点编辑器

---

## 14. 运行时规则附录

> v1.2 新增。汇集前后端联调前必须对齐的 5 条关键运行时规则。

### 14.1 Viewer 读取数据源规则

| 场景 | 读取数据源 | 说明 |
|------|-----------|------|
| 编辑器继续编辑 | 当前草稿态：`exhibition_zones.canvas_data` + `exhibition_exhibits` | 实时数据 |
| 编辑器内预览 | 同上（草稿态） | 预览 = 用编辑数据渲染只读 Viewer |
| 教师审核 | `exhibition_submissions.submitted_version_id` 对应的冻结版本 | 必须读冻结版本，不能读草稿 |
| 历史版本回看 | 指定 `version_id` 的 `version_data` | 任意历史版本 |
| 对外公开浏览 | `exhibitions.published_version_id` 对应的 published 版本 | 如果为 NULL 则展厅不可公开访问 |

> **关键约束**：审核期间学生可以继续编辑草稿，但教师看到的始终是提交时的冻结版本。公开浏览读 `published_version_id`，不会因作者继续编辑而变化。

### 14.2 workflow_status 与 submission.status 同步规则

**事实源**：`exhibition_submissions.status` 是每次提交的真实状态。

**聚合态**：`exhibitions.workflow_status` 是展厅当前面向界面的汇总状态。

| 业务动作 | submission 变化 | exhibition 变化 | 触发方 |
|---------|----------------|-----------------|-------|
| 创建展厅 | — | `workflow_status = draft` | 系统 |
| 发起提交 | 创建 submission（`status=submitted`） | `workflow_status = submitted` | 学生（组长） |
| 教师开始审核 | `status = reviewing` | `workflow_status = reviewing` | 教师 |
| 退回 | `status = returned` | `workflow_status = returned` | 教师 |
| 审核通过 | `status = approved` | `workflow_status = approved` | 教师 |
| 发布 | — | `workflow_status = published`，设置 `published_version_id` | 系统/教师 |
| 退回后重新提交 | 创建新 submission（`submit_count+1`） | `workflow_status = submitted` | 学生 |

> **强制规则**：`workflow_status` 不允许前端直接写入，只能通过业务服务层的提交/审核/发布操作间接更新。

### 14.3 自动保存覆盖范围

| 内容类型 | 是否自动保存 | 存储目标 |
|---------|------------|---------|
| Fabric 画布 JSON | ✅ | `exhibition_zones.canvas_data` |
| 展品标题/描述 | ✅ | `exhibition_exhibits` 行 |
| 展品讲解词 | ✅ | `exhibit_narrations` 行 |
| 展品互动题 | ✅ | `exhibit_interactions` 行 |
| 展区标题/讲解文本 | ✅ | `exhibition_zones` 行 |
| 展厅全局设置 | ❌（需手动保存） | `exhibitions` 行 |

接口：`PUT /api/v1/exhibitions/{id}/draft-bundle`，按展区粒度提交。

### 14.4 editor-bundle 冲突策略

```
前端 GET /editor-bundle → 拿到 revision=12
用户编辑...
前端 PUT /editor-bundle {revision:12, ...}
  ├── 后端 revision=12 → 保存成功，返回 {revision:13}
  └── 后端 revision≠12 → 返回 409 Conflict
      → 前端弹窗："展厅内容已被其他成员更新，请刷新后重试"
      → 用户点刷新 → GET /editor-bundle 拿到最新数据
```

- `draft-bundle`（自动保存）不校验 revision，按展区粒度写入，不存在整包冲突
- `editor-bundle`（手动保存/提交）必须校验 revision
- 如果后续需要更精细的合并策略（per-zone merge），放到 P2

### 14.5 approved 与 published 状态迁移

```
draft → submitted → reviewing → approved → published
                       ↓                       ↑
                    returned → (重新提交) → submitted → ...
```

- `approved` ≠ `published`：审核通过只是"可以发布"，不一定立即对外
- 任务配置 `auto_publish_after_approval`：
  - `true` → 自动创建 `version_type=published` 版本，设置 `published_version_id`
  - `false` → 停留在 `approved`，等待手动发布
- 发布时必须同时设置 `published_version_id`，否则公开浏览接口无法找到内容
- `is_featured` 独立于发布状态，可以在 `published` 之后任意标记/取消
