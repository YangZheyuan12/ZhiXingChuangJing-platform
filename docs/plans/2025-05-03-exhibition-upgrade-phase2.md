# 数字展馆升级 Phase 2：前端编辑器改造 + 参观模式 + 版本格式升级

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将前端从单画布 Fabric 编辑器改造为多展区三层渲染架构，对接 Phase 1 后端 API（展区/展品/热点/editor-bundle），实现创建展厅时模板选择、多展区编辑、沉浸式参观浏览、版本格式 v2 升级。

**架构：** Vue 3 + Fabric 7.3.1 + TailwindCSS。编辑器三层渲染（背景层 HTML/CSS + Fabric 画布层 + 热点覆盖层 HTML absolute）。展区切换通过销毁/重建 Fabric 实现。数据通过 editor-bundle 整包接口加载/保存，自动保存通过 draft-bundle 按展区粒度写入。

**技术栈：** Vue 3.5、TypeScript 5.6、Fabric 7.3.1、Pinia、Vue Router 4、TailwindCSS 3、Axios

**设计规格：** `docs/exhibition-hall-upgrade-design.md` v1.2（§5 编辑器改造、§6 参观模式、§7 数字人、§14 运行时规则）

**Phase 1 后端已完成：** 展区/展品/热点 CRUD API + editor-bundle 整包接口 + 模板实例化 + DB 迁移

---

## 文件结构

### 新增文件

| 文件路径 | 职责 |
|---------|------|
| `frontend/src/api/modules/zones.ts` | 展区 API 调用 |
| `frontend/src/api/modules/exhibits.ts` | 展品 API 调用 |
| `frontend/src/api/modules/editor-bundle.ts` | editor-bundle / draft-bundle API |
| `frontend/src/composables/useZoneManager.ts` | 展区 CRUD / 切换 / 排序 / 编辑锁 |
| `frontend/src/composables/useExhibitManager.ts` | 展品 CRUD / 属性编辑 |
| `frontend/src/composables/useSceneRenderer.ts` | 场景分层渲染 / Fabric 生命周期 / 切换动画 |
| `frontend/src/components/exhibitions/editor/ZoneNavigator.vue` | 左侧栏展区列表/增删/排序 |
| `frontend/src/components/exhibitions/editor/ExhibitList.vue` | 左侧栏当前展区展品列表 |
| `frontend/src/components/exhibitions/editor/EditorCanvas.vue` | 三层渲染画布容器 |
| `frontend/src/components/exhibitions/editor/SceneBackground.vue` | Layer1: 场景背景 |
| `frontend/src/components/exhibitions/editor/HotspotOverlay.vue` | Layer3: 热点覆盖层 |
| `frontend/src/components/exhibitions/editor/HotspotMarker.vue` | 单个热点标记 |
| `frontend/src/components/exhibitions/editor/ExhibitSlotOverlay.vue` | 展品槽位高亮 |
| `frontend/src/components/exhibitions/editor/ZonePropertiesPanel.vue` | 右侧栏展区属性 |
| `frontend/src/components/exhibitions/editor/ExhibitPropertiesPanel.vue` | 右侧栏展品属性（含讲解词/互动题） |
| `frontend/src/components/exhibitions/editor/ZoneStrip.vue` | 底部展区缩略图条 |
| `frontend/src/components/exhibitions/viewer/SceneContainer.vue` | Viewer 场景容器（三层） |
| `frontend/src/components/exhibitions/viewer/ExhibitDetailModal.vue` | 展品详情弹窗 |
| `frontend/src/components/exhibitions/viewer/HotspotButtons.vue` | 导航热点按钮 |
| `frontend/src/components/exhibitions/viewer/MiniMap.vue` | 展区缩略图 + 当前位置 |
| `frontend/src/components/exhibitions/viewer/DigitalHumanWidget.vue` | 数字人讲解（P0 文本气泡） |
| `frontend/src/components/exhibitions/viewer/NavigationControls.vue` | 前进/后退导航 |

### 修改文件

| 文件路径 | 修改内容 |
|---------|---------|
| `frontend/src/api/types.ts` | 新增 Zone/Exhibit/Hotspot/EditorBundle/Template 类型定义 |
| `frontend/src/views/exhibitions/ExhibitionCreateView.vue` | 增加模板选择步骤 |
| `frontend/src/views/exhibitions/Editor.vue` | 完全重写为多展区三层编辑器 |
| `frontend/src/views/exhibitions/ExhibitionViewer.vue` | 完全重写为多展区沉浸式浏览 |
| `frontend/src/composables/useCanvasHistory.ts` | 适配 per-zone 撤销重做 |
| `frontend/src/composables/useCanvasAutosave.ts` | 改为 draft-bundle 按展区保存 |
| `frontend/src/router/routes/index.ts` | 无需新增路由（已有 editor/viewer 路由） |

> **路径缩写约定**：下文中 `src/` = `frontend/src/`

---

## 任务 1：API 类型定义 + API 模块

**文件：**
- 修改：`src/api/types.ts`
- 创建：`src/api/modules/zones.ts`
- 创建：`src/api/modules/exhibits.ts`
- 创建：`src/api/modules/editor-bundle.ts`

- [ ] **步骤 1：在 types.ts 中追加展区/展品/热点/编辑器 Bundle 类型**

在 `types.ts` 末尾追加以下类型（不修改现有类型）：

```typescript
// ═══ 展区 (Zone) ═══

export interface ZoneSummary {
  id: number
  exhibitionId: number
  zoneCode: string
  zoneType: string
  title: string
  sortOrder: number
  status: string
}

export interface ZoneDetail extends ZoneSummary {
  subtitle?: string | null
  description?: string | null
  backgroundUrl?: string | null
  backgroundStyle?: Record<string, unknown> | null
  layoutConfig?: { slots: SlotConfig[] } | null
  transitionIn: string
  narrationText?: string | null
  narrationAudio?: string | null
  canvasData?: Record<string, unknown> | null
  assignedUserId?: number | null
  lockedBy?: number | null
  lockedAt?: string | null
  createdAt: string
  updatedAt: string
}

export interface SlotConfig {
  code: string
  x: number
  y: number
  w: number
  h: number
  label: string
}

export interface CreateZoneRequest {
  zoneCode: string
  zoneType: string
  title: string
  subtitle?: string | null
  description?: string | null
  sortOrder?: number
  backgroundUrl?: string | null
  transitionIn?: string
}

export interface UpdateZoneRequest {
  title?: string
  subtitle?: string | null
  description?: string | null
  backgroundUrl?: string | null
  backgroundStyle?: Record<string, unknown> | null
  transitionIn?: string
  narrationText?: string | null
  narrationAudio?: string | null
  canvasData?: Record<string, unknown> | null
}

// ═══ 展品 (Exhibit) ═══

export interface ExhibitSummary {
  id: number
  exhibitionId: number
  zoneId: number
  title: string
  exhibitType: string
  coverUrl?: string | null
  sortOrder: number
  status: string
}

export interface ExhibitDetail extends ExhibitSummary {
  slotCode?: string | null
  placementMode: string
  placementJson?: Record<string, unknown> | null
  subtitle?: string | null
  mediaUrl?: string | null
  sourceType: string
  museumResourceId?: number | null
  mediaAssetId?: number | null
  description?: string | null
  sourceInfo?: Record<string, unknown> | null
  knowledgePoints?: string[] | null
  narrations: ExhibitNarration[]
  interactions: ExhibitInteraction[]
  createdAt: string
  updatedAt: string
}

export interface ExhibitNarration {
  id: number
  exhibitId: number
  narrationType: string
  content: string
  audioUrl?: string | null
  voiceType?: string | null
  durationSeconds?: number | null
  sortOrder: number
}

export interface ExhibitInteraction {
  id: number
  exhibitId: number
  interactionType: string
  questionText: string
  optionsJson?: Record<string, unknown> | null
  correctAnswer?: string | null
  explanation?: string | null
  sortOrder: number
}

export interface CreateExhibitRequest {
  zoneId: number
  slotCode?: string | null
  placementMode?: string
  placementJson?: Record<string, unknown> | null
  title: string
  subtitle?: string | null
  exhibitType?: string
  coverUrl?: string | null
  mediaUrl?: string | null
  sourceType?: string
  museumResourceId?: number | null
  mediaAssetId?: number | null
  description?: string | null
  sourceInfo?: Record<string, unknown> | null
  knowledgePoints?: string[] | null
}

export interface UpdateExhibitRequest {
  title?: string
  subtitle?: string | null
  slotCode?: string | null
  placementMode?: string
  placementJson?: Record<string, unknown> | null
  exhibitType?: string
  coverUrl?: string | null
  mediaUrl?: string | null
  description?: string | null
  sourceInfo?: Record<string, unknown> | null
  knowledgePoints?: string[] | null
}

// ═══ 热点 (Hotspot) ═══

export interface HotspotDetail {
  id: number
  zoneId: number
  targetZoneId?: number | null
  hotspotType: string
  label?: string | null
  icon?: string | null
  xPercent: number
  yPercent: number
  wPercent: number
  hPercent: number
  styleJson?: Record<string, unknown> | null
  actionConfig?: Record<string, unknown> | null
  sortOrder: number
}

// ═══ 模板 (Template) ═══

export interface ExhibitionTemplate {
  id: number
  templateCode: string
  templateName: string
  templateType: string
  difficultyLevel: string
  description?: string | null
  previewUrl?: string | null
  zonesConfig: Record<string, unknown>
  suitableSubjects?: string[] | null
  suitableGrades?: string[] | null
  status: string
}

// ═══ Editor Bundle ═══

export interface EditorBundleResponse {
  exhibition: ExhibitionDetail
  zones: ZoneDetail[]
  exhibits: ExhibitDetail[]
  hotspots: HotspotDetail[]
  digitalHuman?: DigitalHuman | null
  template?: ExhibitionTemplate | null
  revision: number | null
}

export interface SaveBundleRequest {
  revision: number | null
  zones?: UpdateZoneRequest[] | null
  exhibits?: UpdateExhibitRequest[] | null
  canvasDataMap?: Record<string, Record<string, unknown>> | null
}

export interface SaveBundleResult {
  revision: number
}

export interface DraftBundleRequest {
  zoneCode: string
  canvasData?: Record<string, unknown> | null
  zoneMetadata?: UpdateZoneRequest | null
  exhibits?: UpdateExhibitRequest[] | null
}
```

- [ ] **步骤 2：修改 CreateExhibitionRequest 增加 templateCode**

在现有 `CreateExhibitionRequest` 接口中追加：

```typescript
export interface CreateExhibitionRequest {
  taskId?: number | null
  title: string
  summary?: string | null
  coverUrl?: string | null
  visibility?: 'private' | 'class' | 'public'
  groupName?: string | null
  templateCode?: string | null  // 新增：模板编码
}
```

- [ ] **步骤 3：修改 ExhibitionSummary 和 ExhibitionDetail 增加新字段**

```typescript
export interface ExhibitionSummary {
  // ... 已有字段 ...
  workflowStatus?: string | null      // 新增
  visibilityScope?: string | null      // 新增
  isFeatured?: boolean | null          // 新增
}

export interface ExhibitionDetail extends ExhibitionSummary {
  // ... 已有字段 ...
  bundleRevision?: number | null       // 新增
}
```

- [ ] **步骤 4：创建 zones.ts API 模块**

```typescript
// src/api/modules/zones.ts
import { http } from '@/utils/request'
import type { CreateZoneRequest, UpdateZoneRequest, ZoneDetail, ZoneSummary } from '@/api/types'

export function listZones(exhibitionId: number) {
  return http.get<ZoneSummary[]>(`/exhibitions/${exhibitionId}/zones`)
}

export function getZone(exhibitionId: number, zoneId: number) {
  return http.get<ZoneDetail>(`/exhibitions/${exhibitionId}/zones/${zoneId}`)
}

export function createZone(exhibitionId: number, payload: CreateZoneRequest) {
  return http.post<ZoneDetail>(`/exhibitions/${exhibitionId}/zones`, payload)
}

export function updateZone(exhibitionId: number, zoneId: number, payload: UpdateZoneRequest) {
  return http.put<void>(`/exhibitions/${exhibitionId}/zones/${zoneId}`, payload)
}

export function deleteZone(exhibitionId: number, zoneId: number) {
  return http.delete<void>(`/exhibitions/${exhibitionId}/zones/${zoneId}`)
}

export function reorderZones(exhibitionId: number, zoneIds: number[]) {
  return http.put<void>(`/exhibitions/${exhibitionId}/zones/reorder`, { zoneIds })
}

export function lockZone(exhibitionId: number, zoneId: number) {
  return http.put<void>(`/exhibitions/${exhibitionId}/zones/${zoneId}/lock`)
}

export function unlockZone(exhibitionId: number, zoneId: number) {
  return http.put<void>(`/exhibitions/${exhibitionId}/zones/${zoneId}/unlock`)
}

export function heartbeatZone(exhibitionId: number, zoneId: number) {
  return http.put<void>(`/exhibitions/${exhibitionId}/zones/${zoneId}/heartbeat`)
}
```

- [ ] **步骤 5：创建 exhibits.ts API 模块**

```typescript
// src/api/modules/exhibits.ts
import { http } from '@/utils/request'
import type {
  CreateExhibitRequest,
  ExhibitDetail,
  ExhibitSummary,
  UpdateExhibitRequest,
} from '@/api/types'

export function listExhibits(exhibitionId: number, zoneId?: number) {
  const params = zoneId ? { zoneId } : {}
  return http.get<ExhibitSummary[]>(`/exhibitions/${exhibitionId}/exhibits`, { params })
}

export function getExhibit(exhibitionId: number, exhibitId: number) {
  return http.get<ExhibitDetail>(`/exhibitions/${exhibitionId}/exhibits/${exhibitId}`)
}

export function createExhibit(exhibitionId: number, payload: CreateExhibitRequest) {
  return http.post<ExhibitDetail>(`/exhibitions/${exhibitionId}/exhibits`, payload)
}

export function updateExhibit(exhibitionId: number, exhibitId: number, payload: UpdateExhibitRequest) {
  return http.put<void>(`/exhibitions/${exhibitionId}/exhibits/${exhibitId}`, payload)
}

export function deleteExhibit(exhibitionId: number, exhibitId: number) {
  return http.delete<void>(`/exhibitions/${exhibitionId}/exhibits/${exhibitId}`)
}
```

- [ ] **步骤 6：创建 editor-bundle.ts API 模块**

```typescript
// src/api/modules/editor-bundle.ts
import { http } from '@/utils/request'
import type {
  DraftBundleRequest,
  EditorBundleResponse,
  SaveBundleRequest,
  SaveBundleResult,
} from '@/api/types'

export function getEditorBundle(exhibitionId: number) {
  return http.get<EditorBundleResponse>(`/exhibitions/${exhibitionId}/editor-bundle`)
}

export function saveEditorBundle(exhibitionId: number, payload: SaveBundleRequest) {
  return http.put<SaveBundleResult>(`/exhibitions/${exhibitionId}/editor-bundle`, payload)
}

export function saveDraftBundle(exhibitionId: number, payload: DraftBundleRequest) {
  return http.put<void>(`/exhibitions/${exhibitionId}/draft-bundle`, payload)
}
```

- [ ] **步骤 7：验证 TypeScript 编译**

```bash
cd frontend && npx vue-tsc --noEmit 2>&1 | head -20
```

预期：无新增 TS 错误（现有的可忽略，只关注新增文件）。

- [ ] **步骤 8：Commit**

```bash
git add frontend/src/api/
git commit -m "feat(frontend): add zone/exhibit/editor-bundle API types and modules"
```

---

## 任务 2：展厅创建流程改造 — 模板选择步骤

**文件：**
- 修改：`src/views/exhibitions/ExhibitionCreateView.vue`
- 修改：`src/api/modules/exhibitions.ts`（增加获取模板列表）

- [ ] **步骤 1：在 exhibitions.ts 中增加模板列表 API**

```typescript
export function getExhibitionTemplates() {
  return http.get<ExhibitionTemplate[]>('/exhibition-templates')
}
```

同时在 import 中增加 `ExhibitionTemplate`。

- [ ] **步骤 2：改造 ExhibitionCreateView — 增加模板选择步骤**

在表单中增加模板选择区域。用户创建展厅时可选择模板（或空白创建）。

改造要点：
1. 在 `fetchMeta` 中加载模板列表
2. 在表单中新增模板选择网格（卡片式选择）
3. 表单 `exhibitionForm` 增加 `templateCode` 字段
4. 提交时把 `templateCode` 传给后端

具体 UI：在"展厅标题"字段上方，新增一个"选择展厅模板"区域：
- 一行展示所有可用模板（卡片网格，3 列）
- 每个卡片显示模板名称、类型标签、难度标签、预览图（如有）
- 首张卡片为"空白展厅"（无模板，用户自由创建）
- 选中状态：蓝色边框高亮
- `exhibitionForm.templateCode` 默认空字符串（空白），选中模板后赋值

```vue
<!-- 模板选择区域 — 放在表单最上方 -->
<div class="mb-8 md:col-span-2">
  <span :class="labelClass">选择展厅模板</span>
  <p class="mb-3 text-xs text-stone-500">选择模板将预设展区结构和导航热点，也可选择空白展厅自由创建。</p>
  <div class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
    <!-- 空白展厅 -->
    <button
      type="button"
      class="template-card"
      :class="{ 'template-card-active': !exhibitionForm.templateCode }"
      @click="exhibitionForm.templateCode = ''"
    >
      <div class="flex h-24 items-center justify-center rounded-lg bg-stone-100 text-3xl text-stone-300">+</div>
      <p class="mt-2 text-sm font-medium text-stone-700">空白展厅</p>
      <p class="text-xs text-stone-400">自由创建，无预设结构</p>
    </button>
    <!-- 模板卡片 -->
    <button
      v-for="tpl in templates"
      :key="tpl.templateCode"
      type="button"
      class="template-card"
      :class="{ 'template-card-active': exhibitionForm.templateCode === tpl.templateCode }"
      @click="exhibitionForm.templateCode = tpl.templateCode"
    >
      <img v-if="tpl.previewUrl" :src="tpl.previewUrl" class="h-24 w-full rounded-lg object-cover" />
      <div v-else class="flex h-24 items-center justify-center rounded-lg bg-brand-50 text-sm text-brand-400">{{ tpl.templateType }}</div>
      <p class="mt-2 text-sm font-medium text-stone-700">{{ tpl.templateName }}</p>
      <div class="flex gap-1.5">
        <span class="rounded bg-stone-100 px-1.5 py-0.5 text-[10px] text-stone-500">{{ tpl.templateType }}</span>
        <span class="rounded bg-amber-50 px-1.5 py-0.5 text-[10px] text-amber-600">{{ tpl.difficultyLevel }}</span>
      </div>
    </button>
  </div>
</div>
```

样式追加：
```css
.template-card {
  @apply rounded-xl border-2 border-transparent bg-white p-3 text-left transition hover:border-stone-200 hover:shadow-sm;
}
.template-card-active {
  @apply border-brand-500 shadow-md shadow-brand-100;
}
```

- [ ] **步骤 3：修改 handleCreateExhibition 传递 templateCode**

```typescript
const payload: CreateExhibitionRequest = {
  // ... 已有字段 ...
  templateCode: exhibitionForm.templateCode || null,
}
```

- [ ] **步骤 4：验证编译**

```bash
cd frontend && npx vue-tsc --noEmit 2>&1 | head -20
```

- [ ] **步骤 5：Commit**

```bash
git add frontend/src/
git commit -m "feat(frontend): add template selection step to exhibition creation flow"
```

---

## 任务 3：核心 Composables — useZoneManager + useExhibitManager + useSceneRenderer

**文件：**
- 创建：`src/composables/useZoneManager.ts`
- 创建：`src/composables/useExhibitManager.ts`
- 创建：`src/composables/useSceneRenderer.ts`

- [ ] **步骤 1：创建 useZoneManager**

负责展区 CRUD、切换、排序、编辑锁心跳。

```typescript
// src/composables/useZoneManager.ts
import { computed, ref, shallowRef, type Ref } from 'vue'
import type { ZoneDetail, ZoneSummary } from '@/api/types'

export interface ZoneManagerOptions {
  exhibitionId: Ref<number>
  onZoneSwitch?: (from: ZoneDetail | null, to: ZoneDetail) => Promise<void> | void
}

export function useZoneManager(options: ZoneManagerOptions) {
  const zones = ref<ZoneDetail[]>([])
  const currentZone = shallowRef<ZoneDetail | null>(null)
  const currentZoneIndex = computed(() =>
    currentZone.value ? zones.value.findIndex(z => z.id === currentZone.value!.id) : -1,
  )
  const switching = ref(false)

  function setZones(list: ZoneDetail[]) {
    zones.value = list.sort((a, b) => a.sortOrder - b.sortOrder)
    if (!currentZone.value && zones.value.length > 0) {
      currentZone.value = zones.value[0]
    }
  }

  async function switchToZone(zone: ZoneDetail) {
    if (currentZone.value?.id === zone.id || switching.value) return
    switching.value = true
    try {
      await options.onZoneSwitch?.(currentZone.value, zone)
      currentZone.value = zone
    } finally {
      switching.value = false
    }
  }

  async function switchToIndex(index: number) {
    if (index >= 0 && index < zones.value.length) {
      await switchToZone(zones.value[index])
    }
  }

  async function switchToNext() {
    const idx = currentZoneIndex.value
    if (idx < zones.value.length - 1) await switchToIndex(idx + 1)
  }

  async function switchToPrev() {
    const idx = currentZoneIndex.value
    if (idx > 0) await switchToIndex(idx - 1)
  }

  function updateZoneInList(zoneId: number, patch: Partial<ZoneDetail>) {
    const idx = zones.value.findIndex(z => z.id === zoneId)
    if (idx >= 0) {
      zones.value[idx] = { ...zones.value[idx], ...patch }
      if (currentZone.value?.id === zoneId) {
        currentZone.value = zones.value[idx]
      }
    }
  }

  return {
    zones,
    currentZone,
    currentZoneIndex,
    switching,
    setZones,
    switchToZone,
    switchToIndex,
    switchToNext,
    switchToPrev,
    updateZoneInList,
  }
}
```

- [ ] **步骤 2：创建 useExhibitManager**

负责当前展区的展品列表管理。

```typescript
// src/composables/useExhibitManager.ts
import { computed, ref, type Ref } from 'vue'
import type { ExhibitDetail, ZoneDetail } from '@/api/types'

export function useExhibitManager(
  allExhibits: Ref<ExhibitDetail[]>,
  currentZone: Ref<ZoneDetail | null>,
) {
  const selectedExhibitId = ref<number | null>(null)

  const zoneExhibits = computed(() => {
    if (!currentZone.value) return []
    return allExhibits.value
      .filter(e => e.zoneId === currentZone.value!.id)
      .sort((a, b) => a.sortOrder - b.sortOrder)
  })

  const selectedExhibit = computed(() =>
    selectedExhibitId.value
      ? allExhibits.value.find(e => e.id === selectedExhibitId.value) ?? null
      : null,
  )

  function selectExhibit(id: number | null) {
    selectedExhibitId.value = id
  }

  function addExhibit(exhibit: ExhibitDetail) {
    allExhibits.value.push(exhibit)
  }

  function updateExhibit(exhibitId: number, patch: Partial<ExhibitDetail>) {
    const idx = allExhibits.value.findIndex(e => e.id === exhibitId)
    if (idx >= 0) {
      allExhibits.value[idx] = { ...allExhibits.value[idx], ...patch } as ExhibitDetail
    }
  }

  function removeExhibit(exhibitId: number) {
    const idx = allExhibits.value.findIndex(e => e.id === exhibitId)
    if (idx >= 0) allExhibits.value.splice(idx, 1)
    if (selectedExhibitId.value === exhibitId) selectedExhibitId.value = null
  }

  return {
    zoneExhibits,
    selectedExhibitId,
    selectedExhibit,
    selectExhibit,
    addExhibit,
    updateExhibit,
    removeExhibit,
  }
}
```

- [ ] **步骤 3：创建 useSceneRenderer**

负责三层渲染的 Fabric 生命周期管理和展区切换动画。

核心逻辑：
1. `initScene(zone)` — 设置背景 → 创建 Fabric（透明背景）→ loadCanvasJson → 渲染热点
2. `destroyScene()` — 销毁 Fabric Canvas → 清除热点 overlay
3. 切换时：saveCurrentCanvas → destroyScene → CSS 过渡 → initScene

```typescript
// src/composables/useSceneRenderer.ts
import { ref, shallowRef, type Ref } from 'vue'
import { Canvas } from 'fabric'
import type { HotspotDetail, ZoneDetail } from '@/api/types'

const LOGICAL_WIDTH = 1920
const LOGICAL_HEIGHT = 1080
const CUSTOM_PROPS = ['assetType', 'assetId', 'mediaUrl', 'assetName']

export interface SceneRendererOptions {
  canvasElRef: Ref<HTMLCanvasElement | null>
  wrapperRef: Ref<HTMLElement | null>
  allHotspots: Ref<HotspotDetail[]>
  onCanvasReady?: (canvas: Canvas) => void
  onCanvasDestroyed?: () => void
}

export function useSceneRenderer(options: SceneRendererOptions) {
  const fabricCanvas = shallowRef<Canvas | null>(null)
  const currentZoom = ref(1)
  const transitioning = ref(false)
  const currentBackgroundUrl = ref<string | null>(null)
  const currentHotspots = ref<HotspotDetail[]>([])

  function getCurrentCanvasJson(): Record<string, unknown> | null {
    const canvas = fabricCanvas.value
    if (!canvas) return null
    return (canvas as any).toJSON(CUSTOM_PROPS)
  }

  async function initScene(zone: ZoneDetail) {
    if (!options.canvasElRef.value) return

    currentBackgroundUrl.value = zone.backgroundUrl ?? null
    currentHotspots.value = options.allHotspots.value.filter(h => h.zoneId === zone.id)

    const canvas = new Canvas(options.canvasElRef.value, {
      width: LOGICAL_WIDTH,
      height: LOGICAL_HEIGHT,
      backgroundColor: 'transparent',
      selection: true,
    })
    fabricCanvas.value = canvas

    if (zone.canvasData && typeof zone.canvasData === 'object' && 'objects' in zone.canvasData) {
      await canvas.loadFromJSON(zone.canvasData)
    }

    fitToContainer()
    options.onCanvasReady?.(canvas)
  }

  function destroyScene() {
    options.onCanvasDestroyed?.()
    fabricCanvas.value?.dispose()
    fabricCanvas.value = null
    currentBackgroundUrl.value = null
    currentHotspots.value = []
  }

  async function switchScene(
    fromZone: ZoneDetail | null,
    toZone: ZoneDetail,
    saveCanvasData: (zoneId: number, json: Record<string, unknown>) => void,
  ) {
    transitioning.value = true
    try {
      // 1. 保存当前画布数据
      if (fromZone && fabricCanvas.value) {
        const json = getCurrentCanvasJson()
        if (json) saveCanvasData(fromZone.id, json)
      }
      // 2. 销毁当前场景
      destroyScene()
      // 3. CSS 过渡等待
      await new Promise(r => setTimeout(r, 300))
      // 4. 初始化新场景
      await initScene(toZone)
    } finally {
      transitioning.value = false
    }
  }

  function fitToContainer() {
    const canvas = fabricCanvas.value
    const wrapper = options.wrapperRef.value
    if (!canvas || !wrapper) return

    const padding = 0
    const availW = wrapper.clientWidth - padding
    const availH = wrapper.clientHeight - padding
    const fitRatio = Math.min(availW / LOGICAL_WIDTH, availH / LOGICAL_HEIGHT)

    currentZoom.value = fitRatio
    canvas.setZoom(fitRatio)
    canvas.setDimensions({
      width: LOGICAL_WIDTH * fitRatio,
      height: LOGICAL_HEIGHT * fitRatio,
    })
    canvas.requestRenderAll()
  }

  return {
    fabricCanvas,
    currentZoom,
    transitioning,
    currentBackgroundUrl,
    currentHotspots,
    LOGICAL_WIDTH,
    LOGICAL_HEIGHT,
    initScene,
    destroyScene,
    switchScene,
    fitToContainer,
    getCurrentCanvasJson,
  }
}
```

- [ ] **步骤 4：验证编译**

```bash
cd frontend && npx vue-tsc --noEmit 2>&1 | head -20
```

- [ ] **步骤 5：Commit**

```bash
git add frontend/src/composables/
git commit -m "feat(frontend): add useZoneManager, useExhibitManager, useSceneRenderer composables"
```

---

## 任务 4：编辑器组件 — 左侧栏（ZoneNavigator + ExhibitList）

**文件：**
- 创建：`src/components/exhibitions/editor/ZoneNavigator.vue`
- 创建：`src/components/exhibitions/editor/ExhibitList.vue`

- [ ] **步骤 1：创建 ZoneNavigator**

展区导航列表，显示展区名称/类型/状态，支持点击切换、添加展区。

Props:
- `zones: ZoneDetail[]`
- `currentZoneId: number | null`
- `switching: boolean`

Events:
- `@switch(zone: ZoneDetail)`
- `@add`

```vue
<template>
  <div class="space-y-1">
    <div class="mb-2 flex items-center justify-between">
      <span class="text-xs font-semibold uppercase tracking-widest text-gray-400">展区</span>
      <button type="button" class="rounded px-1.5 py-0.5 text-xs text-brand-600 hover:bg-brand-50" @click="$emit('add')">+ 新增</button>
    </div>
    <button
      v-for="zone in zones"
      :key="zone.id"
      type="button"
      :disabled="switching"
      class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-sm transition"
      :class="zone.id === currentZoneId
        ? 'bg-brand-50 text-brand-700 font-medium'
        : 'text-gray-600 hover:bg-gray-50'"
      @click="$emit('switch', zone)"
    >
      <span class="shrink-0 text-xs">{{ zoneTypeIcon(zone.zoneType) }}</span>
      <span class="min-w-0 flex-1 truncate">{{ zone.title }}</span>
      <span class="shrink-0 text-[10px] text-gray-400">#{{ zone.sortOrder + 1 }}</span>
    </button>
    <p v-if="zones.length === 0" class="py-4 text-center text-xs text-gray-400">暂无展区</p>
  </div>
</template>

<script setup lang="ts">
import type { ZoneDetail } from '@/api/types'

defineProps<{
  zones: ZoneDetail[]
  currentZoneId: number | null
  switching: boolean
}>()

defineEmits<{
  switch: [zone: ZoneDetail]
  add: []
}>()

function zoneTypeIcon(type: string): string {
  const icons: Record<string, string> = {
    entrance: '🚪',
    gallery: '🖼',
    closeup: '🔍',
    exit: '🚶',
    timeline_node: '📅',
    map_point: '📍',
  }
  return icons[type] ?? '📦'
}
</script>
```

- [ ] **步骤 2：创建 ExhibitList**

当前展区的展品列表，支持点击选中、添加展品。

Props:
- `exhibits: ExhibitDetail[]`
- `selectedExhibitId: number | null`

Events:
- `@select(id: number)`
- `@add`
- `@delete(id: number)`

```vue
<template>
  <div class="space-y-1">
    <div class="mb-2 flex items-center justify-between">
      <span class="text-xs font-semibold uppercase tracking-widest text-gray-400">展品</span>
      <button type="button" class="rounded px-1.5 py-0.5 text-xs text-brand-600 hover:bg-brand-50" @click="$emit('add')">+ 添加</button>
    </div>
    <div
      v-for="exhibit in exhibits"
      :key="exhibit.id"
      class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm transition cursor-pointer"
      :class="exhibit.id === selectedExhibitId
        ? 'bg-amber-50 text-amber-700 font-medium'
        : 'text-gray-600 hover:bg-gray-50'"
      @click="$emit('select', exhibit.id)"
    >
      <img v-if="exhibit.coverUrl" :src="exhibit.coverUrl" class="h-8 w-8 shrink-0 rounded object-cover" />
      <span v-else class="flex h-8 w-8 shrink-0 items-center justify-center rounded bg-gray-100 text-xs text-gray-400">{{ exhibitTypeIcon(exhibit.exhibitType) }}</span>
      <span class="min-w-0 flex-1 truncate">{{ exhibit.title }}</span>
      <button type="button" class="shrink-0 text-xs text-gray-300 hover:text-rose-500" @click.stop="$emit('delete', exhibit.id)">✕</button>
    </div>
    <p v-if="exhibits.length === 0" class="py-4 text-center text-xs text-gray-400">当前展区暂无展品</p>
  </div>
</template>

<script setup lang="ts">
import type { ExhibitDetail } from '@/api/types'

defineProps<{
  exhibits: ExhibitDetail[]
  selectedExhibitId: number | null
}>()

defineEmits<{
  select: [id: number]
  add: []
  delete: [id: number]
}>()

function exhibitTypeIcon(type: string): string {
  const icons: Record<string, string> = {
    image: '🖼',
    video: '🎬',
    audio: '🎵',
    document: '📄',
    model: '🧊',
    text: 'T',
  }
  return icons[type] ?? '📦'
}
</script>
```

- [ ] **步骤 3：验证编译 + Commit**

```bash
cd frontend && npx vue-tsc --noEmit
git add frontend/src/components/exhibitions/editor/
git commit -m "feat(frontend): add ZoneNavigator and ExhibitList editor components"
```

---

## 任务 5：编辑器组件 — 画布区（EditorCanvas + SceneBackground + HotspotOverlay + ExhibitSlotOverlay）

**文件：**
- 创建：`src/components/exhibitions/editor/EditorCanvas.vue`
- 创建：`src/components/exhibitions/editor/SceneBackground.vue`
- 创建：`src/components/exhibitions/editor/HotspotOverlay.vue`
- 创建：`src/components/exhibitions/editor/HotspotMarker.vue`
- 创建：`src/components/exhibitions/editor/ExhibitSlotOverlay.vue`

- [ ] **步骤 1：创建 SceneBackground（Layer 1）**

16:9 contain + letterbox，显示展区背景图，支持视差动画。

Props:
- `backgroundUrl: string | null`
- `backgroundStyle: Record<string, unknown> | null`
- `transitioning: boolean`

```vue
<template>
  <div class="absolute inset-0 overflow-hidden" :class="{ 'opacity-0 transition-opacity duration-300': transitioning, 'opacity-100 transition-opacity duration-300': !transitioning }">
    <img
      v-if="backgroundUrl"
      :src="backgroundUrl"
      class="h-full w-full object-contain"
      :style="bgInlineStyle"
      alt="场景背景"
    />
    <div v-else class="flex h-full w-full items-center justify-center bg-gradient-to-br from-stone-100 to-stone-200">
      <span class="text-sm text-stone-400">暂无背景图</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  backgroundUrl: string | null
  backgroundStyle?: Record<string, unknown> | null
  transitioning?: boolean
}>()

const bgInlineStyle = computed(() => {
  const style: Record<string, string> = {}
  if (props.backgroundStyle?.opacity != null) {
    style.opacity = String(props.backgroundStyle.opacity)
  }
  return style
})
</script>
```

- [ ] **步骤 2：创建 HotspotMarker**

单个热点标记，编辑器内只读（P0 不可拖拽编辑）。

Props:
- `hotspot: HotspotDetail`
- `zoom: number`

```vue
<template>
  <div
    class="absolute flex items-center justify-center rounded-full border-2 border-white/80 bg-brand-500/70 text-white shadow-lg backdrop-blur-sm transition-transform hover:scale-110 cursor-pointer"
    :style="positionStyle"
    :title="hotspot.label || hotspot.hotspotType"
  >
    <span class="text-xs font-bold">{{ iconText }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { HotspotDetail } from '@/api/types'

const props = defineProps<{
  hotspot: HotspotDetail
  zoom: number
  stageWidth: number
  stageHeight: number
}>()

const positionStyle = computed(() => {
  const z = props.zoom
  return {
    left: `${(props.hotspot.xPercent / 100) * props.stageWidth * z}px`,
    top: `${(props.hotspot.yPercent / 100) * props.stageHeight * z}px`,
    width: `${(props.hotspot.wPercent / 100) * props.stageWidth * z}px`,
    height: `${(props.hotspot.hPercent / 100) * props.stageHeight * z}px`,
  }
})

const iconText = computed(() => {
  const icons: Record<string, string> = {
    navigation: '→',
    exhibit_popup: '🖼',
    external_link: '🔗',
    narration_trigger: '🎙',
  }
  return icons[props.hotspot.hotspotType] ?? '•'
})
</script>
```

- [ ] **步骤 3：创建 HotspotOverlay（Layer 3）**

```vue
<template>
  <div class="pointer-events-none absolute inset-0">
    <HotspotMarker
      v-for="hs in hotspots"
      :key="hs.id"
      :hotspot="hs"
      :zoom="zoom"
      :stage-width="1920"
      :stage-height="1080"
      class="pointer-events-auto"
    />
  </div>
</template>

<script setup lang="ts">
import type { HotspotDetail } from '@/api/types'
import HotspotMarker from './HotspotMarker.vue'

defineProps<{
  hotspots: HotspotDetail[]
  zoom: number
}>()
</script>
```

- [ ] **步骤 4：创建 ExhibitSlotOverlay**

在编辑器中高亮展品槽位，方便拖入展品。

Props:
- `slots: SlotConfig[]`
- `zoom: number`
- `activeSlotCode: string | null`

```vue
<template>
  <div class="pointer-events-none absolute inset-0">
    <div
      v-for="slot in slots"
      :key="slot.code"
      class="absolute rounded-lg border-2 border-dashed transition-colors"
      :class="slot.code === activeSlotCode ? 'border-brand-500 bg-brand-500/10' : 'border-gray-300/50 bg-gray-100/20'"
      :style="slotStyle(slot)"
      :title="slot.label"
    >
      <span class="absolute left-1 top-0.5 text-[10px] text-gray-400">{{ slot.label }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SlotConfig } from '@/api/types'

const props = defineProps<{
  slots: SlotConfig[]
  zoom: number
  activeSlotCode?: string | null
}>()

function slotStyle(slot: SlotConfig): Record<string, string> {
  const z = props.zoom
  return {
    left: `${(slot.x / 100) * 1920 * z}px`,
    top: `${(slot.y / 100) * 1080 * z}px`,
    width: `${(slot.w / 100) * 1920 * z}px`,
    height: `${(slot.h / 100) * 1080 * z}px`,
  }
}
</script>
```

- [ ] **步骤 5：创建 EditorCanvas（三层容器）**

组合三层渲染：SceneBackground + Fabric canvas + HotspotOverlay + SlotOverlay

```vue
<template>
  <div ref="stageWrapper" class="relative overflow-hidden" :style="stageContainerStyle">
    <!-- Layer 1: 场景背景 -->
    <SceneBackground
      :background-url="backgroundUrl"
      :background-style="backgroundStyle"
      :transitioning="transitioning"
    />
    <!-- Layer 2: Fabric 画布 (透明) -->
    <div class="absolute inset-0 z-10">
      <canvas ref="canvasEl" />
    </div>
    <!-- 展品槽位高亮 -->
    <ExhibitSlotOverlay
      v-if="slots.length > 0"
      class="z-20"
      :slots="slots"
      :zoom="zoom"
      :active-slot-code="activeSlotCode"
    />
    <!-- Layer 3: 热点覆盖 -->
    <HotspotOverlay
      class="z-30"
      :hotspots="hotspots"
      :zoom="zoom"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { HotspotDetail, SlotConfig } from '@/api/types'
import SceneBackground from './SceneBackground.vue'
import HotspotOverlay from './HotspotOverlay.vue'
import ExhibitSlotOverlay from './ExhibitSlotOverlay.vue'

const props = defineProps<{
  backgroundUrl: string | null
  backgroundStyle?: Record<string, unknown> | null
  hotspots: HotspotDetail[]
  slots: SlotConfig[]
  zoom: number
  transitioning: boolean
  activeSlotCode?: string | null
}>()

const stageWrapper = ref<HTMLElement | null>(null)
const canvasEl = ref<HTMLCanvasElement | null>(null)

const stageContainerStyle = computed(() => ({
  width: `${1920 * props.zoom}px`,
  height: `${1080 * props.zoom}px`,
}))

defineExpose({ canvasEl, stageWrapper })
</script>
```

- [ ] **步骤 6：验证编译 + Commit**

```bash
cd frontend && npx vue-tsc --noEmit
git add frontend/src/components/exhibitions/editor/
git commit -m "feat(frontend): add EditorCanvas three-layer rendering components"
```

---

## 任务 6：编辑器组件 — 右侧栏（ZonePropertiesPanel + ExhibitPropertiesPanel）+ 底部（ZoneStrip）

**文件：**
- 创建：`src/components/exhibitions/editor/ZonePropertiesPanel.vue`
- 创建：`src/components/exhibitions/editor/ExhibitPropertiesPanel.vue`
- 创建：`src/components/exhibitions/editor/ZoneStrip.vue`

- [ ] **步骤 1：创建 ZonePropertiesPanel**

展区属性编辑面板：标题、副标题、描述、过渡方式、背景图、讲解文本。

Props:
- `zone: ZoneDetail | null`

Events:
- `@update(field: string, value: unknown)`

面板包含：展区标题/副标题/描述/过渡类型选择器（fade/slide-left/slide-right/zoom-in）/背景图 URL/讲解词文本域。

- [ ] **步骤 2：创建 ExhibitPropertiesPanel**

展品属性编辑面板：标题、描述、类型、来源、封面、讲解词、互动题。

Props:
- `exhibit: ExhibitDetail | null`

Events:
- `@update(field: string, value: unknown)`
- `@add-narration`
- `@add-interaction`

面板分为多个折叠区域：
1. **基本信息**：标题/副标题/描述/类型
2. **媒体**：封面/媒体 URL
3. **来源**：来源类型/来源信息/知识点
4. **讲解词**：讲解列表 + 添加按钮
5. **互动题**：互动列表 + 添加按钮

- [ ] **步骤 3：创建 ZoneStrip**

底部展区缩略图条。

Props:
- `zones: ZoneDetail[]`
- `currentZoneId: number | null`

Events:
- `@switch(zone: ZoneDetail)`
- `@add`

```vue
<template>
  <div class="flex items-center gap-2 overflow-x-auto border-t border-gray-200 bg-white px-4 py-2">
    <button
      v-for="zone in zones"
      :key="zone.id"
      type="button"
      class="shrink-0 rounded-lg border-2 px-4 py-2 text-xs transition"
      :class="zone.id === currentZoneId
        ? 'border-brand-500 bg-brand-50 text-brand-700 font-medium'
        : 'border-transparent bg-gray-50 text-gray-500 hover:bg-gray-100'"
      @click="$emit('switch', zone)"
    >
      {{ zone.title }}
    </button>
    <button
      type="button"
      class="shrink-0 rounded-lg border-2 border-dashed border-gray-200 px-4 py-2 text-xs text-gray-400 hover:border-gray-300 hover:text-gray-500"
      @click="$emit('add')"
    >
      + 添加展区
    </button>
  </div>
</template>

<script setup lang="ts">
import type { ZoneDetail } from '@/api/types'

defineProps<{
  zones: ZoneDetail[]
  currentZoneId: number | null
}>()

defineEmits<{
  switch: [zone: ZoneDetail]
  add: []
}>()
</script>
```

- [ ] **步骤 4：验证编译 + Commit**

```bash
cd frontend && npx vue-tsc --noEmit
git add frontend/src/components/exhibitions/editor/
git commit -m "feat(frontend): add ZonePropertiesPanel, ExhibitPropertiesPanel, ZoneStrip"
```

---

## 任务 7：Editor.vue 完全重写 — 多展区三层编辑器

**文件：**
- 修改：`src/views/exhibitions/Editor.vue`（完全重写）

这是 Phase 2 的核心任务，将现有单画布编辑器改造为多展区三层架构。

- [ ] **步骤 1：重写 Editor.vue template 部分**

新布局结构：
```
┌─────────────────────────────────────────────────────────────────┐
│ 顶栏: 返回│展厅名│自动保存│撤销/重做│保存│预览                   │
├────────┬──────────────────────────────────────────┬─────────────┤
│ 左侧栏  │            EditorCanvas 三层画布          │ 右侧栏      │
│ ZoneNav ││  SceneBackground + Fabric + Hotspots    ││ Zone/Exhibit│
│ Exhibits││                                        ││ Properties  │
│ Assets  ││                                        ││ TextStyle   │
│ Layers  ││                                        ││             │
├────────┴──────────────────────────────────────────┴─────────────┤
│ ZoneStrip: [入口大厅] [左展区] [中央展区] [右展区] [出口] [+]   │
└─────────────────────────────────────────────────────────────────┘
```

关键变化：
- onMounted 调用 `getEditorBundle` 整包加载
- 左侧栏增加"展区"和"展品"tab
- 画布区使用 `EditorCanvas` 组件
- 右侧栏根据选中内容切换：Fabric 对象属性 / 展区属性 / 展品属性
- 底部新增 `ZoneStrip`
- 保存调用 `saveEditorBundle`（含乐观锁 revision）
- 自动保存调用 `saveDraftBundle`

- [ ] **步骤 2：重写 Editor.vue script 部分**

核心逻辑：

```typescript
// 数据加载
const bundle = ref<EditorBundleResponse | null>(null)
const allExhibits = ref<ExhibitDetail[]>([])
const allHotspots = ref<HotspotDetail[]>([])
const revision = ref<number | null>(null)

// Composables
const zoneManager = useZoneManager({
  exhibitionId: computed(() => exhibitionId),
  onZoneSwitch: handleZoneSwitch,
})

const exhibitManager = useExhibitManager(allExhibits, zoneManager.currentZone)

const sceneRenderer = useSceneRenderer({
  canvasElRef: canvasElRef,
  wrapperRef: stageWrapperRef,
  allHotspots,
  onCanvasReady: (canvas) => {
    history.setCanvas(canvas)
    history.bindCanvasEvents()
    history.reset()
    // rebind autosave, shortcuts, alignGuides
  },
  onCanvasDestroyed: () => {
    history.unbindCanvasEvents()
  },
})

// 展区切换回调
async function handleZoneSwitch(from: ZoneDetail | null, to: ZoneDetail) {
  await sceneRenderer.switchScene(from, to, (zoneId, json) => {
    zoneManager.updateZoneInList(zoneId, { canvasData: json })
  })
}

// 整包加载
async function loadBundle() {
  bundle.value = await getEditorBundle(exhibitionId)
  zoneManager.setZones(bundle.value.zones)
  allExhibits.value = bundle.value.exhibits
  allHotspots.value = bundle.value.hotspots
  revision.value = bundle.value.revision
  // 初始化第一个展区场景
  if (zoneManager.currentZone.value) {
    await sceneRenderer.initScene(zoneManager.currentZone.value)
  }
}

// 保存（乐观锁）
async function handleSave() {
  // 先保存当前画布到内存
  if (zoneManager.currentZone.value && sceneRenderer.fabricCanvas.value) {
    const json = sceneRenderer.getCurrentCanvasJson()
    if (json) zoneManager.updateZoneInList(zoneManager.currentZone.value.id, { canvasData: json })
  }
  // 构建整包保存请求
  const canvasDataMap: Record<string, Record<string, unknown>> = {}
  for (const zone of zoneManager.zones.value) {
    if (zone.canvasData) canvasDataMap[zone.zoneCode] = zone.canvasData
  }
  try {
    const result = await saveEditorBundle(exhibitionId, {
      revision: revision.value,
      canvasDataMap,
    })
    revision.value = result.revision
    appStore.showToast('保存成功', 'success')
  } catch (error: any) {
    if (error?.response?.status === 409) {
      appStore.showToast('展厅内容已被其他成员更新，请刷新后重试', 'error')
    } else {
      appStore.showToast(getErrorMessage(error, '保存失败'), 'error')
    }
  }
}
```

- [ ] **步骤 3：验证编译**

```bash
cd frontend && npx vue-tsc --noEmit
```

- [ ] **步骤 4：Commit**

```bash
git add frontend/src/views/exhibitions/Editor.vue
git commit -m "feat(frontend): rewrite Editor.vue for multi-zone three-layer architecture"
```

---

## 任务 8：改造 useCanvasAutosave — 对接 draft-bundle

**文件：**
- 修改：`src/composables/useCanvasAutosave.ts`

- [ ] **步骤 1：改造为 draft-bundle 保存**

当前实现调用 `saveExhibitionVersion`（创建版本），改为调用 `saveDraftBundle`（按展区粒度写入，不创建版本）。

关键改动：
1. 保存回调不再创建版本，改为 `PUT /draft-bundle`
2. 需要传入当前展区的 `zoneCode` 和 `canvasData`
3. `draft-bundle` 不参与乐观锁，无需 revision

```typescript
// 改造后的核心保存逻辑
async function performAutosave() {
  const currentZone = getCurrentZone()
  if (!currentZone || !canvas.value) return
  
  autosaving.value = true
  try {
    const canvasData = (canvas.value as any).toJSON(CUSTOM_PROPS)
    await saveDraftBundle(exhibitionId.value, {
      zoneCode: currentZone.zoneCode,
      canvasData,
    })
    lastAutosaveAt.value = new Date()
    autosaveError.value = ''
  } catch (e) {
    autosaveError.value = getErrorMessage(e, '自动保存失败')
  } finally {
    autosaving.value = false
  }
}
```

- [ ] **步骤 2：验证编译 + Commit**

```bash
cd frontend && npx vue-tsc --noEmit
git add frontend/src/composables/useCanvasAutosave.ts
git commit -m "refactor(frontend): adapt useCanvasAutosave for draft-bundle API"
```

---

## 任务 9：Viewer 组件 — ExhibitDetailModal + HotspotButtons + MiniMap + DigitalHumanWidget + NavigationControls

**文件：**
- 创建：`src/components/exhibitions/viewer/ExhibitDetailModal.vue`
- 创建：`src/components/exhibitions/viewer/HotspotButtons.vue`
- 创建：`src/components/exhibitions/viewer/MiniMap.vue`
- 创建：`src/components/exhibitions/viewer/DigitalHumanWidget.vue`
- 创建：`src/components/exhibitions/viewer/NavigationControls.vue`
- 创建：`src/components/exhibitions/viewer/SceneContainer.vue`

- [ ] **步骤 1：创建 ExhibitDetailModal**

展品详情弹窗（设计文档 §6.3）：大图/视频 + 来源信息 + 知识点 + 讲解词 + 互动答题。

Props:
- `exhibit: ExhibitDetail | null`
- `visible: boolean`

Events:
- `@close`

- [ ] **步骤 2：创建 HotspotButtons**

导航热点按钮，点击切换展区。

Props:
- `hotspots: HotspotDetail[]`
- `zoom: number`

Events:
- `@navigate(targetZoneId: number)`

- [ ] **步骤 3：创建 MiniMap**

展区缩略图 + 当前位置高亮 + 已参观标记 + 点击跳转。

Props:
- `zones: ZoneDetail[]`
- `currentZoneId: number | null`
- `visitedZoneIds: Set<number>`

Events:
- `@jump(zone: ZoneDetail)`

- [ ] **步骤 4：创建 DigitalHumanWidget（P0 文本气泡）**

固定右下角数字人。P0 仅文本气泡（无 TTS）。两种状态：待机 / 讲解中。

Props:
- `digitalHuman: DigitalHuman | null`
- `narrationText: string | null`  // 当前展区讲解词 或 展品跟随讲解词

Events:
- `@click`（触发讲解）

数字人 P0 显示逻辑：
1. 待机状态：显示头像 + "点击我开始讲解"
2. 讲解中：显示文字气泡，逐字动画效果

- [ ] **步骤 5：创建 NavigationControls**

前进/后退箭头导航。

Props:
- `canGoNext: boolean`
- `canGoPrev: boolean`

Events:
- `@next`
- `@prev`

- [ ] **步骤 6：创建 SceneContainer**

Viewer 的三层场景容器（与编辑器类似但只读）。

- [ ] **步骤 7：验证编译 + Commit**

```bash
cd frontend && npx vue-tsc --noEmit
git add frontend/src/components/exhibitions/viewer/
git commit -m "feat(frontend): add viewer components - ExhibitDetailModal, HotspotButtons, MiniMap, DigitalHuman, Navigation"
```

---

## 任务 10：ExhibitionViewer.vue 完全重写 — 多展区沉浸式浏览

**文件：**
- 修改：`src/views/exhibitions/ExhibitionViewer.vue`（完全重写）

- [ ] **步骤 1：重写为多展区沉浸式浏览**

核心改造：
1. 加载 editor-bundle 整包数据（展区/展品/热点/数字人）
2. 三层渲染（背景 + 只读 Fabric + 热点按钮）
3. 展区导航（热点点击 / 前进后退 / 小地图）
4. 展品点击弹出详情弹窗
5. 数字人固定右下角，点击播放当前展区讲解词

数据读取源规则（§14.1）：
- 编辑器内预览 → 草稿态（当前实现）
- 对外公开浏览 → `published_version_id` 对应版本
- 教师审核 → 冻结版本（Phase 3）

P0 简化：直接从 editor-bundle 读取草稿态数据，后续 Phase 3 再区分数据源。

布局结构：
```
┌─────────────────────────────────────────────────────────────┐
│ ViewerTopBar: ← 返回 │ 展厅名 │ 作者                       │
├─────────────────────────────────────────────────────────────┤
│                    SceneContainer (全屏)                     │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  SceneBackground                                     │    │
│  │  Fabric (只读)                                       │    │
│  │  ExhibitClickAreas (展品悬停高亮)                     │    │
│  │  HotspotButtons (导航按钮)                           │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                             │
│  NavigationControls (左/右箭头)                              │
│  MiniMap (右上角)                                           │
│  DigitalHumanWidget (右下角)                                │
│  ExhibitDetailModal (弹窗)                                  │
├─────────────────────────────────────────────────────────────┤
│ CommentSection (已有的评论区)                                 │
└─────────────────────────────────────────────────────────────┘
```

- [ ] **步骤 2：重写 script 部分**

```typescript
// 数据加载 — 直接用 editor-bundle
const bundle = ref<EditorBundleResponse | null>(null)
const zones = ref<ZoneDetail[]>([])
const exhibits = ref<ExhibitDetail[]>([])
const hotspots = ref<HotspotDetail[]>([])

const currentZoneIndex = ref(0)
const currentZone = computed(() => zones.value[currentZoneIndex.value] ?? null)
const currentHotspots = computed(() =>
  hotspots.value.filter(h => h.zoneId === currentZone.value?.id)
)
const zoneExhibits = computed(() =>
  exhibits.value.filter(e => e.zoneId === currentZone.value?.id)
)

const visitedZoneIds = ref(new Set<number>())
const selectedExhibit = ref<ExhibitDetail | null>(null)

// 展区切换
async function navigateToZone(targetZoneId: number) {
  const idx = zones.value.findIndex(z => z.id === targetZoneId)
  if (idx >= 0) {
    // 销毁旧画布 → CSS 过渡 → 初始化新画布
    await switchViewerScene(zones.value[idx])
    currentZoneIndex.value = idx
    visitedZoneIds.value.add(targetZoneId)
  }
}

// 展品点击
function handleExhibitClick(exhibit: ExhibitDetail) {
  selectedExhibit.value = exhibit
}
```

- [ ] **步骤 3：验证编译 + Commit**

```bash
cd frontend && npx vue-tsc --noEmit
git add frontend/src/views/exhibitions/ExhibitionViewer.vue
git commit -m "feat(frontend): rewrite ExhibitionViewer for multi-zone immersive browsing"
```

---

## 任务 11：useCanvasHistory 适配 per-zone + useCanvasShortcuts 扩展展区快捷键

**文件：**
- 修改：`src/composables/useCanvasHistory.ts`
- 修改：`src/composables/useCanvasShortcuts.ts`

- [ ] **步骤 1：改造 useCanvasHistory 支持 per-zone**

当前实现绑定到单个 Canvas 实例。改造后需要支持：
1. 切换展区时保存当前展区的历史栈
2. 切回时恢复该展区的历史栈
3. 提供 `setCanvas` 方法供场景切换后重新绑定

关键：维护 `Map<number, { undoStack, redoStack }>` 按 zoneId 存储。

- [ ] **步骤 2：扩展 useCanvasShortcuts**

增加展区切换快捷键：
- `Ctrl+[` / `Ctrl+]` — 上/下一个展区
- `Ctrl+1~9` — 跳转到第 N 个展区

- [ ] **步骤 3：验证编译 + Commit**

```bash
cd frontend && npx vue-tsc --noEmit
git add frontend/src/composables/
git commit -m "refactor(frontend): adapt useCanvasHistory for per-zone, add zone switch shortcuts"
```

---

## 任务 12：最终集成验证

**文件：** 无新增

- [ ] **步骤 1：TypeScript 全量编译**

```bash
cd frontend && npx vue-tsc --noEmit
```

- [ ] **步骤 2：Vite 构建**

```bash
cd frontend && npx vite build
```

- [ ] **步骤 3：后端编译（确认无回归）**

```bash
cd backend && mvn compile -q
```

- [ ] **步骤 4：最终 Commit + Push**

```bash
git add .
git commit -m "feat: Phase 2 exhibition editor + viewer multi-zone upgrade"
git push
```

---

## 自检

### 规格覆盖度

| 设计规格章节 | 对应任务 | 覆盖情况 |
| ------------ | --------- | --------- |
| §2.1 三层渲染架构 | 任务 5（EditorCanvas）+ 任务 10（Viewer SceneContainer） | ✅ |
| §2.2 场景切换机制 | 任务 3（useSceneRenderer.switchScene）| ✅ |
| §2.4 坐标体系 16:9 + contain + letterbox | 任务 5（SceneBackground contain）+ 任务 3（LOGICAL_WIDTH/HEIGHT） | ✅ |
| §4.4 模板实例化规则 | 任务 2（CreateView 模板选择 → 后端实例化）| ✅ |
| §5.1 编辑器布局 | 任务 7（Editor.vue 重写）| ✅ |
| §5.2 组件树 | 任务 4-6（所有编辑器子组件）| ✅ |
| §5.3 核心 Composables | 任务 3（useZoneManager/useExhibitManager/useSceneRenderer）| ✅ |
| §5.4 展区切换流程 | 任务 3（useSceneRenderer.switchScene）+ 任务 7（handleZoneSwitch） | ✅ |
| §5.5 展品与 Fabric 职责边界 | 任务 6（ExhibitPropertiesPanel 结构化编辑）| ✅ |
| §5.6 自动保存 draft-bundle | 任务 8（useCanvasAutosave 改造）| ✅ |
| §5.7 editor-bundle 整包接口 + 乐观锁 | 任务 1（API）+ 任务 7（handleSave 409 处理）| ✅ |
| §6.1-§6.4 参观模式（场景/展品弹窗/热点/小地图） | 任务 9-10 | ✅ |
| §7.3 P0 数字人讲解（文本气泡） | 任务 9 步骤 4（DigitalHumanWidget）| ✅ |
| §13.4 P0 热点=模板预置不可编辑 | 任务 5（HotspotOverlay 只读）| ✅ |
| §14.1 Viewer 数据源规则 | 任务 10 步骤 1（P0 读草稿态，注释后续扩展点）| ✅（P0 简化） |
| §14.3 自动保存覆盖范围 | 任务 8（draft-bundle）| ✅ |
| §14.4 editor-bundle 冲突策略 | 任务 7 步骤 2（409 处理）| ✅ |
| useCanvasHistory per-zone | 任务 11 | ✅ |
| 展区快捷键 | 任务 11 | ✅ |

### 不在本 Phase 范围（后续 Phase 处理）

- **AI 讲解词生成 API + 编辑器集成**（Phase 3：§7.2）
- **提交审核全链路 + 教师审核评价页**（Phase 3：§8、§9）
- **热点可视化编辑器**（P1：§13.2 #1）
- **TTS 语音合成**（P1：§7.3）
- **AI 问答**（P2：§7.3）
- **版本对比**（P2：§9.5）
- **本地草稿恢复 IndexedDB**（P1：§9.3）
- **操作日志 UI**（P1：§9.4）
- **互评系统**（P1：§13.2 #4）
