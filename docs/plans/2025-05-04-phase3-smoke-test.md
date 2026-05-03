# Phase 3 端到端冒烟测试清单

> 本清单覆盖 Phase 3（提交审核 / AI 讲解 / 观众社交 / CRUD 完整化）所有交付项。
> 适用版本：master commit `f60dccc` 及之后。

## 0. 准备

```pwsh
# 后端
cd backend
mvn -B -DskipTests spring-boot:run

# 前端
cd frontend
npm install   # 首次
npm run dev
```

- 后端端口：`8080`（默认）。
- 前端端口：`5173`（Vite 默认）。
- 数据库：MySQL，确保 `exhibitions.workflow_status` 列存在；
  若提示外键约束错误请先清理孤儿展品（见**已知风险 R1**）。

测试账号（初始数据，密码由前端自动 SHA256 后传给后端）：
- 管理员：`admin / 123456`
- 老师：`teacher001 / 123456`（昵称「李老师」）
- 学生 1：`student001 / 123456`（昵称「小明」）
- 学生 2：`student002 / 123456`（昵称「小红」）

**MySQL 连接**：`127.0.0.1:3306` / `zhixingchuangjing` / `root` / 密码依本地实际为准（dev 配置默认 `123456`，可用环境变量 `SPRING_DATASOURCE_PASSWORD` 覆盖）。首次使用需导入 `backend/src/main/resources/db/seed/mysql-init.sql`。

---

## 1. 学生端：编辑器 + 提交审核

### 1.1 创建展区
**步骤**
1. 学生登录，进入「我的展厅」→ 选一个未提交的展厅 → 点「进入编辑器」。
2. 左侧栏「展区」标签 → 点「+ 新增」→ 填写：
   - 标题：`测试展区A`
   - 编码：`zone_test_a`（自动转下划线小写）
   - 类型：`gallery`
3. 点「确认创建」。

**预期**
- Toast：`展区「测试展区A」已创建`。
- 展区列表新增 1 项；自动切换到该展区；`#N` 序号自增。
- 后端 `exhibition_zones` 多 1 行。

### 1.2 添加展品
**步骤**
1. 选中刚创建的展区。
2. 左侧栏「展区」下方「展品」→ 点「+ 添加」→ 填写：
   - 名称：`青花瓷香炉`
   - 类型：`image`
   - 位置模式：`freeform`
   - 媒体 URL：可选填一张外链图。
3. 点「确认创建」。

**预期**
- Toast：`展品「青花瓷香炉」已创建`。
- 右栏属性面板自动显示新建展品。
- 后端 `exhibition_exhibits` 多 1 行。

### 1.3 AI 讲解词生成
**步骤**
1. 在右栏「展品属性」面板 → 找到「讲解词」→ 点紫色「AI 生成」按钮。
2. 在弹窗中：
   - 风格选 `narrative`。
   - 长度填 `120`。
   - 关键词随便填几个。
3. 点「生成讲解词」→ 编辑生成的文本 → 点「使用这段文案」。

**预期**
- 弹窗内 1~2 秒内显示生成结果（template-based mock）。
- 关闭弹窗后属性面板「讲解词 (1)」计数加 1。
- Toast：`AI 讲解词已保存`。
- 后端 `exhibit_narrations` 多 1 行（`narration_type='ai'`）。

### 1.4 删除展品/展区
**步骤**
1. 展品列表悬停 → 点 ✕ → 确认。
2. 展区列表悬停 → 点 ✕ → 确认（如果展区下还有展品，会提示级联删除数量）。

**预期**
- Toast 成功。
- 删除展区后下属展品/讲解词/互动题在数据库中也消失（级联在 `ZoneCommandRepository.deleteZone` 内）。
- 至少保留一个展区，否则提示「至少保留一个展区」。

### 1.5 提交审核
**步骤**
1. 顶栏点「保存全部」→ 等 `latestVersionNo` 更新。
2. 顶栏「提交审核」按钮变绿可点击 → 点击 → 写说明 → 确认。

**预期**
- Toast：`已提交审核，等待教师评分`。
- 后端 `task_submissions` 多 1 行；`exhibitions.workflow_status` 同步为 `submitted`。

---

## 2. 教师端：班级作品管理 + 审核

### 2.1 任务作品列表
**步骤**
1. 教师登录 → 「我的任务」→ 进入对应任务详情 → 点「查看班级作品」。
2. 进入 `/tasks/:id/submissions`。

**预期**
- 表格列出该任务所有作品；筛选下拉切换 `submitted/reviewed/approved/returned/published` 时正常过滤。
- 点「查看」跳转到作品详情。

### 2.2 通过 / 退回
**步骤**
1. 在作品详情页，先填评分（0~100）和评语 → 点「保存评分」。
2. 点「通过」→ 确认。
3. 另选一份作品测试「退回」→ 必填退回理由 → 提交。

**预期**
- 通过后状态从 `submitted` → `approved`，`workflow_status` → `approved`，按钮按 state machine 失效。
- 退回后状态变 `returned`，`workflow_status` 回 `draft`，学生端可再编辑再提交。

### 2.3 发布到社区
**步骤**
1. 已通过的作品详情页（或学生在「编辑器顶栏 → 发布」）。
2. 点「发布」按钮。

**预期**
- 后端 `exhibition_versions` 标记为 published；`workflow_status` → `published`。
- 在「探索社区」页面可见该展厅。

---

## 3. 观众端：观看 + 互动

### 3.1 进入观众页
**步骤**
1. 任意已发布展厅 → 详情页点「观众浏览」→ `/exhibitions/:id/view`。

**预期**
- 顶栏显示展厅标题、所属班级、作者昵称（已修复 ownerName→author.nickname）。
- 多展区时左侧 MiniMap 可点切换。

### 3.2 数字人讲解气泡
**步骤**
1. 右下角圆形 🎙 按钮（带红色徽章数字 = 当前展区可用讲解条数）。
2. 点击展开 → 看气泡内容（带打字机动画）→ 试「上一条」「下一条」。
3. 切换展区 → 自动重置到第 1 条。

**预期**
- 文本逐字出现；卡片角标显示「展区讲解 / 展品讲解 · 序号」。
- 没有任何讲解词时按钮隐藏（visible=hasNarrationContent）。

### 3.3 评论 / 点赞 / 收藏
**步骤**
1. 顶栏右上角 ❤️ 点赞 → 数字 +1，再点变灰。
2. ⭐ 收藏 → 切换。
3. 💬 评论按钮打开抽屉 → 输入文字 → 提交。

**预期**
- 点赞 / 收藏：乐观更新，刷新页面后状态由 localStorage 保持（key: `liked_exh_<id>`、`fav_exh_<id>`）。
- 评论：抽屉左半「学生评论」实时增 1 条；右半「老师点评」展示已审核的 review。

---

## 4. 已知风险与限制

- **R1**：升级前若数据库已存在 `zone_id` 指向已删除展区的孤儿展品（旧版本无级联），首次执行 `deleteZone` 后视图正常但旧孤儿仍在。建议手工 SQL 清一次：
  ```sql
  DELETE e FROM exhibition_exhibits e
   LEFT JOIN exhibition_zones z ON z.id = e.zone_id
   WHERE z.id IS NULL;
  ```
- **R2**：AI 讲解仍是 template mock，未接入真实 LLM；前端已留 API 通道（`/api/v1/ai/narration`），后续替换 `AiServiceImpl` 即可。
- **R3**：版本快照数据格式 v2 升级（任务 10）暂未实施。当前 publish 仍走 v1 兼容流程，对功能无影响。
- **R4**：观众端点赞/收藏目前**未持久化到后端**，仅 localStorage（避免增加未规划的接口；后续接 `interactions` 表即可）。
- **R5**：编辑器中**重排展区**（拖拽改 sortOrder）尚未做 UI；后端已有 `reorderZones` API。
- **R6**：富文本/视频/音频展品的 viewer 端播放器仍依赖 P1 的 HTML5 占位实现。

---

## 5. 自动化验证（CI 推荐）

```pwsh
# 后端
cd backend
mvn -B -DskipTests package      # 当前 BUILD SUCCESS

# 前端
cd ../frontend
npm run build                   # 当前 built in ~9s, no error
```

## 6. 提交记录（本阶段）

| commit  | 说明                                                                   |
| ------- | ---------------------------------------------------------------------- |
| c42240c | docs: Phase 3 plan                                                     |
| ...     | （任务 1-9 各 commit）                                                 |
| 1c158bb | feat(editor): zone/exhibit CRUD dialogs + backend cascade delete       |
| f60dccc | fix(frontend): align type usage in CommentDrawer / Community / Viewer  |

