# 数字展馆升级 Phase 4：热点编辑器 + 观众互动落库 + 互评 + 教师看板

> **面向 AI 代理的工作者**：逐任务实现此计划。步骤使用复选框（`- [ ]`）语法跟踪进度。
> 与 Phase 3 相同，保持「后端先行 → 前端跟进 → 验证」的节奏。

**目标**：补齐 Phase 3 遗留能力空白，使项目达到「可演示闭环」状态——观众端可以持久化互动，教师可以看到班级数据，学生可以互评，热点可在编辑器中完整创建/编辑。

**Phase 4 不做**：实时协同（WS/CRDT）、版本格式 v3、真实 LLM 接入、内容机审、移动端深度适配——留给 Phase 5+。

**技术栈**：沿用 Phase 3——Spring Boot 3 / Java 21 / JdbcTemplate / Vue 3.5 / TS 5.6 / Fabric 7.3.1 / Tailwind 3。

**前置条件**：
- Phase 3 commit `1db7af3` 及之后。
- 数据库执行 `V2__exhibition_upgrade.sql`（已含 `zone_hotspots` / `peer_reviews` / `peer_review_templates` / `community_interactions`）。
- 所有 11/12 Phase 3 任务已完成并验证。

---

## 任务总览（12 项，按依赖序）

| # | 任务 | 优先级 | 前/后端 | 预估工作量 |
|---|------|------|------|----------|
| 1 | 后端热点 CRUD 完整化（update / delete / upsert） | P0 | 后端 | S |
| 2 | 后端热点 Controller + DTO | P0 | 后端 | S |
| 3 | 前端热点 API 模块 + 类型 | P0 | 前端 | S |
| 4 | 前端热点编辑器 UI（覆盖层 + 属性面板） | P0 | 前端 | L |
| 5 | 前端观众互动替换为后端 API（点赞 / 收藏） | P0 | 前端 | S |
| 6 | 后端互评服务 + 互评模板 API | P1 | 后端 | M |
| 7 | 前端互评填写页 + 教师互评模板配置页 | P1 | 前端 | M |
| 8 | 后端教师数据看板 API（班级/任务/审核聚合） | P1 | 后端 | M |
| 9 | 前端数据看板图表页 | P1 | 前端 | M |
| 10 | 展区/展品拖拽重排（前端） | P2 | 前端 | S |
| 11 | TTS Mock API + 前端接入讲解音频 | P2 | 全栈 | M |
| 12 | Phase 4 端到端集成验证 | - | - | S |

**体量**：S ≈ 0.5-1 天，M ≈ 1-2 天，L ≈ 2-3 天。Phase 4 合计 ≈ 15-20 天工作量。

---

## 任务 1：后端热点 CRUD 完整化

**文件**
- 修改：`backend/src/main/java/com/zhixingchuangjing/platform/repository/HotspotCommandRepository.java`

**当前状态**：仅有 `createHotspot`，无 update / delete / upsert。

- [ ] **步骤 1**：增加 `updateHotspot(Long id, ...)` 方法，支持编辑所有字段（target / type / label / icon / x / y / w / h / style_json / action_config / sort_order）。
- [ ] **步骤 2**：增加 `deleteHotspot(Long id)` 方法。
- [ ] **步骤 3**：增加 `batchUpsertHotspots(Long zoneId, List<HotspotUpsertItem>)`：
  - 如果 `id == null` 走 INSERT；否则 UPDATE。
  - 用于编辑器「保存整个热点数组」场景（简化前端）。
- [ ] **步骤 4**：给 `HotspotCommandRepository` 加 `@Transactional`（批量场景）。

**验证**：添加 Repository 层单元测试（可选），或在任务 2 通过 controller 端到端验证。

---

## 任务 2：后端热点 Controller + DTO

**文件**
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/model/request/HotspotRequests.java`（`CreateHotspotRequest`、`UpdateHotspotRequest`、`BatchUpsertHotspotsRequest`）
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/service/HotspotService.java`
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/service/impl/HotspotServiceImpl.java`
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/controller/HotspotController.java`

**API 设计**
- `GET /api/v1/exhibitions/{exhibitionId}/zones/{zoneId}/hotspots` 列表
- `POST /api/v1/exhibitions/{exhibitionId}/zones/{zoneId}/hotspots` 创建
- `PUT /api/v1/exhibitions/{exhibitionId}/hotspots/{hotspotId}` 更新
- `DELETE /api/v1/exhibitions/{exhibitionId}/hotspots/{hotspotId}` 删除
- `PUT /api/v1/exhibitions/{exhibitionId}/zones/{zoneId}/hotspots/batch` 批量 upsert

- [ ] **步骤 1**：DTO 完整定义，`@NotNull` 校验 zone_id / type / x_percent / y_percent。
- [ ] **步骤 2**：Service 接口 + 实现，权限校验复用 `assertCanEdit(exhibitionId, userId, role)`。
- [ ] **步骤 3**：Controller 路由挂载，`@AuthenticationPrincipal SecurityUserDetails`。
- [ ] **步骤 4**：返回值与 `editor-bundle` 的 `HotspotResponse` 一致，确保前端复用类型。

**验证**：`mvn compile` 通过，`mvn spring-boot:run` 启动无 bean 冲突。

---

## 任务 3：前端热点 API 模块 + 类型

**文件**
- 修改：`frontend/src/api/types.ts`（增加 `CreateHotspotRequest`、`UpdateHotspotRequest`、`BatchUpsertHotspotsRequest`）
- 创建：`frontend/src/api/modules/hotspots.ts`

- [ ] **步骤 1**：类型补全，字段与后端一一对应。
- [ ] **步骤 2**：导出 `listHotspots / createHotspot / updateHotspot / deleteHotspot / batchUpsertHotspots` 函数。
- [ ] **步骤 3**：`vue-tsc --noEmit` 无错。

---

## 任务 4：前端热点编辑器 UI

**文件**
- 创建：`frontend/src/components/exhibitions/editor/HotspotEditorLayer.vue`（画布上的热点可视化叠加层）
- 创建：`frontend/src/components/exhibitions/editor/HotspotPropertiesPanel.vue`（右栏属性编辑）
- 修改：`frontend/src/views/exhibitions/Editor.vue`（集成图层、右栏 Tab、状态管理）

**UX 设计**
- 编辑器右栏加「热点」Tab（与「展区」「展品」「元素」并列）。
- 热点 Tab 显示当前展区所有热点列表，点击选中。
- 画布叠加一层 HTML absolute 层，渲染热点方框（百分比坐标），可拖拽、可调尺寸。
- 右栏属性面板：type（navigation / exhibit_popup / external_link / narration_trigger）、target_zone_id（下拉）、label、icon、外链 URL（action_config.url）。
- 操作：点击空白处添加新热点（默认 type=navigation、尺寸 8%×8%）；选中后 Del 键删除；拖拽改 x/y；右下角 resize handle 改 w/h。

- [ ] **步骤 1**：渲染层（只读观众端复用 `HotspotButtons.vue` 即可，编辑器需新写 `HotspotEditorLayer.vue`）。
- [ ] **步骤 2**：添加 / 拖拽 / 调尺寸交互。
- [ ] **步骤 3**：属性面板 + API 调用（每次属性修改后 debounce 300ms 调 `updateHotspot`）。
- [ ] **步骤 4**：Editor.vue 集成热点管理状态（`allHotspots` ref + `currentHotspots` computed）。
- [ ] **步骤 5**：观众端 `ExhibitionViewer.vue` 的 `HotspotButtons.vue` 自动从 bundle 拿到新创建的热点（已经有了，验证联通即可）。

**验证**：创建 > 编辑 > 删除热点，切换展区后热点正确过滤，观众端能看到并点击跳转。

---

## 任务 5：前端观众互动替换为后端 API

**文件**
- 修改：`frontend/src/views/exhibitions/ExhibitionViewer.vue`
- 修改：`frontend/src/api/modules/community.ts`（确认已有 like / unlike / favorite / unfavorite 方法；若无则补）

**当前状态**：点赞 / 收藏用 localStorage 持久化（`liked_exh_<id>`、`fav_exh_<id>`）。
**后端已就绪**：
- `POST /community/{exhibitionId}/like`
- `DELETE /community/{exhibitionId}/like`
- `POST /community/{exhibitionId}/favorite`
- `DELETE /community/{exhibitionId}/favorite`
- bundle 中 `bundle.exhibition.isLiked` / `isFavorited` 字段（需要确认，无则后端补返回）。

- [ ] **步骤 1**：确认 `viewer` 数据中包含当前用户互动状态；若缺失则在 `CommunityServiceImpl.getCommunityDetail` / `ExhibitionServiceImpl.getViewerData` 增加 `isLiked` / `isFavorited` 返回。
- [ ] **步骤 2**：前端初始化 `liked` / `favorited` 从 bundle 拿，而非 localStorage。
- [ ] **步骤 3**：点击按钮调用 API，成功后更新本地 ref；失败回滚（乐观更新 + 错误回退）。
- [ ] **步骤 4**：登录态未登录时提示「请先登录」。
- [ ] **步骤 5**：彻底移除 localStorage 相关代码与 key（保持语义干净）。

**验证**：不同用户登录后点赞/收藏状态独立；刷新页面状态正确；未登录用户被引导登录。

---

## 任务 6：后端互评服务 + 互评模板 API

**文件**
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/repository/PeerReviewRepository.java`（Query + Command 合一，简单场景）
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/model/request/PeerReviewRequests.java`
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/model/response/PeerReviewResponses.java`
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/service/PeerReviewService.java`
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/service/impl/PeerReviewServiceImpl.java`
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/controller/PeerReviewController.java`

**API 设计**
- `GET /api/v1/tasks/{taskId}/peer-review-templates` 获取任务的互评模板问题列表（学生 + 老师都可调）
- `PUT /api/v1/tasks/{taskId}/peer-review-templates`（教师）批量保存互评模板
- `GET /api/v1/exhibitions/{exhibitionId}/peer-reviews` 获取该展厅收到的互评（所有人的答案，当前用户可见）
- `POST /api/v1/exhibitions/{exhibitionId}/peer-reviews`（学生）提交自己的互评答案
- `GET /api/v1/tasks/{taskId}/my-peer-review-status` 返回我需要评哪些同学的作品、已评哪些

**领域规则**
- 同一用户对同一展厅的同一问题只能答一次（`uk_peer_review_once` 已在表中）。
- 互评模板必须先由老师配置，学生才能看到互评入口。
- 互评仅在展厅 `workflow_status = 'approved'` 后开放。

- [ ] **步骤 1**：Repository 提供 upsert answer、查询 answer、查询 template、计算「待评清单」（from 同任务下其他同学的 approved 展厅）。
- [ ] **步骤 2**：Service 做权限与业务规则。
- [ ] **步骤 3**：Controller + DTO 完整。
- [ ] **步骤 4**：在 `SubmissionDetailView` / `CommunityDetailView` 预留对 peer-reviews 的展示钩子（实际 UI 在任务 7）。

---

## 任务 7：前端互评填写页 + 教师互评模板配置页

**文件**
- 创建：`frontend/src/api/modules/peer-reviews.ts`
- 创建：`frontend/src/views/tasks/TaskPeerReviewTemplatesView.vue`（教师配置）
- 创建：`frontend/src/views/tasks/MyPeerReviewsView.vue`（学生填写）
- 创建：`frontend/src/components/peer-reviews/PeerReviewForm.vue`
- 修改：`frontend/src/router/routes/index.ts`（新增路由）
- 修改：`frontend/src/views/tasks/TaskDetailView.vue`（教师入口「配置互评模板」）
- 修改：`frontend/src/views/tasks/TaskSubmissionsView.vue`（教师入口「查看互评结果」）
- 修改：`frontend/src/views/tasks/TaskListView.vue`（学生入口「去互评」）

**UX**
- 教师模板配置页：每条问题一行（text / rating / choice），支持增删改，批量保存。
- 学生互评页：先列出待评清单（同任务下其他 approved 作品），点某个 → 跳入填写页 → 逐条题目作答 → 提交。
- 评分题：1-5 星；选择题：单选；文本题：textarea max 500。

- [ ] **步骤 1**：API 模块。
- [ ] **步骤 2**：教师模板配置页（仿 `TaskCreateView.vue` 的表单风格）。
- [ ] **步骤 3**：学生互评列表 + 填写页。
- [ ] **步骤 4**：入口链接挂接。
- [ ] **步骤 5**：`vue-tsc` + `vite build` 通过。

---

## 任务 8：后端教师数据看板 API

**文件**
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/model/response/DashboardResponses.java`
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/repository/DashboardQueryRepository.java`
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/service/DashboardService.java`
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/service/impl/DashboardServiceImpl.java`
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/controller/DashboardController.java`

**API 设计**（仅教师可调）
- `GET /api/v1/dashboard/overview` 返回：
  ```json
  {
    "classCount": 3,
    "taskCount": 12,
    "activeStudentCount": 45,
    "exhibitionCount": { "draft": 10, "submitted": 5, "approved": 8, "published": 20 },
    "recentSubmissions": [...]
  }
  ```
- `GET /api/v1/dashboard/classes/{classId}/stats` 班级统计：任务完成率、学生活跃度。
- `GET /api/v1/dashboard/tasks/{taskId}/progress` 任务作品进度柱状图数据。

- [ ] **步骤 1**：SQL 聚合查询（按教师 userId 过滤）。
- [ ] **步骤 2**：Service 缓存 5 分钟（Caffeine 或内存 Map + timestamp）。
- [ ] **步骤 3**：Controller 挂载，权限校验。

---

## 任务 9：前端数据看板图表页

**文件**
- 修改：`frontend/src/views/dashboard/DashboardView.vue`（当前是占位）
- 创建：`frontend/src/components/dashboard/MetricCard.vue`
- 创建：`frontend/src/components/dashboard/StatusPieChart.vue`（用 SVG 手绘或引入 `chart.js`）
- 创建：`frontend/src/components/dashboard/RecentSubmissionsList.vue`
- 创建：`frontend/src/api/modules/dashboard.ts`

- [ ] **步骤 1**：API 模块。
- [ ] **步骤 2**：首屏布局：4 个 MetricCard（班级 / 任务 / 学生 / 作品）+ 状态饼图 + 最近提交列表。
- [ ] **步骤 3**：可选：引入轻量图表库（如 `vue-chartjs` 或直接 SVG 手写，避免 bundle 膨胀）。
- [ ] **步骤 4**：登录角色判断：学生打开看板重定向到首页。

---

## 任务 10：展区/展品拖拽重排

**文件**
- 修改：`frontend/src/components/exhibitions/editor/ZoneNavigator.vue`
- 修改：`frontend/src/components/exhibitions/editor/ExhibitList.vue`
- 修改：`frontend/src/views/exhibitions/Editor.vue`（调用 `reorderZones` API）
- 后端 **展品** 需补一个 `reorderExhibits` API（如不存在）。

- [ ] **步骤 1**：确认后端 `PUT /exhibitions/{id}/zones/reorder` 和 `PUT /exhibitions/{id}/zones/{zoneId}/exhibits/reorder` 是否齐全；若无展品 reorder，补一个。
- [ ] **步骤 2**：前端引入 `vuedraggable@next`（Vue 3 兼容版）。
- [ ] **步骤 3**：拖拽结束后本地更新 `sortOrder`，调 API 持久化，失败回滚。

---

## 任务 11：TTS Mock + 前端接入

**文件**
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/controller/TtsController.java`
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/service/TtsService.java`
- 创建：`backend/src/main/java/com/zhixingchuangjing/platform/service/impl/TtsServiceImpl.java`
- 修改：`frontend/src/components/exhibitions/editor/NarrationGeneratorModal.vue`（增加「生成语音」按钮）
- 修改：`frontend/src/components/exhibitions/viewer/DigitalHumanWidget.vue`（已预留 `<audio>` 播放器）

**Mock 策略**：后端接收文本返回一个占位音频 URL（静态 mp3 或 data URL），不真接 TTS 服务。保留接口，未来接入阿里 / 百度 / 微软 TTS。

- [ ] **步骤 1**：后端 `POST /api/v1/ai/tts` 接收 `{ text, voiceType }`，返回 `{ audioUrl, durationSeconds }`。
- [ ] **步骤 2**：前端在讲解词生成模态里「生成语音」，调用后端 TTS，保存到 `exhibit_narrations.audio_url`（复用 `upsertExhibitNarration`，补 audioUrl 字段）。
- [ ] **步骤 3**：数字人气泡自动显示 `<audio controls>`。

---

## 任务 12：Phase 4 端到端集成验证

**文件**
- 创建：`docs/plans/2025-05-05-phase4-smoke-test.md`

**流程**：
1. `mvn clean package` 后端 BUILD SUCCESS。
2. `npm run build` 前端 built without error。
3. `vue-tsc --noEmit` 无错。
4. 手动走完 12 个任务的冒烟场景并记录到 smoke-test 清单。
5. 若发现 bug 立即修复并回到 1。

**冒烟清单大纲**：
- 热点：创建 / 拖拽 / 编辑属性 / 删除 / 观众端跳转
- 互动：登录用户点赞 / 取消 / 收藏 / 取消 / 未登录引导
- 互评：老师配模板 / 学生评同学 / 答案幂等
- 看板：教师打开 / 数据正确 / 学生被重定向
- 重排：拖展区 / 拖展品 / 持久化
- TTS：生成讲解词 + 语音 / 观众端播放

---

## 验收标准

Phase 4 结束时应达到：
- **12 个任务全部完成并 commit**（体量比 Phase 3 略小）。
- **两端构建全绿**（mvn package / npm run build / vue-tsc）。
- **所有新 API 在 `docs/openapi.yaml` 更新**。
- **冒烟清单通过**（smoke-test 文档全部打勾）。
- **技术债不增加**：不得出现 any 类型、未清理的 localStorage 残留、未 @Transactional 的级联删除。

---

## 风险与备选

- **风险 R1**：任务 4 热点编辑器 UX 实施复杂，若遇到 Fabric 与 HTML absolute 层 z-index 冲突，回退方案：热点编辑**不**叠在画布上，改为独立右栏列表 + 坐标数字输入（功能齐，体验略差）。
- **风险 R2**：任务 8 数据看板 SQL 聚合可能对大数据量有性能压力，Phase 4 阶段不引入缓存层（Redis），只做最小 Caffeine 内存缓存。
- **风险 R3**：任务 11 TTS 若无真实服务，仅返回占位音频不违背 Phase 4 承诺，但观众体验缺失真实语音。
- **推迟 R4**：真实 LLM / TTS 接入、实时协同、版本 v3 快照——Phase 5 再战。

---

## 本 Phase 交付清单

- 12 个任务的 git commit（每任务 1-2 个 commit）。
- `docs/plans/2025-05-05-phase4-smoke-test.md` 冒烟清单。
- `docs/openapi.yaml` 更新。
- 前后端零 lint 错误（vue-tsc、mvn compile warnings 除外）。

