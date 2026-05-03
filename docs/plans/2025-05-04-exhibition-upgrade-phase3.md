# 数字展馆升级 Phase 3：提交审核闭环 + AI 讲解系统 + 版本格式 v2

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 跑通"学生编辑→提交审核→教师评分→发布浏览"完整闭环；集成 AI 讲解词生成（含降级）；版本数据格式升级为 v2 多展区结构。

**架构：** 前端 Vue 3 + Fabric 7.3.1 + TailwindCSS；后端 Spring Boot 3 + JdbcTemplate + MySQL。AI 讲解词通过后端代理调用大模型 API，前端展示文本气泡。提交审核通过 `workflow_status` 状态机驱动，审核页嵌入 Viewer 预览。

**技术栈：** Vue 3.5、TypeScript 5.6、Fabric 7.3.1、Pinia、TailwindCSS 3、Axios、Spring Boot 3、Java 21

**设计规格：** `docs/exhibition-hall-upgrade-design.md` v1.2（§5.6 保存与版本、§6 参观模式、§7 AI 数字人、§8 教师端、§11 发布流程、§14 运行时规则附录）

**Phase 1 已完成：** DB 迁移（8 张新表）+ Zone/Exhibit/Hotspot CRUD + editor-bundle 整包接口 + 模板实例化

**Phase 2 已完成：** 前端多展区编辑器 + 沉浸式 Viewer + per-zone 撤销/重做 + draft-bundle 自动保存 + 展区快捷键

---

## 已有基础设施（不重复实现）

### 后端已有

| 模块 | 说明 |
|------|------|
| `SubmissionController` | `GET /submissions/{id}` + `POST /submissions/{id}/reviews` |
| `SubmissionRepository` | 创建提交、查询任务提交列表、upsert review |
| `TaskService.submitTaskWork()` | 学生提交作品（创建 `task_submissions` 记录） |
| `TaskService.getTaskSubmissions()` | 教师查看某任务的所有提交 |
| `exhibitions.workflow_status` | 字段已存在（`draft`→`submitted`→`reviewing`→`approved`→`published`） |
| `exhibitions.bundle_revision` | 乐观锁 revision 字段已存在 |

### 前端已有

| 模块 | 说明 |
|------|------|
| `api/modules/submissions.ts` | `getSubmissionDetail` + `createSubmissionReview` |
| `api/types.ts` | `SubmissionDetail` / `SubmissionReview` / `CreateSubmissionReviewRequest` 类型 |
| `views/submissions/SubmissionDetailView.vue` | 基础提交详情页（已有评分表单） |
| `router` | `/submissions/:submissionId` 路由已注册 |

---

## 文件结构

### 新增文件

| 文件路径 | 职责 |
|---------|------|
| **后端** | |
| `backend/.../controller/AiController.java` | AI 讲解词 / 大纲 / 润色 API |
| `backend/.../service/AiService.java` | AI 服务接口 |
| `backend/.../service/impl/AiServiceImpl.java` | AI 服务实现（对接大模型 + mock 降级） |
| `backend/.../model/request/AiRequests.java` | AI 请求 DTO |
| `backend/.../model/response/AiResponses.java` | AI 响应 DTO |
| **前端** | |
| `frontend/src/api/modules/ai.ts` | AI API 调用 |
| `frontend/src/components/exhibitions/editor/NarrationGenerator.vue` | AI 讲解词生成面板（编辑器内） |
| `frontend/src/components/exhibitions/editor/SubmitForReviewDialog.vue` | 提交审核对话框 |
| `frontend/src/components/exhibitions/viewer/NarrationBubble.vue` | 数字人文本气泡讲解（Viewer 内） |
| `frontend/src/components/exhibitions/viewer/CommentDrawer.vue` | 评论/点赞抽屉 |
| `frontend/src/views/tasks/TaskSubmissionsView.vue` | 教师查看班级作品提交管理页 |

### 修改文件

| 文件路径 | 修改内容 |
|---------|---------|
| **后端** | |
| `SubmissionRepository.java` | 新增 `updateSubmissionStatus()` + `updateWorkflowStatus()` |
| `SubmissionServiceImpl.java` | 新增 `approveSubmission()` / `returnSubmission()` / `publishExhibition()` |
| `SubmissionController.java` | 新增 `POST /submissions/{id}/approve` / `POST /submissions/{id}/return` |
| `ExhibitionController.java` | 新增 `POST /exhibitions/{id}/publish` |
| `TaskServiceImpl.java` | `submitTaskWork()` 中同步更新 `workflow_status = 'submitted'` |
| **前端** | |
| `api/types.ts` | 新增 AI 相关类型、`ApproveSubmissionRequest`、`ReturnSubmissionRequest` |
| `api/modules/submissions.ts` | 新增 `approveSubmission()` / `returnSubmission()` |
| `api/modules/exhibitions.ts` | 新增 `publishExhibition()` 调用（如未有） |
| `views/exhibitions/Editor.vue` | 顶栏增加"提交审核"按钮 + AI 讲解词生成入口 |
| `views/exhibitions/ExhibitionViewer.vue` | 集成 `NarrationBubble` + `CommentDrawer` |
| `views/submissions/SubmissionDetailView.vue` | 增强为审核页（嵌入 Viewer + 审核操作按钮） |
| `components/exhibitions/viewer/DigitalHumanWidget.vue` | 集成 NarrationBubble 替换占位文本 |
| `components/exhibitions/editor/ExhibitPropertiesPanel.vue` | 增加"AI 生成讲解词"按钮 |
| `router/routes/index.ts` | 新增 `/tasks/:taskId/submissions` 路由 |

> **路径缩写约定**：下文中 `src/` = `frontend/src/`，Java 类省略包路径前缀。

---

## 任务 1：后端 — 提交审核状态机

**文件：**
- 修改：`SubmissionRepository.java`
- 修改：`SubmissionServiceImpl.java`
- 修改：`SubmissionService.java`
- 修改：`SubmissionController.java`
- 修改：`TaskServiceImpl.java`

> 规则来源：设计文档 §14.2 workflow_status 与 submission.status 同步规则

- [ ] **步骤 1：SubmissionRepository 新增状态更新方法**

在 `SubmissionRepository` 中新增：

```java
public void updateSubmissionStatus(Long submissionId, String status) {
    jdbcTemplate.update("""
        UPDATE task_submissions
        SET submission_status = ?, updated_at = NOW()
        WHERE id = ?
        """, status, submissionId);
}

public void updateWorkflowStatus(Long exhibitionId, String status) {
    jdbcTemplate.update("""
        UPDATE exhibitions
        SET workflow_status = ?, updated_at = NOW()
        WHERE id = ?
        """, status, exhibitionId);
}

public void setPublishedVersion(Long exhibitionId, Integer versionNo) {
    jdbcTemplate.update("""
        UPDATE exhibitions
        SET published_version_no = ?, workflow_status = 'published', updated_at = NOW()
        WHERE id = ?
        """, versionNo, exhibitionId);
}

public Long findExhibitionIdBySubmissionId(Long submissionId) {
    List<Long> list = jdbcTemplate.query(
        "SELECT exhibition_id FROM task_submissions WHERE id = ? LIMIT 1",
        (rs, rowNum) -> rs.getLong("exhibition_id"), submissionId);
    return list.isEmpty() ? null : list.get(0);
}
```

- [ ] **步骤 2：SubmissionService 新增审核接口**

```java
// SubmissionService.java 新增方法签名
void approveSubmission(Long submissionId, Long userId, String role, Double score, String comment);
void returnSubmission(Long submissionId, Long userId, String role, String reason);
void publishExhibition(Long exhibitionId, Long userId, String role);
```

- [ ] **步骤 3：SubmissionServiceImpl 实现审核逻辑**

```java
@Override
@Transactional
public void approveSubmission(Long submissionId, Long userId, String role, Double score, String comment) {
    Long taskId = submissionRepository.findTaskIdBySubmissionId(submissionId);
    if (taskId == null) throw new BusinessException(HttpStatus.NOT_FOUND, 40471, "提交记录不存在");
    if (!taskQueryRepository.canManageTaskProgress(taskId, userId, role))
        throw new BusinessException(HttpStatus.FORBIDDEN, 40352, "无权审核");

    // 更新 submission
    submissionRepository.updateSubmissionStatus(submissionId, "approved");
    // 写入 review
    submissionRepository.upsertReview(submissionId, userId, score, comment, null, true);
    // 同步 exhibition workflow_status
    Long exhibitionId = submissionRepository.findExhibitionIdBySubmissionId(submissionId);
    if (exhibitionId != null) {
        submissionRepository.updateWorkflowStatus(exhibitionId, "approved");
    }
}

@Override
@Transactional
public void returnSubmission(Long submissionId, Long userId, String role, String reason) {
    Long taskId = submissionRepository.findTaskIdBySubmissionId(submissionId);
    if (taskId == null) throw new BusinessException(HttpStatus.NOT_FOUND, 40471, "提交记录不存在");
    if (!taskQueryRepository.canManageTaskProgress(taskId, userId, role))
        throw new BusinessException(HttpStatus.FORBIDDEN, 40352, "无权审核");

    submissionRepository.updateSubmissionStatus(submissionId, "returned");
    submissionRepository.upsertReview(submissionId, userId, null, reason, null, true);
    Long exhibitionId = submissionRepository.findExhibitionIdBySubmissionId(submissionId);
    if (exhibitionId != null) {
        submissionRepository.updateWorkflowStatus(exhibitionId, "returned");
    }
}

@Override
@Transactional
public void publishExhibition(Long exhibitionId, Long userId, String role) {
    // 验证权限：教师或管理员
    if (!"admin".equals(role) && !"teacher".equals(role))
        throw new BusinessException(HttpStatus.FORBIDDEN, 40353, "无权发布");
    // 获取最新版本号
    Integer latestVersionNo = jdbcTemplate.queryForObject(
        "SELECT latest_version_no FROM exhibitions WHERE id = ?", Integer.class, exhibitionId);
    if (latestVersionNo == null || latestVersionNo <= 0)
        throw new BusinessException(HttpStatus.BAD_REQUEST, 40036, "展厅没有可发布的版本");
    submissionRepository.setPublishedVersion(exhibitionId, latestVersionNo);
}
```

- [ ] **步骤 4：SubmissionController 新增审核端点**

```java
@PostMapping("/{submissionId}/approve")
public ApiResponse<Void> approveSubmission(
        @PathVariable Long submissionId,
        @AuthenticationPrincipal SecurityUserDetails currentUser,
        @RequestBody Map<String, Object> body) {
    Double score = body.get("score") != null ? ((Number) body.get("score")).doubleValue() : null;
    String comment = (String) body.get("comment");
    submissionService.approveSubmission(submissionId, currentUser.getId(), currentUser.getRole(), score, comment);
    return success(null);
}

@PostMapping("/{submissionId}/return")
public ApiResponse<Void> returnSubmission(
        @PathVariable Long submissionId,
        @AuthenticationPrincipal SecurityUserDetails currentUser,
        @RequestBody Map<String, Object> body) {
    String reason = (String) body.get("reason");
    submissionService.returnSubmission(submissionId, currentUser.getId(), currentUser.getRole(), reason);
    return success(null);
}
```

- [ ] **步骤 5：ExhibitionController 新增发布端点**

```java
@PostMapping("/{exhibitionId}/publish")
public ApiResponse<Void> publish(
        @PathVariable Long exhibitionId,
        @AuthenticationPrincipal SecurityUserDetails currentUser) {
    submissionService.publishExhibition(exhibitionId, currentUser.getId(), currentUser.getRole());
    return success(null);
}
```

- [ ] **步骤 6：TaskServiceImpl.submitTaskWork() 同步 workflow_status**

在 `submitTaskWork()` 方法中、`createSubmission` 调用后追加：

```java
// 同步展厅 workflow_status = 'submitted'
submissionRepository.updateWorkflowStatus(request.exhibitionId(), "submitted");
```

- [ ] **步骤 7：编译验证**

运行：`mvn compile -pl backend -q`
预期：BUILD SUCCESS

- [ ] **步骤 8：Commit**

```bash
git add backend/
git commit -m "feat(backend): submission workflow state machine — approve/return/publish endpoints + workflow_status sync"
```

---

## 任务 2：前端 — 提交审核 API + 类型

**文件：**
- 修改：`src/api/types.ts`
- 修改：`src/api/modules/submissions.ts`
- 修改：`src/api/modules/exhibitions.ts`

- [ ] **步骤 1：types.ts 追加审核请求类型**

```typescript
export interface ApproveSubmissionRequest {
  score?: number | null
  comment?: string | null
}

export interface ReturnSubmissionRequest {
  reason?: string | null
}

// AI 相关类型（任务 7 使用，此处先定义）
export interface GenerateNarrationRequest {
  exhibitTitle: string
  exhibitDescription?: string | null
  knowledgePoints?: string[] | null
  targetGrade?: string | null
  style?: 'narrative' | 'academic' | 'storytelling' | 'conversational'
  maxLength?: number
}

export interface GenerateNarrationResponse {
  narration: string
  suggestions?: string[]
}
```

- [ ] **步骤 2：submissions.ts 新增 approve / return**

```typescript
export function approveSubmission(submissionId: number, payload: ApproveSubmissionRequest) {
  return http.post<void>(`/submissions/${submissionId}/approve`, payload)
}

export function returnSubmission(submissionId: number, payload: ReturnSubmissionRequest) {
  return http.post<void>(`/submissions/${submissionId}/return`, payload)
}
```

- [ ] **步骤 3：exhibitions.ts 确认 publishExhibition 存在**

如果 `publishExhibition` 已存在则跳过；否则新增：

```typescript
export function publishExhibition(exhibitionId: number) {
  return http.post<void>(`/exhibitions/${exhibitionId}/publish`)
}
```

- [ ] **步骤 4：vue-tsc 验证**

运行：`npx vue-tsc --noEmit`
预期：零错误

- [ ] **步骤 5：Commit**

```bash
git add frontend/src/api/
git commit -m "feat(frontend): add approve/return/publish API + AI narration types"
```

---

## 任务 3：前端 — 编辑器"提交审核"对话框

**文件：**
- 创建：`src/components/exhibitions/editor/SubmitForReviewDialog.vue`
- 修改：`src/views/exhibitions/Editor.vue`

- [ ] **步骤 1：创建 SubmitForReviewDialog.vue**

```vue
<template>
  <Teleport to="body">
    <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div class="w-[28rem] rounded-2xl bg-white p-6 shadow-2xl">
        <h3 class="text-lg font-semibold text-gray-900">提交审核</h3>
        <p class="mt-2 text-sm text-gray-500">
          提交后，教师将对当前版本进行审核评分。提交期间你仍可继续编辑草稿。
        </p>
        <div class="mt-4">
          <label class="block text-sm font-medium text-gray-700">提交说明（选填）</label>
          <textarea
            v-model="remark"
            rows="3"
            class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none"
            placeholder="描述本次提交的主要变更..."
          />
        </div>
        <div class="mt-6 flex justify-end gap-3">
          <button type="button" class="rounded-lg px-4 py-2 text-sm text-gray-600 hover:bg-gray-100" @click="$emit('close')">取消</button>
          <button
            type="button"
            class="rounded-lg bg-brand-600 px-4 py-2 text-sm text-white hover:bg-brand-700 disabled:opacity-50"
            :disabled="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '提交中...' : '确认提交' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{ visible: boolean }>()
const emit = defineEmits<{
  close: []
  submit: [remark: string]
}>()

const remark = ref('')
const submitting = ref(false)

async function handleSubmit() {
  submitting.value = true
  emit('submit', remark.value)
}
</script>
```

- [ ] **步骤 2：Editor.vue 顶栏添加"提交审核"按钮**

在顶栏保存按钮旁新增：

```html
<button
  v-if="bundle?.exhibition.taskId"
  type="button"
  class="rounded-lg border border-brand-200 px-3 py-1.5 text-sm text-brand-700 hover:bg-brand-50"
  @click="showSubmitDialog = true"
>
  提交审核
</button>
```

在 script 中新增：

```typescript
import SubmitForReviewDialog from '@/components/exhibitions/editor/SubmitForReviewDialog.vue'
import { submitTaskWork } from '@/api/modules/tasks'

const showSubmitDialog = ref(false)

async function handleSubmitForReview(remark: string) {
  if (!bundle.value?.exhibition.taskId) return
  try {
    await handleSave()  // 先保存
    await submitTaskWork(bundle.value.exhibition.taskId, {
      exhibitionId: exhibitionId,
      submitRemark: remark || undefined,
    })
    showSubmitDialog.value = false
    appStore.showToast('提交成功，等待教师审核', 'success')
  } catch (e) {
    appStore.showToast(getErrorMessage(e, '提交失败'), 'error')
  }
}
```

在 template 末尾增加 dialog 组件：

```html
<SubmitForReviewDialog
  :visible="showSubmitDialog"
  @close="showSubmitDialog = false"
  @submit="handleSubmitForReview"
/>
```

- [ ] **步骤 3：vue-tsc 验证 + Commit**

```bash
npx vue-tsc --noEmit
git add frontend/src/
git commit -m "feat(frontend): add submit-for-review dialog in editor"
```

---

## 任务 4：前端 — 教师班级作品管理页

**文件：**
- 创建：`src/views/tasks/TaskSubmissionsView.vue`
- 修改：`src/router/routes/index.ts`
- 修改：`src/api/modules/tasks.ts`

- [ ] **步骤 1：确认 tasks.ts 中 getTaskSubmissions API**

检查 `src/api/modules/tasks.ts` 是否已有 `getTaskSubmissions`。如有，跳过；否则新增：

```typescript
export function getTaskSubmissions(taskId: number) {
  return http.get<SubmissionDetail[]>(`/tasks/${taskId}/submissions`)
}
```

- [ ] **步骤 2：创建 TaskSubmissionsView.vue**

功能要点：
- 调用 `getTaskSubmissions(taskId)` 获取所有提交
- 表格展示：提交人、展厅名、提交时间、状态、操作（查看详情 / 审核）
- 按状态筛选（全部 / submitted / reviewed / approved / returned）
- 点击"审核"跳转到 `/submissions/:submissionId`

```vue
<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="班级作品管理"
      :title="'任务提交列表'"
      description="查看所有学生提交的作品，进行审核评分。"
      :back-to="`/tasks/${taskId}`"
      back-label="返回任务详情"
    />

    <div class="flex items-center gap-3">
      <button
        v-for="st in statusFilters"
        :key="st.value"
        type="button"
        class="rounded-full px-4 py-1.5 text-sm transition"
        :class="activeFilter === st.value ? 'bg-brand-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
        @click="activeFilter = st.value"
      >
        {{ st.label }}
      </button>
    </div>

    <p v-if="loading" class="text-sm text-gray-500">加载中...</p>
    <p v-else-if="errorMessage" class="text-sm text-rose-500">{{ errorMessage }}</p>

    <div v-else class="overflow-hidden rounded-xl border border-gray-200 bg-white">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 text-left text-xs uppercase text-gray-500">
          <tr>
            <th class="px-4 py-3">提交人</th>
            <th class="px-4 py-3">版本</th>
            <th class="px-4 py-3">状态</th>
            <th class="px-4 py-3">提交时间</th>
            <th class="px-4 py-3">评分</th>
            <th class="px-4 py-3 text-right">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="s in filteredSubmissions" :key="s.id" class="hover:bg-gray-50">
            <td class="px-4 py-3 font-medium text-gray-900">{{ s.submitter.nickname || s.submitter.name }}</td>
            <td class="px-4 py-3 text-gray-600">v{{ s.versionNo }}</td>
            <td class="px-4 py-3">
              <span class="rounded-full px-2 py-0.5 text-xs" :class="statusClass(s.submissionStatus)">
                {{ statusLabel(s.submissionStatus) }}
              </span>
            </td>
            <td class="px-4 py-3 text-gray-500">{{ formatDate(s.submittedAt) }}</td>
            <td class="px-4 py-3 text-gray-600">{{ getScore(s) ?? '—' }}</td>
            <td class="px-4 py-3 text-right">
              <RouterLink
                :to="`/submissions/${s.id}`"
                class="text-brand-600 hover:underline"
              >
                {{ s.submissionStatus === 'submitted' ? '去审核' : '查看详情' }}
              </RouterLink>
            </td>
          </tr>
          <tr v-if="filteredSubmissions.length === 0">
            <td colspan="6" class="py-8 text-center text-gray-400">暂无提交记录</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getTaskSubmissions } from '@/api/modules/tasks'
import { getErrorMessage } from '@/utils/request'
import type { SubmissionDetail } from '@/api/types'
import PageHero from '@/components/common/PageHero.vue'

const route = useRoute()
const taskId = Number(route.params.taskId)

const loading = ref(true)
const errorMessage = ref('')
const submissions = ref<SubmissionDetail[]>([])
const activeFilter = ref('all')

const statusFilters = [
  { value: 'all', label: '全部' },
  { value: 'submitted', label: '待审核' },
  { value: 'reviewed', label: '已评分' },
  { value: 'approved', label: '已通过' },
  { value: 'returned', label: '已退回' },
]

const filteredSubmissions = computed(() => {
  if (activeFilter.value === 'all') return submissions.value
  return submissions.value.filter(s => s.submissionStatus === activeFilter.value)
})

function statusClass(status: string) {
  const map: Record<string, string> = {
    submitted: 'bg-amber-100 text-amber-700',
    reviewed: 'bg-blue-100 text-blue-700',
    approved: 'bg-green-100 text-green-700',
    returned: 'bg-rose-100 text-rose-700',
  }
  return map[status] ?? 'bg-gray-100 text-gray-600'
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    submitted: '待审核',
    reviewed: '已评分',
    approved: '已通过',
    returned: '已退回',
  }
  return map[status] ?? status
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function getScore(s: SubmissionDetail) {
  if (!s.reviews || s.reviews.length === 0) return null
  return s.reviews[0].score
}

onMounted(async () => {
  try {
    submissions.value = await getTaskSubmissions(taskId)
  } catch (e) {
    errorMessage.value = getErrorMessage(e, '加载失败')
  } finally {
    loading.value = false
  }
})
</script>
```

- [ ] **步骤 3：注册路由**

在 `router/routes/index.ts` 中添加：

```typescript
{
  path: 'tasks/:taskId/submissions',
  name: 'task-submissions',
  component: () => import('@/views/tasks/TaskSubmissionsView.vue'),
  meta: { title: '班级作品管理' },
},
```

- [ ] **步骤 4：vue-tsc 验证 + Commit**

```bash
npx vue-tsc --noEmit
git add frontend/src/
git commit -m "feat(frontend): add TaskSubmissionsView for teacher submission management"
```

---

## 任务 5：前端 — 增强 SubmissionDetailView 为审核页

**文件：**
- 修改：`src/views/submissions/SubmissionDetailView.vue`

- [ ] **步骤 1：在现有 SubmissionDetailView 中新增审核操作区**

在现有评分表单下方增加：
- "通过" 按钮：调用 `approveSubmission(submissionId, { score, comment })`
- "退回修改" 按钮：调用 `returnSubmission(submissionId, { reason })`
- "发布" 按钮（仅 `approved` 状态显示）：调用 `publishExhibition(exhibitionId)`

按钮仅对 teacher / admin 角色可见。

- [ ] **步骤 2：嵌入展厅预览 iframe 或链接**

在页面左侧（或上方）增加展厅预览入口按钮，点击在新标签页打开 `/exhibitions/:exhibitionId/view`。

- [ ] **步骤 3：vue-tsc 验证 + Commit**

```bash
npx vue-tsc --noEmit
git add frontend/src/views/submissions/
git commit -m "feat(frontend): enhance SubmissionDetailView with approve/return/publish actions"
```

---

## 任务 6：前端 — Viewer 评论抽屉 + 点赞

**文件：**
- 创建：`src/components/exhibitions/viewer/CommentDrawer.vue`
- 修改：`src/views/exhibitions/ExhibitionViewer.vue`

- [ ] **步骤 1：创建 CommentDrawer.vue**

侧滑抽屉组件，显示教师点评 + 学生评论列表，底部有发送评论的输入框。

Props: `visible`, `teacherReviews`, `comments`, `exhibitionId`
Emits: `close`, `comment-posted`

- [ ] **步骤 2：ExhibitionViewer.vue 集成 CommentDrawer**

在顶栏右侧增加"💬评论"按钮，点击打开 CommentDrawer。
保留底部评论区作为备用（CommentDrawer 关闭时可见）。

- [ ] **步骤 3：vue-tsc 验证 + Commit**

```bash
npx vue-tsc --noEmit
git add frontend/src/
git commit -m "feat(frontend): add CommentDrawer to viewer + comment toggle button"
```

---

## 任务 7：后端 — AI 讲解词生成 API

**文件：**
- 创建：`AiRequests.java`
- 创建：`AiResponses.java`
- 创建：`AiService.java`
- 创建：`AiServiceImpl.java`
- 创建：`AiController.java`

> 规则来源：设计文档 §7.2 创作辅助 AI + §7.5 降级方案

- [ ] **步骤 1：创建 AiRequests.java**

```java
public final class AiRequests {
    private AiRequests() {}

    public record GenerateNarrationRequest(
        @NotBlank String exhibitTitle,
        String exhibitDescription,
        List<String> knowledgePoints,
        String targetGrade,
        String style,     // narrative / academic / storytelling / conversational
        Integer maxLength  // 默认 500
    ) {}
}
```

- [ ] **步骤 2：创建 AiResponses.java**

```java
public final class AiResponses {
    private AiResponses() {}

    public record NarrationResponse(
        String narration,
        List<String> suggestions
    ) {}
}
```

- [ ] **步骤 3：创建 AiService 接口**

```java
public interface AiService {
    AiResponses.NarrationResponse generateNarration(AiRequests.GenerateNarrationRequest request);
}
```

- [ ] **步骤 4：创建 AiServiceImpl（含 mock 降级）**

```java
@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AiResponses.NarrationResponse generateNarration(AiRequests.GenerateNarrationRequest request) {
        if (!aiEnabled || apiKey.isBlank()) {
            return mockNarration(request);
        }
        try {
            return callAiApi(request);
        } catch (Exception e) {
            log.warn("AI API 调用失败，降级为 mock 模式: {}", e.getMessage());
            return mockNarration(request);
        }
    }

    private AiResponses.NarrationResponse mockNarration(AiRequests.GenerateNarrationRequest request) {
        String template = "同学们，现在我们来看"%s"。%s希望大家认真观察，思考其中的文化内涵。";
        String narration = String.format(template,
            request.exhibitTitle(),
            request.exhibitDescription() != null ? request.exhibitDescription() + "。" : ""
        );
        return new AiResponses.NarrationResponse(narration, List.of("建议补充历史背景信息", "建议增加互动提问"));
    }

    private AiResponses.NarrationResponse callAiApi(AiRequests.GenerateNarrationRequest request) {
        // 构建 ChatCompletion 请求
        String systemPrompt = "你是一位博物馆讲解员，为中小学生讲解展品。语言要生动有趣、通俗易懂。";
        String userPrompt = String.format(
            "请为展品"%s"生成一段讲解词。描述：%s。知识点：%s。风格：%s。字数限制：%d字以内。",
            request.exhibitTitle(),
            request.exhibitDescription() != null ? request.exhibitDescription() : "无",
            request.knowledgePoints() != null ? String.join("、", request.knowledgePoints()) : "无",
            request.style() != null ? request.style() : "narrative",
            request.maxLength() != null ? request.maxLength() : 500
        );
        // 调用 OpenAI 兼容 API（需要根据实际使用的大模型调整）
        // ... 省略 HTTP 调用细节，返回 NarrationResponse
        return mockNarration(request); // 占位，实际接入时替换
    }
}
```

- [ ] **步骤 5：创建 AiController**

```java
@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/ai")
public class AiController extends BaseController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/narration/generate")
    public ApiResponse<AiResponses.NarrationResponse> generateNarration(
            @Valid @RequestBody AiRequests.GenerateNarrationRequest request) {
        return success(aiService.generateNarration(request));
    }
}
```

- [ ] **步骤 6：application.yml 添加 AI 配置**

```yaml
ai:
  enabled: false
  api-key: ${AI_API_KEY:}
  base-url: ${AI_BASE_URL:https://api.openai.com/v1}
```

- [ ] **步骤 7：编译验证 + Commit**

```bash
mvn compile -pl backend -q
git add backend/
git commit -m "feat(backend): AI narration generation API with mock fallback"
```

---

## 任务 8：前端 — AI API 模块 + 讲解词生成面板

**文件：**
- 创建：`src/api/modules/ai.ts`
- 创建：`src/components/exhibitions/editor/NarrationGenerator.vue`
- 修改：`src/components/exhibitions/editor/ExhibitPropertiesPanel.vue`

- [ ] **步骤 1：创建 ai.ts**

```typescript
import { http } from '@/utils/request'
import type { GenerateNarrationRequest, GenerateNarrationResponse } from '@/api/types'

export function generateNarration(payload: GenerateNarrationRequest) {
  return http.post<GenerateNarrationResponse>('/ai/narration/generate', payload)
}
```

- [ ] **步骤 2：创建 NarrationGenerator.vue**

生成讲解词的面板组件：
- 输入：展品标题、描述、知识点（从 props 获取）
- 风格选择下拉（narrative / academic / storytelling / conversational）
- "生成讲解词"按钮 → 调用 `generateNarration()` → 显示结果
- "使用此讲解词"按钮 → emit 给父组件

- [ ] **步骤 3：ExhibitPropertiesPanel 集成 NarrationGenerator**

在展品属性面板的"讲解词"字段下方增加 NarrationGenerator 组件的折叠展示。

- [ ] **步骤 4：vue-tsc 验证 + Commit**

```bash
npx vue-tsc --noEmit
git add frontend/src/
git commit -m "feat(frontend): AI narration generator panel in exhibit properties"
```

---

## 任务 9：前端 — Viewer 数字人文本气泡讲解

**文件：**
- 创建：`src/components/exhibitions/viewer/NarrationBubble.vue`
- 修改：`src/components/exhibitions/viewer/DigitalHumanWidget.vue`
- 修改：`src/views/exhibitions/ExhibitionViewer.vue`

> 规则来源：设计文档 §7.3 参观讲解 AI — P0 仅文本气泡

- [ ] **步骤 1：创建 NarrationBubble.vue**

文本气泡组件：
- Props: `text` (string), `visible` (boolean), `typing` (boolean)
- 逐字显示效果（typewriter animation）
- "关闭"按钮

- [ ] **步骤 2：改造 DigitalHumanWidget.vue**

替换占位文本为 NarrationBubble：
- 点击数字人按钮 → 显示当前展区的 narration_text
- 展品详情弹窗打开时 → 自动切换显示展品讲解词
- 无内容时显示"暂无讲解内容"

- [ ] **步骤 3：ExhibitionViewer.vue 传递讲解数据**

将 `currentZone.narrationText` 和 `selectedExhibit.narration` 传递给 DigitalHumanWidget。

- [ ] **步骤 4：vue-tsc 验证 + Commit**

```bash
npx vue-tsc --noEmit
git add frontend/src/
git commit -m "feat(frontend): digital human text bubble narration in viewer"
```

---

## 任务 10：版本数据格式 v2

**文件：**
- 修改：`src/api/types.ts` （前端）
- 修改：`EditorBundleController.java` / `EditorBundleServiceImpl.java` （后端，如需）
- 修改：`src/views/exhibitions/Editor.vue`

> 规则来源：设计文档 §5.6 保存与版本语义分层

- [ ] **步骤 1：定义 v2 版本数据结构**

在 `types.ts` 追加：

```typescript
export interface VersionDataV2 {
  formatVersion: 2
  zones: Record<string, Record<string, unknown>>  // zoneCode → fabricJson
  exhibits: Array<{ id: number; title: string; description?: string; narration?: string }>
  metadata: {
    savedAt: string
    savedBy: number
    templateId?: number | null
  }
}
```

- [ ] **步骤 2：Editor.vue 手动保存时生成 v2 格式**

在 `handleSave()` 的 `buildCanvasDataMap()` 后，构建 v2 格式的 `versionData`：

```typescript
const versionData: VersionDataV2 = {
  formatVersion: 2,
  zones: buildCanvasDataMap(),
  exhibits: allExhibits.value.map(e => ({
    id: e.id, title: e.title, description: e.description, narration: e.narration,
  })),
  metadata: {
    savedAt: new Date().toISOString(),
    savedBy: currentUserId,
    templateId: bundle.value?.exhibition.templateId ?? null,
  },
}
```

- [ ] **步骤 3：Viewer 兼容 v1/v2 格式**

ExhibitionViewer.vue 中检测 `zone.canvasData` 的格式：
- 如果是 v2 对象（`formatVersion === 2`），从 `zones` 字段按 zoneCode 取数据
- 如果是 v1 或直接 fabricJson（无 `formatVersion`），按旧逻辑处理

- [ ] **步骤 4：vue-tsc + vite build 验证 + Commit**

```bash
npx vue-tsc --noEmit
npx vite build
git add frontend/src/ backend/
git commit -m "feat: version data format v2 with multi-zone structure + v1 compatibility"
```

---

## 任务 11：展区/展品 CRUD 完整实现（去 stub）

**文件：**
- 修改：`src/views/exhibitions/Editor.vue`
- 修改：`src/composables/useZoneManager.ts`
- 修改：`src/composables/useExhibitManager.ts`

- [ ] **步骤 1：useZoneManager 实现 addZone / removeZone**

调用已有的 `createZone` / `deleteZone` API：

```typescript
async function addZone(title: string, zoneCode: string) {
  const newZone = await createZone(exhibitionId.value, { title, zoneCode, sortOrder: zones.value.length })
  zones.value.push(newZone)
}

async function removeZone(zoneId: number) {
  await deleteZone(exhibitionId.value, zoneId)
  zones.value = zones.value.filter(z => z.id !== zoneId)
}
```

- [ ] **步骤 2：useExhibitManager 实现 addExhibit / removeExhibit**

调用已有的 `createExhibit` / `deleteExhibit` API。

- [ ] **步骤 3：Editor.vue 替换 stub 为真实调用**

将 `handleAddZone()` 和 `handleAddExhibit()` 中的 toast 替换为弹窗/对话框 + 真实 API 调用。

- [ ] **步骤 4：vue-tsc 验证 + Commit**

```bash
npx vue-tsc --noEmit
git add frontend/src/
git commit -m "feat(frontend): implement zone/exhibit CRUD — replace stubs with real API calls"
```

---

## 任务 12：最终集成验证

**文件：** 无新增

- [ ] **步骤 1：后端编译**

```bash
cd backend && mvn compile -q
```

- [ ] **步骤 2：前端类型检查**

```bash
cd frontend && npx vue-tsc --noEmit
```

- [ ] **步骤 3：前端构建**

```bash
npx vite build
```

- [ ] **步骤 4：验证路由**

确认以下路由可访问：
- `/exhibitions/:id/editor` — 编辑器（含提交审核按钮 + AI 讲解词入口）
- `/exhibitions/:id/view` — Viewer（含数字人气泡讲解 + 评论抽屉）
- `/tasks/:taskId/submissions` — 教师班级作品管理
- `/submissions/:submissionId` — 审核评价页

- [ ] **步骤 5：Commit + Push**

```bash
git add -A
git commit -m "chore: Phase 3 final integration verification"
git push origin master
```

---

## 不在 Phase 3 范围

以下功能留待后续阶段：

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 热点可视化编辑器 | P1 | 需要拖拽 + 碰撞检测 |
| TTS 语音合成 | P1 | 需接入语音 API |
| 模板化互评系统 | P1 | 学生间互评 |
| 团队协作 UI（编辑锁/心跳） | P1 | 需 WebSocket 或轮询 |
| 教师数据看板 | P1 | 统计分析 |
| AI 问答模式 | P2 | 限定上下文问答 |
| 版本对比 | P2 | per-zone diff |
| 实时协同编辑 | P2 | WebSocket |
