# 《知行创境》教育创作平台数据库设计

## 1. 设计原则

- 数据库类型：MySQL 8.0
- 字符集：`utf8mb4`
- 存储引擎：`InnoDB`
- 主键策略：`BIGINT UNSIGNED AUTO_INCREMENT`
- 设计目标：
  - 满足学生创作、教师布置任务、班级协作、社区展示、AI 数字人、博物馆资源缓存六大场景
  - 兼顾展厅编辑性能与后续扩展性
  - 对高频查询字段建立复合索引

## 2. 核心建模说明

### 2.1 用户与组织

- `schools`：学校
- `classes`：班级
- `users`：账户
- `class_members`：班级成员关系
- `announcements`：班级公告

### 2.2 任务与教学流程

- `tasks`：教师发布的项目式学习任务
- `task_target_classes`：任务投放到哪些班级
- `task_materials`：任务背景资料
- `task_submissions`：学生/小组提交记录
- `submission_reviews`：教师评分与点评

### 2.3 展厅创作

- `exhibitions`：展厅主表
- `exhibition_members`：展厅协作成员
- `exhibition_versions`：展厅版本快照
- `exhibition_components`：版本中的组件明细
- `media_assets`：上传素材元数据

### 2.4 AI 数字人与博物馆资源

- `digital_humans`：展厅中的数字角色
- `museum_providers`：文博机构来源
- `museum_resources`：外部文博资源缓存
- `digital_human_equipments`：数字人绑定装备

### 2.5 社区互动

- `exhibition_tags`：主题标签
- `exhibition_tag_relations`：展厅标签关系
- `exhibition_comments`：评论
- `comment_mentions`：评论 @ 关系
- `exhibition_interactions`：点赞/收藏/分享
- `notifications`：消息通知

## 3. 关键关系

- 一个学校有多个班级
- 一个用户可以属于多个班级，通过 `class_members` 维护
- 一个任务可投放多个班级，通过 `task_target_classes` 维护
- 一个任务通常对应多个小组展厅，每个展厅由一个或多个学生共同维护
- 一个展厅可有多个历史版本，前端编辑器读取最近草稿版本，观众模式读取已发布版本
- 一个展厅最多维护一个主要 AI 数字人；如后续需要多角色，可将 `digital_humans` 扩展为一对多
- 博物馆资源先落本地缓存，再绑定给数字人装备
- 社区互动统一沉淀在 `exhibition_interactions` 与 `exhibition_comments`

## 4. 表设计

## 4.1 schools

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| school_name | varchar(128) | NOT NULL | 学校名称 |
| school_code | varchar(64) | UNIQUE | 学校编码 |
| province | varchar(64) |  | 省份 |
| city | varchar(64) |  | 城市 |
| district | varchar(64) |  | 区县 |
| status | varchar(20) | NOT NULL | `enabled/disabled` |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

## 4.2 classes

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| school_id | bigint unsigned | FK | 学校 ID |
| class_name | varchar(128) | NOT NULL | 班级名称 |
| grade_level | varchar(32) |  | 年级 |
| academic_year | varchar(20) |  | 学年 |
| head_teacher_id | bigint unsigned | FK | 班主任/负责人 |
| status | varchar(20) | NOT NULL | `active/inactive` |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

## 4.3 users

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| school_id | bigint unsigned | FK | 所属学校 |
| account | varchar(64) | UNIQUE | 登录账号 |
| password_hash | varchar(255) | NOT NULL | 密码哈希 |
| role | varchar(20) | NOT NULL | `student/teacher/parent/admin/visitor` |
| real_name | varchar(64) | NOT NULL | 真实姓名 |
| nickname | varchar(64) |  | 昵称 |
| avatar_url | varchar(255) |  | 头像 |
| gender | varchar(10) |  | 性别 |
| email | varchar(128) | UNIQUE | 邮箱 |
| mobile | varchar(32) | UNIQUE | 手机号 |
| bio | varchar(255) |  | 个人简介 |
| last_login_at | datetime |  | 最近登录时间 |
| status | varchar(20) | NOT NULL | `active/inactive/locked` |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

## 4.4 class_members

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| class_id | bigint unsigned | FK | 班级 ID |
| user_id | bigint unsigned | FK | 用户 ID |
| member_role | varchar(20) | NOT NULL | `student/teacher/assistant` |
| joined_at | datetime | NOT NULL | 加入时间 |
| status | varchar(20) | NOT NULL | `active/inactive` |

唯一键：`uk_class_user(class_id, user_id)`

## 4.5 announcements

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| class_id | bigint unsigned | FK | 班级 ID |
| publisher_id | bigint unsigned | FK | 发布人 |
| title | varchar(128) | NOT NULL | 公告标题 |
| content | text | NOT NULL | 公告内容 |
| pinned | tinyint(1) | NOT NULL | 是否置顶 |
| published_at | datetime | NOT NULL | 发布时间 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

## 4.6 tasks

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| creator_id | bigint unsigned | FK | 教师 ID |
| title | varchar(128) | NOT NULL | 任务标题 |
| cover_url | varchar(255) |  | 封面图 |
| description | text | NOT NULL | 任务描述 |
| evaluation_criteria | text |  | 评价标准 |
| start_time | datetime |  | 开始时间 |
| due_time | datetime |  | 截止时间 |
| status | varchar(20) | NOT NULL | `draft/published/closed/archived` |
| excellent_count | int unsigned | NOT NULL | 优秀作品数量 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

## 4.7 task_target_classes

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| task_id | bigint unsigned | FK | 任务 ID |
| class_id | bigint unsigned | FK | 班级 ID |
| created_at | datetime | NOT NULL | 创建时间 |

唯一键：`uk_task_class(task_id, class_id)`

## 4.8 task_materials

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| task_id | bigint unsigned | FK | 任务 ID |
| title | varchar(128) | NOT NULL | 资料标题 |
| material_type | varchar(20) | NOT NULL | `image/video/audio/link/document` |
| file_url | varchar(255) |  | 文件地址 |
| external_url | varchar(255) |  | 外链地址 |
| description | varchar(255) |  | 简介 |
| sort_no | int unsigned | NOT NULL | 排序 |
| created_at | datetime | NOT NULL | 创建时间 |

## 4.9 media_assets

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| owner_id | bigint unsigned | FK | 上传人 |
| asset_type | varchar(20) | NOT NULL | `image/video/audio/document/model` |
| source_type | varchar(20) | NOT NULL | `upload/platform/museum` |
| file_name | varchar(255) | NOT NULL | 文件名 |
| file_url | varchar(255) | NOT NULL | 文件地址 |
| file_ext | varchar(20) |  | 扩展名 |
| mime_type | varchar(100) |  | MIME |
| file_size | bigint unsigned |  | 文件大小 |
| width | int unsigned |  | 宽 |
| height | int unsigned |  | 高 |
| duration_seconds | int unsigned |  | 时长 |
| checksum_md5 | varchar(32) |  | MD5 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

## 4.10 exhibitions

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| task_id | bigint unsigned | FK | 关联任务 |
| owner_id | bigint unsigned | FK | 发起人 |
| title | varchar(128) | NOT NULL | 展厅标题 |
| cover_url | varchar(255) |  | 封面图 |
| summary | varchar(500) |  | 摘要 |
| group_name | varchar(128) |  | 小组名称 |
| status | varchar(20) | NOT NULL | `draft/published/archived` |
| visibility | varchar(20) | NOT NULL | `private/class/public` |
| latest_version_no | int unsigned | NOT NULL | 最新版本号 |
| published_version_no | int unsigned | NOT NULL | 已发布版本号 |
| featured_flag | tinyint(1) | NOT NULL | 是否加精推荐 |
| featured_reason | varchar(255) |  | 推荐理由 |
| featured_at | datetime |  | 推荐时间 |
| view_count | int unsigned | NOT NULL | 浏览数 |
| like_count | int unsigned | NOT NULL | 点赞数 |
| favorite_count | int unsigned | NOT NULL | 收藏数 |
| comment_count | int unsigned | NOT NULL | 评论数 |
| published_at | datetime |  | 发布时间 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

## 4.11 exhibition_members

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| exhibition_id | bigint unsigned | FK | 展厅 ID |
| user_id | bigint unsigned | FK | 用户 ID |
| member_role | varchar(20) | NOT NULL | `owner/editor/viewer` |
| joined_at | datetime | NOT NULL | 加入时间 |
| status | varchar(20) | NOT NULL | `active/inactive` |

唯一键：`uk_exhibition_user(exhibition_id, user_id)`

## 4.12 exhibition_versions

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| exhibition_id | bigint unsigned | FK | 展厅 ID |
| version_no | int unsigned | NOT NULL | 版本号 |
| save_type | varchar(20) | NOT NULL | `manual/autosave/publish` |
| version_note | varchar(255) |  | 版本备注 |
| canvas_width | int unsigned | NOT NULL | 画布宽度 |
| canvas_height | int unsigned | NOT NULL | 画布高度 |
| canvas_background | varchar(64) |  | 画布背景 |
| zoom_ratio | decimal(6,2) | NOT NULL | 缩放比例 |
| element_count | int unsigned | NOT NULL | 元素数量 |
| version_data | json | NOT NULL | 版本快照 JSON |
| created_by | bigint unsigned | FK | 保存人 |
| created_at | datetime | NOT NULL | 创建时间 |

唯一键：`uk_exhibition_version(exhibition_id, version_no)`

## 4.13 exhibition_components

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| version_id | bigint unsigned | FK | 版本 ID |
| component_key | varchar(64) | NOT NULL | 前端组件唯一键 |
| component_type | varchar(32) | NOT NULL | `showcase/frame/label/image/video/model/digital_human` |
| title | varchar(128) |  | 组件标题 |
| asset_id | bigint unsigned | FK | 关联素材 |
| x | decimal(10,2) | NOT NULL | 横坐标 |
| y | decimal(10,2) | NOT NULL | 纵坐标 |
| width | decimal(10,2) | NOT NULL | 宽 |
| height | decimal(10,2) | NOT NULL | 高 |
| z_index | int | NOT NULL | 层级 |
| rotation | decimal(8,2) | NOT NULL | 旋转角度 |
| style_json | json |  | 样式配置 |
| content_json | json |  | 内容配置 |
| interaction_json | json |  | 交互配置 |
| created_at | datetime | NOT NULL | 创建时间 |

唯一键：`uk_version_component(version_id, component_key)`

## 4.14 digital_humans

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| exhibition_id | bigint unsigned | FK | 展厅 ID |
| name | varchar(64) | NOT NULL | 数字人名称 |
| avatar_2d_url | varchar(255) |  | 2D 形象 |
| model_3d_url | varchar(255) |  | 3D 模型 |
| persona | varchar(255) |  | 角色设定 |
| voice_type | varchar(64) |  | 音色类型 |
| story_script | longtext |  | 故事脚本 |
| story_timeline | json |  | 故事时间线 |
| sort_no | int unsigned | NOT NULL | 排序 |
| status | varchar(20) | NOT NULL | `active/inactive` |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

唯一键：`uk_digital_human_exhibition(exhibition_id)`

## 4.15 museum_providers

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| provider_code | varchar(64) | UNIQUE | 机构编码 |
| provider_name | varchar(128) | NOT NULL | 机构名称 |
| api_base_url | varchar(255) |  | 外部 API 地址 |
| status | varchar(20) | NOT NULL | `enabled/disabled` |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

## 4.16 museum_resources

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| provider_id | bigint unsigned | FK | 来源机构 |
| external_id | varchar(128) | NOT NULL | 外部资源 ID |
| resource_type | varchar(20) | NOT NULL | `image/video/audio/model/document` |
| category | varchar(64) | NOT NULL | 分类，如 `uniform/equipment/living` |
| title | varchar(255) | NOT NULL | 资源标题 |
| subtitle | varchar(255) |  | 副标题 |
| museum_name | varchar(128) |  | 博物馆名称 |
| cover_url | varchar(255) |  | 封面图 |
| detail_url | varchar(255) |  | 详情地址 |
| description | text |  | 描述 |
| tags_json | json |  | 标签 |
| metadata_json | json |  | 扩展元数据 |
| raw_payload | json |  | 原始响应 |
| cache_status | varchar(20) | NOT NULL | `fresh/expired/error` |
| synced_at | datetime |  | 同步时间 |
| cache_expire_at | datetime |  | 缓存失效时间 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

唯一键：`uk_provider_external(provider_id, external_id)`

## 4.17 digital_human_equipments

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| digital_human_id | bigint unsigned | FK | 数字人 ID |
| slot_code | varchar(64) | NOT NULL | 装备槽位，如 `uniform` |
| museum_resource_id | bigint unsigned | FK | 关联文博资源 |
| anchor_code | varchar(64) |  | 故事锚点编码 |
| display_order | int unsigned | NOT NULL | 排序 |
| resource_snapshot_json | json | NOT NULL | 绑定时资源快照 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

## 4.18 task_submissions

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| task_id | bigint unsigned | FK | 任务 ID |
| exhibition_id | bigint unsigned | FK | 展厅 ID |
| submitter_id | bigint unsigned | FK | 提交人 |
| version_no | int unsigned | NOT NULL | 提交的版本号 |
| submit_remark | varchar(255) |  | 提交说明 |
| submission_status | varchar(20) | NOT NULL | `submitted/reviewed/returned` |
| submitted_at | datetime | NOT NULL | 提交时间 |
| reviewed_at | datetime |  | 评审时间 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

## 4.19 submission_reviews

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| submission_id | bigint unsigned | FK | 提交记录 ID |
| reviewer_id | bigint unsigned | FK | 教师 ID |
| score | decimal(5,2) |  | 分数 |
| comment_text | text |  | 文字点评 |
| comment_audio_url | varchar(255) |  | 语音点评 |
| is_public | tinyint(1) | NOT NULL | 是否公开展示 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

唯一键：`uk_submission_reviewer(submission_id, reviewer_id)`

## 4.20 exhibition_tags

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| tag_name | varchar(64) | UNIQUE | 标签名 |
| tag_type | varchar(20) | NOT NULL | `theme/grade/custom` |
| created_at | datetime | NOT NULL | 创建时间 |

## 4.21 exhibition_tag_relations

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| exhibition_id | bigint unsigned | FK | 展厅 ID |
| tag_id | bigint unsigned | FK | 标签 ID |
| created_at | datetime | NOT NULL | 创建时间 |

唯一键：`uk_exhibition_tag(exhibition_id, tag_id)`

## 4.22 exhibition_comments

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| exhibition_id | bigint unsigned | FK | 展厅 ID |
| user_id | bigint unsigned | FK | 评论人 |
| parent_comment_id | bigint unsigned | FK | 父评论 |
| root_comment_id | bigint unsigned | FK | 根评论 |
| content | text | NOT NULL | 评论内容 |
| status | varchar(20) | NOT NULL | `normal/deleted` |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

## 4.23 comment_mentions

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| comment_id | bigint unsigned | FK | 评论 ID |
| mentioned_user_id | bigint unsigned | FK | 被 @ 用户 ID |
| created_at | datetime | NOT NULL | 创建时间 |

## 4.24 exhibition_interactions

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| exhibition_id | bigint unsigned | FK | 展厅 ID |
| user_id | bigint unsigned | FK | 用户 ID |
| interaction_type | varchar(20) | NOT NULL | `like/favorite/share` |
| channel | varchar(32) | NOT NULL | 分享渠道，点赞/收藏时可置空字符串 |
| created_at | datetime | NOT NULL | 创建时间 |

唯一键：`uk_exhibition_user_interaction(exhibition_id, user_id, interaction_type, channel)`

## 4.25 notifications

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint unsigned | PK | 主键 |
| user_id | bigint unsigned | FK | 接收人 |
| notification_type | varchar(32) | NOT NULL | `system/comment/like/review/announcement` |
| title | varchar(128) | NOT NULL | 通知标题 |
| content | varchar(255) | NOT NULL | 通知内容 |
| biz_type | varchar(32) |  | 业务类型 |
| biz_id | bigint unsigned |  | 业务主键 |
| read_status | varchar(20) | NOT NULL | `unread/read` |
| read_at | datetime |  | 已读时间 |
| created_at | datetime | NOT NULL | 创建时间 |

## 5. 关键索引建议

- `users`：`idx_users_role_status(role, status)`、`idx_users_school(school_id)`
- `classes`：`idx_classes_school_grade(school_id, grade_level)`
- `tasks`：`idx_tasks_creator_status(creator_id, status)`、`idx_tasks_due_time(due_time)`
- `exhibitions`：`idx_exhibitions_task_status(task_id, status)`、`idx_exhibitions_featured(featured_flag, published_at)`、`idx_exhibitions_owner(owner_id, updated_at)`
- `exhibition_versions`：`idx_versions_exhibition_created(exhibition_id, created_at)`
- `museum_resources`：`idx_museum_category(category, cache_status)`、`idx_museum_expire(cache_expire_at)`
- `task_submissions`：`idx_submissions_task_status(task_id, submission_status)`、`idx_submissions_exhibition(exhibition_id)`
- `exhibition_comments`：`idx_comments_exhibition_created(exhibition_id, created_at)`、`idx_comments_parent(parent_comment_id)`
- `notifications`：`idx_notifications_user_read(user_id, read_status, created_at)`

## 6. 落库策略建议

- 展厅编辑器高频写入时，只落 `exhibition_versions.version_data` 快照即可；`exhibition_components` 可由服务端同步拆解，便于统计与检索
- 浏览数、点赞数、评论数、收藏数建议采用“主表冗余 + 异步校正”
- 博物馆资源表建议设置定时刷新任务，对 `cache_expire_at` 已过期的数据增量更新
- 用户敏感信息如邮箱、手机号建议在应用层加密后入库

## 7. 与接口文档的映射关系

- `POST /tasks` -> `tasks`、`task_target_classes`、`task_materials`
- `POST /exhibitions` -> `exhibitions`、`exhibition_members`
- `POST /exhibitions/{id}/versions` -> `exhibition_versions`、`exhibition_components`
- `PUT /exhibitions/{id}/digital-human` -> `digital_humans`
- `POST /digital-humans/{id}/equipments` -> `digital_human_equipments`
- `GET /museum/resources` -> `museum_resources`
- `POST /tasks/{id}/submit` -> `task_submissions`
- `POST /submissions/{id}/reviews` -> `submission_reviews`
- `POST /community/exhibitions/{id}/comments` -> `exhibition_comments`、`comment_mentions`
- `POST /community/exhibitions/{id}/like` -> `exhibition_interactions`
