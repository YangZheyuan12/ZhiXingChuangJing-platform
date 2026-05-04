<template>
  <div class="flex h-screen flex-col bg-neutral-50 text-gray-900">
    <!-- ═══ 顶栏 ═══ -->
    <header class="flex shrink-0 items-center justify-between border-b border-gray-200 bg-white px-4 py-2.5">
      <div class="flex items-center gap-3">
        <button type="button" class="rounded-md px-3 py-1.5 text-sm text-gray-500 transition hover:bg-gray-100" @click="router.back()">← 返回</button>
        <h1 class="text-sm font-semibold text-gray-800">{{ bundle?.exhibition.title || '展厅编辑器' }}</h1>
        <span v-if="currentZone" class="rounded bg-red-50 px-2 py-0.5 text-xs text-red-700">{{ currentZone.title }}</span>
      </div>

      <div class="flex items-center gap-2">
        <!-- 自动保存状态 -->
        <span v-if="autosaving" class="text-xs text-amber-500">保存中...</span>
        <span v-else-if="autosaveError" class="text-xs text-rose-500" :title="autosaveError">保存失败</span>
        <span v-else-if="lastAutosaveAt" class="text-xs text-gray-400">已自动保存 {{ autosaveTimeAgo }}</span>

        <!-- 冲突提示 -->
        <span v-if="conflictDetected" class="rounded bg-orange-100 px-2 py-0.5 text-xs text-orange-700">⚠ 冲突，请刷新</span>

        <!-- Undo / Redo -->
        <button type="button" :disabled="!canUndo" class="toolbar-btn" title="撤销 (Ctrl+Z)" @click="undo">↶</button>
        <button type="button" :disabled="!canRedo" class="toolbar-btn" title="重做 (Ctrl+Y)" @click="redo">↷</button>

        <div class="mx-1 h-5 w-px bg-gray-200" />

        <!-- 数字人摆放 -->
        <div class="relative">
          <button
            type="button"
            class="flex items-center gap-1.5 rounded-md border border-gray-200 bg-white px-3 py-1.5 text-xs font-medium text-gray-700 transition hover:border-brand-300 hover:bg-brand-50"
            :disabled="!currentZone || digitalHumanBusy"
            :title="currentZone ? '为当前展区指派数字人讲解员' : '请先选中或创建一个展区'"
            @click="showDigitalHumanMenu = !showDigitalHumanMenu"
          >
            <img
              v-if="currentZonePlacement?.avatar2dUrl"
              :src="currentZonePlacement.avatar2dUrl"
              :alt="currentZonePlacement.name"
              class="h-5 w-5 rounded-full object-cover"
            />
            <span v-else class="text-base leading-none">🧍</span>
            <span class="max-w-[8rem] truncate">
              {{ currentZonePlacement ? currentZonePlacement.name : '指派数字人' }}
            </span>
            <span class="text-[10px] text-gray-400">▾</span>
          </button>

          <!-- 透明全屏遮罩，点空白处关闭菜单 -->
          <div
            v-if="showDigitalHumanMenu"
            class="fixed inset-0 z-20"
            @click="showDigitalHumanMenu = false"
          />

          <!-- 下拉面板 -->
          <div
            v-if="showDigitalHumanMenu"
            class="absolute right-0 top-full z-30 mt-1 w-64 overflow-hidden rounded-lg border border-gray-200 bg-white shadow-lg"
          >
            <div class="border-b border-gray-100 bg-gray-50 px-3 py-2 text-[11px] text-gray-500">
              选择该展区的讲解员（最多 1 个）
            </div>

            <!-- 当前摆放的微调：缩放 + 朝向 -->
            <div
              v-if="currentZonePlacement"
              class="border-b border-gray-100 px-3 py-2.5"
            >
              <div class="mb-1.5 flex items-center justify-between text-[11px] text-gray-500">
                <span>缩放</span>
                <span class="tabular-nums text-gray-700">{{ currentZonePlacement.scale.toFixed(2) }}x</span>
              </div>
              <input
                type="range"
                min="0.5"
                max="2"
                step="0.05"
                :value="currentZonePlacement.scale"
                class="w-full accent-brand-600"
                @change="onPlacementScaleChange"
              />
              <div class="mt-2.5 flex items-center justify-between text-[11px] text-gray-500">
                <span>朝向</span>
                <div class="flex gap-1">
                  <button
                    type="button"
                    class="rounded-md px-2 py-0.5 text-xs transition"
                    :class="currentZonePlacement.facing === 'left' ? 'bg-brand-100 text-brand-700' : 'text-gray-500 hover:bg-gray-100'"
                    :disabled="digitalHumanBusy"
                    @click="handleUpdatePlacementAttrs({ facing: 'left' })"
                  >← 左</button>
                  <button
                    type="button"
                    class="rounded-md px-2 py-0.5 text-xs transition"
                    :class="currentZonePlacement.facing === 'right' ? 'bg-brand-100 text-brand-700' : 'text-gray-500 hover:bg-gray-100'"
                    :disabled="digitalHumanBusy"
                    @click="handleUpdatePlacementAttrs({ facing: 'right' })"
                  >右 →</button>
                </div>
              </div>
            </div>

            <div class="max-h-72 overflow-y-auto">
              <button
                v-if="currentZonePlacement"
                type="button"
                class="flex w-full items-center gap-2 border-b border-gray-100 px-3 py-2 text-left text-xs text-rose-600 transition hover:bg-rose-50 disabled:text-gray-400"
                :disabled="digitalHumanBusy"
                @click="handleRemoveZoneDigitalHuman"
              >
                <span class="text-base leading-none">🚫</span>
                <span>从当前展区撤下数字人</span>
              </button>

              <div v-if="digitalHumans.length === 0" class="px-3 py-6 text-center text-xs text-gray-400">
                还没有数字人角色。<br />
                请先到「展厅详情 → 数字人」创建。
              </div>

              <button
                v-for="dh in digitalHumans"
                :key="dh.id"
                type="button"
                class="flex w-full items-center gap-2 px-3 py-2 text-left transition hover:bg-brand-50 disabled:opacity-50"
                :class="{ 'bg-brand-50': currentZonePlacement?.digitalHumanId === dh.id }"
                :disabled="digitalHumanBusy"
                @click="handleAssignDigitalHuman(dh)"
              >
                <img
                  v-if="dh.avatar2dUrl"
                  :src="dh.avatar2dUrl"
                  :alt="dh.name"
                  class="h-8 w-8 shrink-0 rounded-full object-cover"
                />
                <div v-else class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-gray-100 text-base">
                  🧍
                </div>
                <div class="min-w-0 flex-1">
                  <div class="truncate text-xs font-medium text-gray-800">{{ dh.name }}</div>
                  <div v-if="dh.persona" class="truncate text-[11px] text-gray-400">{{ dh.persona }}</div>
                </div>
                <span
                  v-if="currentZonePlacement?.digitalHumanId === dh.id"
                  class="text-xs text-brand-600"
                >✓</span>
              </button>
            </div>
          </div>
        </div>

        <div class="mx-1 h-5 w-px bg-gray-200" />

        <!-- 保存 / 提交审核 / 发布 -->
        <button type="button" :disabled="saving || conflictDetected" class="rounded-md bg-brand-600 px-4 py-1.5 text-sm font-medium text-white transition hover:bg-brand-700 disabled:bg-gray-300" @click="handleSave">
          {{ saving ? '保存中...' : '保存全部' }}
        </button>
        <button
          v-if="bundle?.exhibition.taskId"
          type="button"
          :disabled="submittingForReview || !bundle?.exhibition.latestVersionNo"
          class="rounded-md border border-emerald-300 bg-emerald-50 px-4 py-1.5 text-sm font-medium text-emerald-700 transition hover:bg-emerald-100 disabled:border-gray-200 disabled:bg-gray-50 disabled:text-gray-400"
          :title="!bundle?.exhibition.latestVersionNo ? '请先保存一个版本再提交' : '提交审核'"
          @click="showSubmitDialog = true"
        >
          {{ submittingForReview ? '提交中...' : '提交审核' }}
        </button>
        <button type="button" :disabled="publishing" class="rounded-md border border-brand-200 px-4 py-1.5 text-sm font-medium text-brand-700 transition hover:bg-brand-50 disabled:border-gray-200 disabled:text-gray-400" @click="handlePublish">
          {{ publishing ? '发布中...' : '发布' }}
        </button>
      </div>
    </header>

    <SubmitForReviewDialog
      :visible="showSubmitDialog"
      :submitting="submittingForReview"
      @close="showSubmitDialog = false"
      @submit="handleSubmitForReview"
    />

    <CreateZoneDialog
      :visible="showCreateZoneDialog"
      :submitting="creatingZone"
      :next-sort-order="zones.length"
      @close="showCreateZoneDialog = false"
      @submit="handleCreateZoneSubmit"
    />

    <CreateExhibitDialog
      :visible="showCreateExhibitDialog"
      :submitting="creatingExhibit"
      :zone-id="currentZone?.id ?? null"
      :zone-title="currentZone?.title ?? ''"
      :available-slots="zoneSlots"
      @close="showCreateExhibitDialog = false"
      @submit="handleCreateExhibitSubmit"
    />

    <div class="flex min-h-0 flex-1 overflow-hidden">
      <!-- ═══ 左侧栏 ═══ -->
      <aside class="flex w-60 shrink-0 flex-col border-r border-gray-200 bg-white">
        <div class="flex border-b border-gray-100">
          <button
            v-for="tab in leftTabs"
            :key="tab.value"
            type="button"
            class="flex-1 py-2.5 text-xs font-medium transition"
            :class="activeLeftTab === tab.value ? 'border-b-2 border-brand-600 text-brand-700' : 'text-gray-400 hover:text-gray-600'"
            @click="activeLeftTab = tab.value"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="flex-1 overflow-y-auto p-3">
          <!-- 展区列表 -->
          <template v-if="activeLeftTab === 'zones'">
            <ZoneNavigator
              :zones="zones"
              :current-zone-id="currentZone?.id ?? null"
              :switching="switching"
              @switch="handleZoneSwitch"
              @add="openCreateZoneDialog"
              @delete="handleDeleteZone"
            />
            <div class="mt-4 border-t border-gray-100 pt-4">
              <ExhibitList
                :exhibits="zoneExhibits"
                :selected-exhibit-id="selectedExhibitId"
                @select="selectExhibit"
                @add="openCreateExhibitDialog"
                @delete="handleDeleteExhibit"
              />
            </div>
          </template>

          <!-- 组件库 -->
          <template v-else-if="activeLeftTab === 'components'">
            <div class="space-y-3">
              <div>
                <h4 class="mb-1.5 text-[11px] font-semibold uppercase tracking-wider text-gray-400">文本</h4>
                <div class="grid grid-cols-2 gap-2">
                  <button type="button" class="component-btn" @click="addTitle">
                    <span class="component-icon font-serif font-bold">H1</span>
                    <span class="component-label">大标题</span>
                  </button>
                  <button type="button" class="component-btn" @click="addSubtitle">
                    <span class="component-icon font-serif">H2</span>
                    <span class="component-label">副标题</span>
                  </button>
                  <button type="button" class="component-btn" @click="addParagraph">
                    <span class="component-icon">¶</span>
                    <span class="component-label">正文</span>
                  </button>
                  <button type="button" class="component-btn" @click="addTextbox">
                    <span class="component-icon">T</span>
                    <span class="component-label">文本框</span>
                  </button>
                  <button type="button" class="component-btn" @click="addQuote">
                    <span class="component-icon">"</span>
                    <span class="component-label">引用</span>
                  </button>
                  <button type="button" class="component-btn" @click="addBadge">
                    <span class="component-icon">●</span>
                    <span class="component-label">徽章</span>
                  </button>
                </div>
              </div>
              <div>
                <h4 class="mb-1.5 text-[11px] font-semibold uppercase tracking-wider text-gray-400">形状</h4>
                <div class="grid grid-cols-2 gap-2">
                  <button type="button" class="component-btn" @click="addRect">
                    <span class="component-icon">▭</span>
                    <span class="component-label">矩形</span>
                  </button>
                  <button type="button" class="component-btn" @click="addCard">
                    <span class="component-icon">▢</span>
                    <span class="component-label">卡片</span>
                  </button>
                  <button type="button" class="component-btn" @click="addCircle">
                    <span class="component-icon">●</span>
                    <span class="component-label">圆形</span>
                  </button>
                  <button type="button" class="component-btn" @click="addDivider">
                    <span class="component-icon">―</span>
                    <span class="component-label">分割线</span>
                  </button>
                </div>
              </div>
            </div>
          </template>

          <!-- 素材库 -->
          <template v-else-if="activeLeftTab === 'assets'">
            <AssetPicker
              @insert-asset="handleInsertAsset"
              @insert-museum="handleInsertMuseum"
            />
          </template>

          <!-- 图层 -->
          <template v-else-if="activeLeftTab === 'layers'">
            <LayerPanel
              :layers="layerList"
              @select="handleLayerSelect"
              @toggle-visible="handleLayerToggleVisible"
              @move-up="handleLayerMoveUp"
              @move-down="handleLayerMoveDown"
            />
          </template>

        </div>
      </aside>

      <!-- ═══ 画布区 ═══ -->
      <main class="relative flex min-w-0 flex-1 flex-col bg-neutral-100">
        <div class="flex shrink-0 items-center justify-between border-b border-gray-200 bg-white px-4 py-1.5">
          <div class="flex items-center gap-3">
            <span class="text-xs text-gray-400">{{ LOGICAL_WIDTH }} × {{ LOGICAL_HEIGHT }}</span>
            <button
              type="button"
              :disabled="!currentZone || hotspotBusy"
              class="flex items-center gap-1 rounded-md border border-amber-300 bg-amber-50 px-2.5 py-1 text-xs font-medium text-amber-700 transition hover:bg-amber-100 disabled:cursor-not-allowed disabled:border-gray-200 disabled:bg-gray-50 disabled:text-gray-300"
              :title="!currentZone ? '请先选择展区' : '在画布中央添加一个热点'"
              @click="handleHotspotCreate"
            >
              <span class="text-sm leading-none">＋</span>
              <span>添加热点</span>
            </button>
          </div>
          <div class="flex items-center gap-1">
            <button type="button" class="toolbar-btn text-xs" title="缩小" @click="zoomBy(-0.1)">−</button>
            <span class="w-12 text-center text-xs text-gray-400">{{ Math.round(currentZoom * 100) }}%</span>
            <button type="button" class="toolbar-btn text-xs" title="放大" @click="zoomBy(0.1)">+</button>
            <button type="button" class="toolbar-btn text-xs" title="适应窗口" @click="fitCanvasToContainer()">⊡</button>
          </div>
        </div>
        <div ref="canvasWrapper" class="flex flex-1 items-center justify-center overflow-hidden p-4">
          <EditorCanvas
            ref="editorCanvasRef"
            :background-url="currentBackgroundUrl"
            :background-style="currentZone?.backgroundStyle ?? null"
            :hotspots="currentHotspots"
            :slots="zoneSlots"
            :exhibits="zoneExhibits"
            :zoom="currentZoom"
            :transitioning="transitioning"
            :active-slot-code="selectedExhibit?.slotCode ?? null"
            :selected-hotspot-id="selectedHotspotId"
            :selected-exhibit-id="selectedExhibitId"
            :hotspot-draggable="activeRightTab === 'hotspot'"
            :digital-human-placement="currentZonePlacement"
            :digital-human-draggable="!!currentZonePlacement"
            @hotspot-select="handleHotspotClick"
            @hotspot-drag-end="handleHotspotDragEnd"
            @digital-human-drag-end="handleDigitalHumanDragEnd"
          />
        </div>

        <ZoneStrip
          :zones="zones"
          :current-zone-id="currentZone?.id ?? null"
          @switch="handleZoneSwitch"
          @add="openCreateZoneDialog"
        />
      </main>

      <!-- ═══ 右侧栏：属性面板 ═══ -->
      <aside class="w-64 shrink-0 overflow-y-auto border-l border-gray-200 bg-white p-4">
        <div class="mb-3 flex border-b border-gray-100">
          <button
            v-for="tab in rightTabs"
            :key="tab.value"
            type="button"
            class="flex-1 py-2 text-xs font-medium transition"
            :class="activeRightTab === tab.value ? 'border-b-2 border-brand-600 text-brand-700' : 'text-gray-400 hover:text-gray-600'"
            @click="activeRightTab = tab.value"
          >
            {{ tab.label }}
          </button>
        </div>

        <template v-if="activeRightTab === 'zone'">
          <ZonePropertiesPanel
            :zone="currentZone"
            @update="handleZonePropUpdate"
          />
        </template>

        <template v-else-if="activeRightTab === 'exhibit'">
          <ExhibitPropertiesPanel
            :exhibit="selectedExhibit"
            @update="handleExhibitPropUpdate"
            @ai-narration="handleAiNarration"
            @add-narration="handleAddNarration"
            @add-interaction="handleAddInteraction"
          />
        </template>

        <template v-else-if="activeRightTab === 'hotspot'">
          <HotspotPropertiesPanel
            :hotspot="selectedHotspot"
            :available-zones="zones"
            @update="handleHotspotPropUpdate"
            @delete="handleHotspotDelete"
            @deselect="selectedHotspotId = null"
            @create="handleHotspotCreate"
          />
        </template>

        <template v-else-if="activeRightTab === 'element'">
          <h3 class="mb-4 text-xs font-semibold uppercase tracking-widest text-gray-400">元素属性</h3>
          <template v-if="selectedObject">
            <div class="space-y-4">
              <div class="space-y-3">
                <label v-for="prop in propertyFields" :key="prop.key" class="block">
                  <span class="text-xs text-gray-500">{{ prop.label }}</span>
                  <input
                    type="number"
                    class="mt-1 w-full rounded-md border border-gray-200 px-2.5 py-1.5 text-sm focus:border-brand-400 focus:outline-none"
                    :value="Math.round(selectedProps[prop.key])"
                    @input="onPropInput(prop.key, $event)"
                  />
                </label>
              </div>

              <div v-if="isTextboxSelected" class="border-t border-gray-100 pt-4">
                <TextStylePanel
                  :font-family="textStyle.fontFamily"
                  :font-size="textStyle.fontSize"
                  :fill="textStyle.fill"
                  :font-weight="textStyle.fontWeight"
                  :font-style="textStyle.fontStyle"
                  :underline="textStyle.underline"
                  :text-align="textStyle.textAlign"
                  @update="handleTextStyleUpdate"
                />
              </div>

              <div class="border-t border-gray-100 pt-4">
                <button
                  type="button"
                  class="w-full rounded-lg border border-rose-200 bg-rose-50 py-2 text-sm font-medium text-rose-700 transition hover:border-rose-300 hover:bg-rose-100"
                  title="删除选中元素（快捷键 Del）"
                  @click="handleDeleteSelected"
                >
                  删除元素
                </button>
              </div>
            </div>
          </template>
          <p v-else class="text-xs text-gray-400">选中画布元素后可编辑属性。</p>
        </template>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, shallowRef, toRef, triggerRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Canvas, Circle, Line, Rect, Shadow, Textbox, FabricImage, Group, type FabricObject } from 'fabric'
import { publishExhibition } from '@/api/modules/exhibitions'
import { getEditorBundle, saveEditorBundle } from '@/api/modules/editor-bundle'
import { submitTaskWork } from '@/api/modules/tasks'
import { createZone, deleteZone as deleteZoneApi, getZone, updateZone } from '@/api/modules/zones'
import {
  createExhibit as createExhibitApi,
  deleteExhibit as deleteExhibitApi,
  getExhibit,
  updateExhibit,
  upsertExhibitNarration,
  upsertExhibitInteraction,
} from '@/api/modules/exhibits'
import {
  createHotspot as createHotspotApi,
  updateHotspot as updateHotspotApi,
  deleteHotspot as deleteHotspotApi,
} from '@/api/modules/hotspots'
import {
  placeZoneDigitalHuman,
  removeZoneDigitalHuman,
} from '@/api/modules/digital-human'
import type {
  Asset,
  CreateExhibitRequest,
  CreateZoneRequest,
  DigitalHuman,
  ZoneDigitalHumanPlacement,
  EditorBundleResponse,
  ExhibitDetail,
  HotspotDetail,
  MuseumResource,
  SlotConfig,
  UpdateExhibitRequest,
  UpdateZoneRequest,
  ZoneDetail,
} from '@/api/types'
import { getErrorMessage } from '@/utils/request'
import { useAppStore } from '@/stores/app'
import { useCanvasHistory } from '@/composables/useCanvasHistory'
import { useCanvasAutosave } from '@/composables/useCanvasAutosave'
import { useCanvasShortcuts } from '@/composables/useCanvasShortcuts'
import { useZoneManager } from '@/composables/useZoneManager'
import { useExhibitManager } from '@/composables/useExhibitManager'
import AssetPicker from '@/components/exhibitions/editor/AssetPicker.vue'
import LayerPanel from '@/components/exhibitions/editor/LayerPanel.vue'
import TextStylePanel from '@/components/exhibitions/editor/TextStylePanel.vue'
import type { LayerItem } from '@/components/exhibitions/editor/LayerPanel.vue'
import { useAlignmentGuides } from '@/composables/useAlignmentGuides'
import ZoneNavigator from '@/components/exhibitions/editor/ZoneNavigator.vue'
import ExhibitList from '@/components/exhibitions/editor/ExhibitList.vue'
import EditorCanvas from '@/components/exhibitions/editor/EditorCanvas.vue'
import ZoneStrip from '@/components/exhibitions/editor/ZoneStrip.vue'
import ZonePropertiesPanel from '@/components/exhibitions/editor/ZonePropertiesPanel.vue'
import ExhibitPropertiesPanel from '@/components/exhibitions/editor/ExhibitPropertiesPanel.vue'
import HotspotPropertiesPanel from '@/components/exhibitions/editor/HotspotPropertiesPanel.vue'
import SubmitForReviewDialog from '@/components/exhibitions/editor/SubmitForReviewDialog.vue'
import CreateZoneDialog from '@/components/exhibitions/editor/CreateZoneDialog.vue'
import CreateExhibitDialog from '@/components/exhibitions/editor/CreateExhibitDialog.vue'

// ─── 常量 ───
const LOGICAL_WIDTH = 1920
const LOGICAL_HEIGHT = 1080
const CUSTOM_PROPS = ['assetType', 'assetId', 'mediaUrl', 'assetName', 'exhibitId']

// ─── 路由 & Store ───
const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const exhibitionId = Number(route.params.exhibitionId)

// ─── 基础状态 ───
const bundle = ref<EditorBundleResponse | null>(null)
const bundleRevision = ref<number | null>(null)
const allExhibits = ref<ExhibitDetail[]>([])
const allHotspots = ref<HotspotDetail[]>([])
const digitalHumans = ref<DigitalHuman[]>([])
const zonePlacements = ref<ZoneDigitalHumanPlacement[]>([])
const showDigitalHumanMenu = ref(false)
const digitalHumanBusy = ref(false)
const saving = ref(false)
const publishing = ref(false)
const conflictDetected = ref(false)
const currentZoom = ref(1)

// ─── 提交审核 ───
const showSubmitDialog = ref(false)
const submittingForReview = ref(false)

// ─── 展区/展品 CRUD 对话框 ───
const showCreateZoneDialog = ref(false)
const creatingZone = ref(false)
const showCreateExhibitDialog = ref(false)
const creatingExhibit = ref(false)

// ─── 展区切换时保存画布数据的缓存 ───
const canvasDataCache = new Map<number, Record<string, unknown>>()

// ─── 属性面板 debounce 下发 API 的待决补丁 ───
const PATCH_DEBOUNCE_MS = 500
const zonePatchTimers = new Map<number, ReturnType<typeof setTimeout>>()
const zonePendingPatches = new Map<number, Record<string, unknown>>()
const exhibitPatchTimers = new Map<number, ReturnType<typeof setTimeout>>()
const exhibitPendingPatches = new Map<number, Record<string, unknown>>()

// ─── 左侧栏 ───
const leftTabs = [
  { label: '展区', value: 'zones' as const },
  { label: '组件库', value: 'components' as const },
  { label: '素材库', value: 'assets' as const },
  { label: '图层', value: 'layers' as const },
]
const activeLeftTab = ref<'zones' | 'components' | 'assets' | 'layers'>('zones')

// ─── 右侧栏 ───
const rightTabs = [
  { label: '展区', value: 'zone' as const },
  { label: '展品', value: 'exhibit' as const },
  { label: '热点', value: 'hotspot' as const },
  { label: '元素', value: 'element' as const },
]
const activeRightTab = ref<'zone' | 'exhibit' | 'hotspot' | 'element'>('zone')

// ─── 热点选中状态 ───
const selectedHotspotId = ref<number | null>(null)
const selectedHotspot = computed<HotspotDetail | null>(() =>
  selectedHotspotId.value
    ? allHotspots.value.find(h => h.id === selectedHotspotId.value) ?? null
    : null,
)
const hotspotBusy = ref(false)

// ─── 画布 ───
const canvasWrapper = ref<HTMLElement | null>(null)
const editorCanvasRef = ref<InstanceType<typeof EditorCanvas> | null>(null)
const fabricCanvas = shallowRef<Canvas | null>(null)
let resizeObserver: ResizeObserver | null = null

// ─── 三层渲染的 reactive 状态 ───
const currentBackgroundUrl = ref<string | null>(null)
const currentHotspots = computed<HotspotDetail[]>(() =>
  currentZone.value
    ? allHotspots.value.filter(h => h.zoneId === currentZone.value!.id)
    : [],
)
const transitioning = ref(false)

// ─── 选中元素属性 ───
const selectedObject = shallowRef<FabricObject | null>(null)
const selectedProps = reactive({ x: 0, y: 0, w: 0, h: 0 })
const layerVersion = ref(0)
const propertyFields = [
  { key: 'x' as const, label: 'X 坐标' },
  { key: 'y' as const, label: 'Y 坐标' },
  { key: 'w' as const, label: '宽度' },
  { key: 'h' as const, label: '高度' },
]

// ─── Zone Manager ───
const exhibitionIdRef = toRef(() => exhibitionId)
const zm = useZoneManager({
  exhibitionId: exhibitionIdRef,
  onZoneSwitch: handleZoneSwitchInternal,
})
const { zones, currentZone, switching } = zm

// ─── Exhibit Manager ───
const em = useExhibitManager(allExhibits, currentZone as any)
const { zoneExhibits, selectedExhibitId, selectedExhibit, selectExhibit } = em

// ─── 展区插槽 ───
const zoneSlots = computed<SlotConfig[]>(() => {
  if (!currentZone.value?.layoutConfig?.slots) return []
  return currentZone.value.layoutConfig.slots
})

// ─── 当前展区的数字人摆放（zone : 数字人 = 1 : 1） ───
const currentZonePlacement = computed<ZoneDigitalHumanPlacement | null>(() => {
  if (!currentZone.value) return null
  return zonePlacements.value.find(p => p.zoneId === currentZone.value!.id) ?? null
})

// ─── Composables ───
const history = useCanvasHistory(fabricCanvas)
const { canUndo, canRedo, undo, redo } = history

const autosave = useCanvasAutosave(
  fabricCanvas,
  () => exhibitionId,
  {
    currentZoneCode: () => currentZone.value?.zoneCode ?? null,
    getCanvasJson: () => getCanvasJsonClean(),
  },
)

// 序列化画布时过滤掉展品 anchor 对象 —— 它们的位置存在 exhibit.placementJson，不走 layoutConfig
function getCanvasJsonClean(): Record<string, unknown> | null {
  const canvas = fabricCanvas.value
  if (!canvas) return null
  const json = (canvas as any).toJSON(CUSTOM_PROPS)
  if (json && Array.isArray(json.objects)) {
    json.objects = json.objects.filter((o: any) => o?.assetType !== 'exhibit-anchor')
  }
  return json
}
const { lastAutosaveAt, autosaveError, autosaving } = autosave

const autosaveTimeAgo = computed(() => {
  if (!lastAutosaveAt.value) return ''
  const diff = Math.round((Date.now() - lastAutosaveAt.value.getTime()) / 1000)
  if (diff < 5) return '刚刚'
  if (diff < 60) return `${diff}秒前`
  return `${Math.round(diff / 60)}分钟前`
})

const shortcuts = useCanvasShortcuts(fabricCanvas, {
  undo: () => undo(),
  redo: () => redo(),
  save: () => handleSave(),
  prevZone: () => navigateToPrevZone(),
  nextZone: () => navigateToNextZone(),
})

const alignGuides = useAlignmentGuides(fabricCanvas, LOGICAL_WIDTH, LOGICAL_HEIGHT)

// ═══════════════════════════════════════════════════════════
//  画布初始化 & 坐标系
// ═══════════════════════════════════════════════════════════

function initCanvas() {
  const canvasEl = editorCanvasRef.value?.canvasEl
  if (!canvasEl) return
  const canvas = new Canvas(canvasEl, {
    width: LOGICAL_WIDTH,
    height: LOGICAL_HEIGHT,
    backgroundColor: 'transparent',
    selection: true,
  })
  fabricCanvas.value = canvas

  canvas.on('selection:created', syncSelectedProps)
  canvas.on('selection:updated', syncSelectedProps)
  canvas.on('selection:cleared', () => {
    selectedObject.value = null
    selectedHotspotId.value = null
  })
  canvas.on('object:modified', (e: any) => {
    syncSelectedProps()
    const target = e?.target as FabricObject | undefined
    if (target && (target as any).assetType === 'exhibit-anchor') {
      handleAnchorModified(target)
    }
  })
  canvas.on('object:added', refreshLayers)
  canvas.on('object:removed', refreshLayers)

  const wrapper = editorCanvasRef.value?.stageWrapper
  if (wrapper) {
    resizeObserver = new ResizeObserver(() => fitCanvasToContainer())
    resizeObserver.observe(wrapper)
  }

  fitCanvasToContainer()
  history.bindCanvasEvents()
  history.reset()
  autosave.bindCanvasEvents()
  shortcuts.bind()
  alignGuides.bind()

  canvasWrapper.value?.addEventListener('wheel', handleWheel, { passive: false })
}

function fitCanvasToContainer() {
  const canvas = fabricCanvas.value
  const wrapper = canvasWrapper.value
  if (!canvas || !wrapper) return

  const padding = 32
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

// ═══════════════════════════════════════════════════════════
//  展区切换
// ═══════════════════════════════════════════════════════════

function handleZoneSwitch(zone: ZoneDetail) {
  zm.switchToZone(zone)
}

async function handleZoneSwitchInternal(from: ZoneDetail | null, to: ZoneDetail) {
  transitioning.value = true
  // 先 flush 自动保存和属性面板待决补丁，避免丢失老展区未存改动
  await Promise.all([
    autosave.flush(),
    flushAllPendingPatches(),
  ])
  const canvas = fabricCanvas.value
  if (from && canvas) {
    const cleanJson = getCanvasJsonClean()
    if (cleanJson) canvasDataCache.set(from.id, cleanJson)
  }

  if (canvas) {
    canvas.clear()
    canvas.backgroundColor = 'transparent'
  }

  currentBackgroundUrl.value = to.backgroundUrl ?? null
  // currentHotspots is computed; updates automatically when allHotspots/currentZone change

  await new Promise(r => setTimeout(r, 200))

  if (canvas) {
    const cached = canvasDataCache.get(to.id)
    const data = cached ?? to.canvasData
    if (data && typeof data === 'object' && 'objects' in data) {
      await canvas.loadFromJSON(data)
    }
    fitCanvasToContainer()
    canvas.requestRenderAll()
  }

  // 同步展品 anchor（在 layoutConfig 被加载后进行）
  await syncExhibitAnchors()

  history.switchZone(to.zoneCode)
  history.reset()
  transitioning.value = false
}

function navigateToPrevZone() {
  const idx = zones.value.findIndex(z => z.id === currentZone.value?.id)
  if (idx > 0) handleZoneSwitch(zones.value[idx - 1])
}

function navigateToNextZone() {
  const idx = zones.value.findIndex(z => z.id === currentZone.value?.id)
  if (idx >= 0 && idx < zones.value.length - 1) handleZoneSwitch(zones.value[idx + 1])
}

function openCreateZoneDialog() {
  showCreateZoneDialog.value = true
}

async function handleCreateZoneSubmit(payload: CreateZoneRequest) {
  if (creatingZone.value) return
  creatingZone.value = true
  try {
    const { id } = await createZone(exhibitionId, payload)
    const detail = await getZone(exhibitionId, id)
    zm.addZoneToList(detail)
    showCreateZoneDialog.value = false
    appStore.showToast(`展区「${detail.title}」已创建`, 'success')
    await handleZoneSwitch(detail)
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '创建展区失败'), 'error')
  } finally {
    creatingZone.value = false
  }
}

async function handleDeleteZone(zoneId: number) {
  const zone = zones.value.find(z => z.id === zoneId)
  if (!zone) return
  if (zones.value.length <= 1) {
    appStore.showToast('至少保留一个展区', 'error')
    return
  }
  const exhibitCount = allExhibits.value.filter(e => e.zoneId === zoneId).length
  const confirmMsg = exhibitCount > 0
    ? `删除展区「${zone.title}」将同时删除其下 ${exhibitCount} 个展品，确认继续？`
    : `确认删除展区「${zone.title}」？`
  if (!window.confirm(confirmMsg)) return
  // 删除前 flush，避免已写入 pending 但尚未下发的补丁在删区后才发出去
  await flushAllPendingPatches()
  // 丢弃待决展品补丁（反正展品要一起删）
  for (const ex of allExhibits.value.filter(e => e.zoneId === zoneId)) {
    exhibitPendingPatches.delete(ex.id)
    const t = exhibitPatchTimers.get(ex.id)
    if (t) clearTimeout(t)
    exhibitPatchTimers.delete(ex.id)
  }
  try {
    await deleteZoneApi(exhibitionId, zoneId)
    // 移除本地展品
    allExhibits.value = allExhibits.value.filter(e => e.zoneId !== zoneId)
    zm.removeZoneFromList(zoneId)
    canvasDataCache.delete(zoneId)
    appStore.showToast('展区已删除', 'success')
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '删除展区失败'), 'error')
  }
}

// ═══════════════════════════════════════════════════════════
//  展品操作
// ═══════════════════════════════════════════════════════════

function openCreateExhibitDialog() {
  if (!currentZone.value) {
    appStore.showToast('请先选择一个展区', 'info')
    return
  }
  showCreateExhibitDialog.value = true
}

async function handleCreateExhibitSubmit(payload: CreateExhibitRequest) {
  if (creatingExhibit.value) return
  creatingExhibit.value = true
  try {
    const { id } = await createExhibitApi(exhibitionId, payload)
    const detail = await getExhibit(exhibitionId, id)
    em.addExhibit(detail)
    em.selectExhibit(detail.id)
    showCreateExhibitDialog.value = false
    appStore.showToast(`展品「${detail.title}」已创建`, 'success')
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '创建展品失败'), 'error')
  } finally {
    creatingExhibit.value = false
  }
}

async function handleDeleteExhibit(id: number) {
  const exhibit = allExhibits.value.find(e => e.id === id)
  if (!exhibit) return
  if (!window.confirm(`确认删除展品「${exhibit.title}」？`)) return
  try {
    await deleteExhibitApi(exhibitionId, id)
    em.removeExhibit(id)
    appStore.showToast('展品已删除', 'success')
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '删除展品失败'), 'error')
  }
}

// ─── 展区/展品属性更新 ───

function handleZonePropUpdate(field: string, value: unknown) {
  if (!currentZone.value) return
  const zoneId = currentZone.value.id
  zm.updateZoneInList(zoneId, { [field]: value } as Partial<ZoneDetail>)
  if (field === 'backgroundUrl') {
    currentBackgroundUrl.value = (value as string) ?? null
  }
  const pending = zonePendingPatches.get(zoneId) ?? {}
  pending[field] = value
  zonePendingPatches.set(zoneId, pending)
  scheduleZonePatch(zoneId)
}

function handleExhibitPropUpdate(field: string, value: unknown) {
  if (!selectedExhibit.value) return
  const exhibitId = selectedExhibit.value.id
  em.updateExhibit(exhibitId, { [field]: value } as Partial<ExhibitDetail>)
  const pending = exhibitPendingPatches.get(exhibitId) ?? {}
  pending[field] = value
  exhibitPendingPatches.set(exhibitId, pending)
  scheduleExhibitPatch(exhibitId)
}

function scheduleZonePatch(zoneId: number) {
  const existing = zonePatchTimers.get(zoneId)
  if (existing) clearTimeout(existing)
  zonePatchTimers.set(zoneId, setTimeout(() => { void flushZonePatch(zoneId) }, PATCH_DEBOUNCE_MS))
}

function scheduleExhibitPatch(exhibitId: number) {
  const existing = exhibitPatchTimers.get(exhibitId)
  if (existing) clearTimeout(existing)
  exhibitPatchTimers.set(exhibitId, setTimeout(() => { void flushExhibitPatch(exhibitId) }, PATCH_DEBOUNCE_MS))
}

async function flushZonePatch(zoneId: number) {
  const patch = zonePendingPatches.get(zoneId)
  zonePendingPatches.delete(zoneId)
  const timer = zonePatchTimers.get(zoneId)
  if (timer) clearTimeout(timer)
  zonePatchTimers.delete(zoneId)
  if (!patch || Object.keys(patch).length === 0) return
  try {
    await updateZone(exhibitionId, zoneId, patch as UpdateZoneRequest)
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '展区更新失败'), 'error')
  }
}

async function flushExhibitPatch(exhibitId: number) {
  const patch = exhibitPendingPatches.get(exhibitId)
  exhibitPendingPatches.delete(exhibitId)
  const timer = exhibitPatchTimers.get(exhibitId)
  if (timer) clearTimeout(timer)
  exhibitPatchTimers.delete(exhibitId)
  if (!patch || Object.keys(patch).length === 0) return
  try {
    await updateExhibit(exhibitionId, exhibitId, patch as UpdateExhibitRequest)
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '展品更新失败'), 'error')
  }
}

async function flushAllPendingPatches() {
  const zoneIds = [...zonePendingPatches.keys()]
  const exhibitIds = [...exhibitPendingPatches.keys()]
  await Promise.all([
    ...zoneIds.map(id => flushZonePatch(id)),
    ...exhibitIds.map(id => flushExhibitPatch(id)),
  ])
}

async function handleAiNarration(narration: string, _suggestions: string[]) {
  const current = selectedExhibit.value
  if (!current) {
    appStore.showToast('未选中展品，无法保存讲解词', 'error')
    return
  }
  try {
    await upsertExhibitNarration(exhibitionId, current.id, {
      content: narration,
      narrationType: 'ai',
      sortOrder: current.narrations.length,
    })
    const refreshed = await getExhibit(exhibitionId, current.id)
    em.updateExhibit(current.id, { narrations: refreshed.narrations })
    appStore.showToast('AI 讲解词已保存', 'success')
  } catch (error) {
    appStore.showToast(getErrorMessage(error, 'AI 讲解词保存失败'), 'error')
  }
  void _suggestions
}

async function handleAddNarration() {
  const current = selectedExhibit.value
  if (!current) return
  const content = window.prompt('请输入讲解词内容：', '')
  if (!content || !content.trim()) return
  try {
    await upsertExhibitNarration(exhibitionId, current.id, {
      content: content.trim(),
      narrationType: 'text',
      sortOrder: current.narrations.length,
    })
    const refreshed = await getExhibit(exhibitionId, current.id)
    em.updateExhibit(current.id, { narrations: refreshed.narrations })
    appStore.showToast('讲解词已添加', 'success')
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '讲解词添加失败'), 'error')
  }
}

async function handleAddInteraction() {
  const current = selectedExhibit.value
  if (!current) return
  const questionText = window.prompt('请输入互动题目：', '')
  if (!questionText || !questionText.trim()) return
  try {
    await upsertExhibitInteraction(exhibitionId, current.id, {
      interactionType: 'open_question',
      questionText: questionText.trim(),
      sortOrder: current.interactions.length,
    })
    const refreshed = await getExhibit(exhibitionId, current.id)
    em.updateExhibit(current.id, { interactions: refreshed.interactions })
    appStore.showToast('互动题已添加', 'success')
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '互动题添加失败'), 'error')
  }
}

// ═══════════════════════════════════════════════════════════
//  热点操作（CRUD）
// ═══════════════════════════════════════════════════════════

function handleHotspotClick(id: number) {
  selectedHotspotId.value = id
  activeRightTab.value = 'hotspot'
}

// ═══════════════════════════════════════════════════════════
//  展品 ↔ Fabric 画布对象（anchor）双向同步
// ═══════════════════════════════════════════════════════════

const TYPE_LABELS_FOR_ANCHOR: Record<string, string> = {
  image: '图片', video: '视频', audio: '音频', text: '文本',
  model: '3D', '3d': '3D', artifact: '文物', document: '文档',
}

const anchorPatchTimers = new Map<number, ReturnType<typeof setTimeout>>()

function computeAnchorBox(
  ex: ExhibitDetail,
  slotMap: Map<string, SlotConfig>,
): { left: number; top: number; width: number; height: number } | null {
  let x = 0, y = 0, w = 0, h = 0
  if (ex.placementMode === 'slot' && ex.slotCode) {
    const s = slotMap.get(ex.slotCode)
    if (!s) return null
    x = s.x; y = s.y; w = s.w; h = s.h
  } else {
    const p = (ex.placementJson ?? {}) as Record<string, unknown>
    x = typeof p.x === 'number' ? p.x : 35
    y = typeof p.y === 'number' ? p.y : 35
    w = typeof p.w === 'number' ? p.w : 30
    h = typeof p.h === 'number' ? p.h : 30
  }
  return {
    left: (x / 100) * LOGICAL_WIDTH,
    top: (y / 100) * LOGICAL_HEIGHT,
    width: (w / 100) * LOGICAL_WIDTH,
    height: (h / 100) * LOGICAL_HEIGHT,
  }
}

function createPlaceholderAnchor(
  ex: ExhibitDetail,
  box: { left: number; top: number; width: number; height: number },
): FabricObject {
  const bg = new Rect({
    left: 0, top: 0,
    width: box.width, height: box.height,
    fill: '#f1f5f9', stroke: '#cbd5e1', strokeWidth: 1,
    rx: 8, ry: 8,
    selectable: false, evented: false,
  })
  const typeBadge = new Textbox(TYPE_LABELS_FOR_ANCHOR[ex.exhibitType] ?? ex.exhibitType, {
    left: 8, top: 6, width: Math.max(40, box.width - 16),
    fontSize: 11, fill: '#64748b',
    fontFamily: 'sans-serif',
    selectable: false, evented: false,
  })
  const title = new Textbox(ex.title, {
    left: 8, top: 24, width: Math.max(40, box.width - 16),
    fontSize: 14, fill: '#1e293b', fontWeight: '500',
    fontFamily: 'sans-serif',
    selectable: false, evented: false,
  })
  return new Group([bg, typeBadge, title], {
    left: box.left, top: box.top,
    originX: 'left', originY: 'top',
  })
}

async function createExhibitAnchor(
  ex: ExhibitDetail,
  box: { left: number; top: number; width: number; height: number },
): Promise<FabricObject | null> {
  const url = ex.coverUrl || ex.mediaUrl
  let obj: FabricObject
  if (url) {
    try {
      const img = await FabricImage.fromURL(url, { crossOrigin: 'anonymous' })
      const iw = img.width ?? box.width
      const ih = img.height ?? box.height
      img.set({
        left: box.left, top: box.top,
        scaleX: box.width / iw,
        scaleY: box.height / ih,
        originX: 'left', originY: 'top',
      })
      obj = img
    } catch {
      obj = createPlaceholderAnchor(ex, box)
    }
  } else {
    obj = createPlaceholderAnchor(ex, box)
  }
  ;(obj as any).assetType = 'exhibit-anchor'
  ;(obj as any).exhibitId = ex.id
  applyDefaultControls(obj)
  // 展品保持卡片正向，禁用旋转
  obj.setControlsVisibility({
    tl: true, tr: true, bl: true, br: true,
    ml: true, mr: true, mt: true, mb: true,
    mtr: false,
  })
  return obj
}

async function syncExhibitAnchors() {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const exhibits = zoneExhibits.value
  const slots = zoneSlots.value
  const slotMap = new Map(slots.map(s => [s.code, s]))

  // 现有 anchor 索引
  const existingMap = new Map<number, FabricObject>()
  for (const o of canvas.getObjects()) {
    if ((o as any).assetType === 'exhibit-anchor') {
      const id = (o as any).exhibitId
      if (typeof id === 'number') existingMap.set(id, o)
    }
  }

  // 删除已不存在的 anchor
  const wantIds = new Set(exhibits.map(e => e.id))
  for (const [id, obj] of existingMap) {
    if (!wantIds.has(id)) canvas.remove(obj)
  }

  // 创建/更新需要的 anchor
  const active = canvas.getActiveObject()
  for (const ex of exhibits) {
    const box = computeAnchorBox(ex, slotMap)
    if (!box) continue
    const existing = existingMap.get(ex.id)
    if (existing) {
      // 用户正在拖动当前 anchor 时不打断
      if (existing === active) continue
      const curLeft = existing.left ?? 0
      const curTop = existing.top ?? 0
      const curWidth = (existing.width ?? 0) * (existing.scaleX ?? 1)
      const curHeight = (existing.height ?? 0) * (existing.scaleY ?? 1)
      const eps = 1
      if (
        Math.abs(curLeft - box.left) > eps ||
        Math.abs(curTop - box.top) > eps ||
        Math.abs(curWidth - box.width) > eps ||
        Math.abs(curHeight - box.height) > eps
      ) {
        const iw = (existing as any).width ?? 1
        const ih = (existing as any).height ?? 1
        // FabricImage 用 scale 调尺寸；Group 直接设 width/height + scale=1
        if (existing.type === 'image') {
          existing.set({
            left: box.left, top: box.top,
            scaleX: box.width / iw,
            scaleY: box.height / ih,
          })
        } else {
          existing.set({
            left: box.left, top: box.top,
            scaleX: box.width / iw,
            scaleY: box.height / ih,
          })
        }
        existing.setCoords()
      }
      continue
    }
    const anchor = await createExhibitAnchor(ex, box)
    if (anchor) canvas.add(anchor)
  }
  canvas.requestRenderAll()
}

// zoneExhibits 变化 → 同步画布 anchor（增/删/封面更新）
watch(
  () => zoneExhibits.value.map(e => ({
    id: e.id,
    coverUrl: e.coverUrl,
    mediaUrl: e.mediaUrl,
    title: e.title,
    placementMode: e.placementMode,
    placementJson: e.placementJson,
    slotCode: e.slotCode,
  })),
  () => { void syncExhibitAnchors() },
  { deep: true },
)

function handleAnchorModified(obj: FabricObject) {
  const exhibitId = (obj as any).exhibitId as number | undefined
  if (typeof exhibitId !== 'number') return
  const left = obj.left ?? 0
  const top = obj.top ?? 0
  const width = (obj.width ?? 0) * (obj.scaleX ?? 1)
  const height = (obj.height ?? 0) * (obj.scaleY ?? 1)
  const placement = {
    x: Math.round((left / LOGICAL_WIDTH) * 10000) / 100,
    y: Math.round((top / LOGICAL_HEIGHT) * 10000) / 100,
    w: Math.round((width / LOGICAL_WIDTH) * 10000) / 100,
    h: Math.round((height / LOGICAL_HEIGHT) * 10000) / 100,
  }
  // 乐观更新本地 placementMode / placementJson
  em.updateExhibit(exhibitId, {
    placementMode: 'freeform',
    placementJson: placement,
  } as Partial<ExhibitDetail>)
  // debounce 回写后端
  const existing = anchorPatchTimers.get(exhibitId)
  if (existing) clearTimeout(existing)
  anchorPatchTimers.set(exhibitId, setTimeout(async () => {
    anchorPatchTimers.delete(exhibitId)
    try {
      await updateExhibit(exhibitionId, exhibitId, {
        placementMode: 'freeform',
        placementJson: placement,
      })
    } catch (error) {
      appStore.showToast(getErrorMessage(error, '位置保存失败'), 'error')
    }
  }, 400))
}

// ═══════════════════════════════════════════════════════════
//  数字人摆放
// ═══════════════════════════════════════════════════════════

/** 把指定数字人摆到当前展区，已存在则覆盖（包括位置）。 */
async function handleAssignDigitalHuman(digitalHuman: DigitalHuman) {
  const zone = currentZone.value
  if (!zone) {
    appStore.showToast('请先选择一个展区', 'info')
    return
  }
  digitalHumanBusy.value = true
  try {
    const placement = await placeZoneDigitalHuman(exhibitionId, zone.id, {
      digitalHumanId: digitalHuman.id,
    })
    upsertLocalPlacement(placement)
    appStore.showToast(`已让「${digitalHuman.name}」驻守该展区`, 'success')
    showDigitalHumanMenu.value = false
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '数字人摆放失败'), 'error')
  } finally {
    digitalHumanBusy.value = false
  }
}

/** 把当前展区的数字人撤下（不删除角色本身）。 */
async function handleRemoveZoneDigitalHuman() {
  const zone = currentZone.value
  if (!zone) return
  if (!currentZonePlacement.value) return
  digitalHumanBusy.value = true
  try {
    await removeZoneDigitalHuman(exhibitionId, zone.id)
    zonePlacements.value = zonePlacements.value.filter(p => p.zoneId !== zone.id)
    appStore.showToast('数字人已从该展区撤下', 'success')
    showDigitalHumanMenu.value = false
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '撤下数字人失败'), 'error')
  } finally {
    digitalHumanBusy.value = false
  }
}

/** 修改当前摆放的缩放/朝向等非位置属性。 */
async function handleUpdatePlacementAttrs(patch: { scale?: number; facing?: 'left' | 'right' }) {
  const zone = currentZone.value
  const placement = currentZonePlacement.value
  if (!zone || !placement) return
  const original = { ...placement }
  const merged = { ...placement, ...patch }
  upsertLocalPlacement(merged)
  try {
    const updated = await placeZoneDigitalHuman(exhibitionId, zone.id, {
      digitalHumanId: placement.digitalHumanId,
      xPercent: merged.xPercent,
      yPercent: merged.yPercent,
      scale: merged.scale,
      facing: merged.facing,
    })
    upsertLocalPlacement(updated)
  } catch (error) {
    upsertLocalPlacement(original)
    appStore.showToast(getErrorMessage(error, '更新数字人属性失败'), 'error')
  }
}

/** 缩放滑块 onchange（松手时一次 API 调用）。 */
function onPlacementScaleChange(e: Event) {
  const target = e.target as HTMLInputElement
  const value = Number(target.value)
  if (Number.isFinite(value)) {
    handleUpdatePlacementAttrs({ scale: value })
  }
}

/** 画布拖拽 emit 上来的位置变化。 */
async function handleDigitalHumanDragEnd(xPercent: number, yPercent: number) {
  const zone = currentZone.value
  const placement = currentZonePlacement.value
  if (!zone || !placement) return
  // 乐观更新：先改本地，请求失败再回滚
  const original = { ...placement }
  upsertLocalPlacement({ ...placement, xPercent, yPercent })
  try {
    const updated = await placeZoneDigitalHuman(exhibitionId, zone.id, {
      digitalHumanId: placement.digitalHumanId,
      xPercent,
      yPercent,
      scale: placement.scale,
      facing: placement.facing,
    })
    upsertLocalPlacement(updated)
  } catch (error) {
    upsertLocalPlacement(original)
    appStore.showToast(getErrorMessage(error, '保存数字人位置失败'), 'error')
  }
}

function upsertLocalPlacement(placement: ZoneDigitalHumanPlacement) {
  const idx = zonePlacements.value.findIndex(p => p.zoneId === placement.zoneId)
  if (idx >= 0) {
    zonePlacements.value.splice(idx, 1, placement)
  } else {
    zonePlacements.value = [...zonePlacements.value, placement]
  }
}

async function handleHotspotCreate() {
  const zone = currentZone.value
  if (!zone) {
    appStore.showToast('请先选择一个展区', 'info')
    return
  }
  if (hotspotBusy.value) return
  hotspotBusy.value = true
  try {
    const existingCount = currentHotspots.value.length
    const created = await createHotspotApi(exhibitionId, zone.id, {
      hotspotType: 'navigation',
      label: '新热点',
      icon: 'arrow-right',
      xPercent: 50,
      yPercent: 50,
      wPercent: 6,
      hPercent: 6,
      sortOrder: existingCount,
    })
    allHotspots.value = [...allHotspots.value, created]
    selectedHotspotId.value = created.id
    activeRightTab.value = 'hotspot'
    appStore.showToast('热点已添加', 'success')
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '创建热点失败'), 'error')
  } finally {
    hotspotBusy.value = false
  }
}

async function handleHotspotDragEnd(id: number, xPercent: number, yPercent: number) {
  const target = allHotspots.value.find(h => h.id === id)
  if (!target) return
  const before = { ...target }
  // 乐观更新位置
  allHotspots.value = allHotspots.value.map(h =>
    h.id === id ? { ...h, xPercent, yPercent } : h,
  )
  selectedHotspotId.value = id
  activeRightTab.value = 'hotspot'
  if (hotspotBusy.value) return
  hotspotBusy.value = true
  try {
    const updated = await updateHotspotApi(exhibitionId, id, {
      xPercent,
      yPercent,
    })
    allHotspots.value = allHotspots.value.map(h => (h.id === id ? updated : h))
  } catch (error) {
    allHotspots.value = allHotspots.value.map(h => (h.id === id ? before : h))
    appStore.showToast(getErrorMessage(error, '更新热点位置失败'), 'error')
  } finally {
    hotspotBusy.value = false
  }
}

async function handleHotspotPropUpdate(field: string, value: unknown) {
  const current = selectedHotspot.value
  if (!current) return
  // 乐观更新
  const before = { ...current }
  allHotspots.value = allHotspots.value.map(h =>
    h.id === current.id ? { ...h, [field]: value } : h,
  )
  if (hotspotBusy.value) return
  hotspotBusy.value = true
  try {
    const updated = await updateHotspotApi(exhibitionId, current.id, {
      [field]: value,
    } as Record<string, unknown>)
    allHotspots.value = allHotspots.value.map(h =>
      h.id === current.id ? updated : h,
    )
  } catch (error) {
    // 回滚
    allHotspots.value = allHotspots.value.map(h =>
      h.id === current.id ? before : h,
    )
    appStore.showToast(getErrorMessage(error, '更新热点失败'), 'error')
  } finally {
    hotspotBusy.value = false
  }
}

async function handleHotspotDelete() {
  const current = selectedHotspot.value
  if (!current) return
  if (!window.confirm(`确认删除热点「${current.label || current.hotspotType}」？`)) return
  if (hotspotBusy.value) return
  hotspotBusy.value = true
  try {
    await deleteHotspotApi(exhibitionId, current.id)
    allHotspots.value = allHotspots.value.filter(h => h.id !== current.id)
    selectedHotspotId.value = null
    appStore.showToast('热点已删除', 'success')
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '删除热点失败'), 'error')
  } finally {
    hotspotBusy.value = false
  }
}

// ═══════════════════════════════════════════════════════════
//  选中元素属性同步
// ═══════════════════════════════════════════════════════════

function syncSelectedProps() {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const obj = canvas.getActiveObject()
  if (!obj) { selectedObject.value = null; return }
  selectedObject.value = obj
  selectedProps.x = obj.left ?? 0
  selectedProps.y = obj.top ?? 0
  selectedProps.w = (obj.width ?? 0) * (obj.scaleX ?? 1)
  selectedProps.h = (obj.height ?? 0) * (obj.scaleY ?? 1)
  // 展品 anchor 被选中 → 路由到展品 Tab
  if ((obj as any).assetType === 'exhibit-anchor') {
    const exhibitId = (obj as any).exhibitId
    if (typeof exhibitId === 'number') em.selectExhibit(exhibitId)
    activeRightTab.value = 'exhibit'
  } else {
    activeRightTab.value = 'element'
  }
}

function onPropInput(key: string, e: Event) {
  const value = (e.target as HTMLInputElement)?.value ?? ''
  handlePropChange(key, value)
}

function handlePropChange(key: string, rawValue: string) {
  const obj = selectedObject.value
  const canvas = fabricCanvas.value
  if (!obj || !canvas) return
  const value = Number(rawValue) || 0
  if (key === 'x') obj.set('left', value)
  else if (key === 'y') obj.set('top', value)
  else if (key === 'w') obj.set('scaleX', value / (obj.width ?? 1))
  else if (key === 'h') obj.set('scaleY', value / (obj.height ?? 1))
  obj.setCoords()
  canvas.requestRenderAll()
}

// ═══════════════════════════════════════════════════════════
//  图层管理
// ═══════════════════════════════════════════════════════════

const layerList = computed<LayerItem[]>(() => {
  layerVersion.value
  const canvas = fabricCanvas.value
  if (!canvas) return []
  const objects = canvas.getObjects().filter((o) => !(o as any).__isGuide)
  const activeObj = canvas.getActiveObject()
  return [...objects].reverse().map((obj) => {
    const name = (obj as any).assetName || (obj as any).text?.slice(0, 20) || obj.type || 'object'
    const icon = obj.type === 'textbox' ? 'T' : obj.type === 'rect' ? '▬' : obj.type === 'group' ? '▦' : obj.type === 'image' ? '🖼' : '●'
    return {
      icon,
      label: name,
      active: obj === activeObj,
      visible: obj.visible !== false,
    }
  })
})

function getFilteredObjects(): FabricObject[] {
  const canvas = fabricCanvas.value
  if (!canvas) return []
  return canvas.getObjects().filter((o) => !(o as any).__isGuide)
}

function getReversedObjects(): FabricObject[] {
  return [...getFilteredObjects()].reverse()
}

function handleLayerSelect(idx: number) {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const obj = getReversedObjects()[idx]
  if (obj) {
    canvas.setActiveObject(obj)
    canvas.requestRenderAll()
  }
}

function handleLayerToggleVisible(idx: number) {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const obj = getReversedObjects()[idx]
  if (obj) {
    obj.set('visible', !obj.visible)
    canvas.requestRenderAll()
  }
}

function handleLayerMoveUp(idx: number) {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const reversed = getReversedObjects()
  if (idx <= 0) return
  const obj = reversed[idx]
  const allObjects = canvas.getObjects()
  const realIdx = allObjects.indexOf(obj)
  canvas.moveObjectTo(obj, realIdx + 1)
  canvas.requestRenderAll()
  refreshLayers()
}

function handleLayerMoveDown(idx: number) {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const reversed = getReversedObjects()
  if (idx >= reversed.length - 1) return
  const obj = reversed[idx]
  const allObjects = canvas.getObjects()
  const realIdx = allObjects.indexOf(obj)
  if (realIdx <= 0) return
  canvas.moveObjectTo(obj, realIdx - 1)
  canvas.requestRenderAll()
  refreshLayers()
}

// ═══════════════════════════════════════════════════════════
//  文本样式
// ═══════════════════════════════════════════════════════════

const isTextboxSelected = computed(() => selectedObject.value?.type === 'textbox')

const textStyle = computed(() => {
  const obj = selectedObject.value as any
  if (!obj || obj.type !== 'textbox') return { fontFamily: 'sans-serif', fontSize: 28, fill: '#1e293b', fontWeight: 'normal', fontStyle: 'normal', underline: false, textAlign: 'left' }
  return {
    fontFamily: obj.fontFamily ?? 'sans-serif',
    fontSize: obj.fontSize ?? 28,
    fill: typeof obj.fill === 'string' ? obj.fill : '#1e293b',
    fontWeight: obj.fontWeight ?? 'normal',
    fontStyle: obj.fontStyle ?? 'normal',
    underline: obj.underline ?? false,
    textAlign: obj.textAlign ?? 'left',
  }
})

function handleTextStyleUpdate(prop: string, value: unknown) {
  const obj = selectedObject.value
  const canvas = fabricCanvas.value
  if (!obj || !canvas || obj.type !== 'textbox') return
  obj.set(prop as keyof typeof obj, value)
  // 改字号后需重新测量 Textbox 排版，否则边界框不更新
  if (prop === 'fontSize' || prop === 'fontFamily' || prop === 'fontWeight') {
    (obj as any).initDimensions?.()
  }
  canvas.requestRenderAll()
  // selectedObject 是 shallowRef，对内部字段变化不响应，手动触发 textStyle computed 重算
  triggerRef(selectedObject)
  // 进历史栈，与其他 modified 操作一致
  canvas.fire('object:modified', { target: obj })
  syncSelectedProps()
}

function handleDeleteSelected() {
  const obj = selectedObject.value
  const canvas = fabricCanvas.value
  if (!obj || !canvas) return
  // 支持多选
  const active = canvas.getActiveObjects()
  if (active.length > 0) {
    active.forEach(o => canvas.remove(o))
  } else {
    canvas.remove(obj)
  }
  canvas.discardActiveObject()
  canvas.requestRenderAll()
  selectedObject.value = null
}

// ═══════════════════════════════════════════════════════════
//  画布缩放
// ═══════════════════════════════════════════════════════════

const MIN_ZOOM = 0.1
const MAX_ZOOM = 3

function zoomBy(delta: number) {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, currentZoom.value + delta))
  applyZoom(newZoom)
}

function applyZoom(zoom: number) {
  const canvas = fabricCanvas.value
  if (!canvas) return
  currentZoom.value = zoom
  canvas.setZoom(zoom)
  canvas.setDimensions({
    width: LOGICAL_WIDTH * zoom,
    height: LOGICAL_HEIGHT * zoom,
  })
  canvas.requestRenderAll()
}

function handleWheel(e: WheelEvent) {
  if (!e.ctrlKey && !e.metaKey) return
  e.preventDefault()
  const delta = e.deltaY > 0 ? -0.05 : 0.05
  zoomBy(delta)
}

// ═══════════════════════════════════════════════════════════
//  元素插入
// ═══════════════════════════════════════════════════════════

// 统一控件外观 + 控制点可见性配置（让四角拖拽更明显，操作更顺手）
function applyDefaultControls<T extends FabricObject>(obj: T, opts?: { lockAspect?: boolean }): T {
  obj.set({
    cornerSize: 14,
    cornerColor: '#3b82f6',
    cornerStrokeColor: '#1d4ed8',
    cornerStyle: 'circle',
    transparentCorners: false,
    borderColor: '#3b82f6',
    borderScaleFactor: 1.5,
    padding: 4,
  })
  // 所有控制点全开（包括 Textbox 的上下中点便于用户拖拽调整宽高）
  obj.setControlsVisibility({
    tl: true, tr: true, bl: true, br: true,
    ml: true, mr: true, mt: true, mb: true,
    mtr: true,
  })
  if (opts?.lockAspect) {
    obj.set({ lockUniScaling: true })
  }
  return obj
}

function placeAndActivate(obj: FabricObject) {
  const canvas = fabricCanvas.value
  if (!canvas) return
  applyDefaultControls(obj)
  canvas.add(obj)
  canvas.setActiveObject(obj)
  canvas.requestRenderAll()
}

function addTextbox() {
  placeAndActivate(new Textbox('请输入文本', {
    left: 200, top: 200, width: 400,
    fontSize: 28, fontFamily: 'sans-serif', fill: '#1e293b',
  }))
}

function addTitle() {
  placeAndActivate(new Textbox('展厅标题', {
    left: 160, top: 120, width: 1000,
    fontSize: 72, fontFamily: 'serif', fontWeight: 'bold',
    fill: '#0f172a', textAlign: 'center',
  }))
}

function addSubtitle() {
  placeAndActivate(new Textbox('副标题或简短描述', {
    left: 200, top: 260, width: 800,
    fontSize: 36, fontFamily: 'sans-serif',
    fill: '#475569', textAlign: 'center',
  }))
}

function addParagraph() {
  placeAndActivate(new Textbox('在此撰写正文段落，介绍文物背景、历史脉络或学习要点。', {
    left: 200, top: 320, width: 720,
    fontSize: 20, fontFamily: 'sans-serif', fill: '#334155',
    lineHeight: 1.6,
  }))
}

function addRect() {
  placeAndActivate(new Rect({
    left: 200, top: 200, width: 400, height: 240,
    rx: 16, ry: 16, fill: '#e2d6cc',
    stroke: '#c5b9ad', strokeWidth: 1,
  }))
}

function addCard() {
  // 一个浅色圆角面板做"卡片"风格容器
  placeAndActivate(new Rect({
    left: 200, top: 200, width: 520, height: 320,
    rx: 20, ry: 20, fill: '#ffffff',
    stroke: '#e2e8f0', strokeWidth: 2,
    shadow: new Shadow({ color: 'rgba(15,23,42,0.12)', blur: 24, offsetX: 0, offsetY: 8 }),
  }))
}

function addQuote() {
  // 引用块：左侧色条 + 文字（用 Group 绑定方便整体移动）
  const bar = new Rect({
    left: 0, top: 0, width: 8, height: 200,
    fill: '#f59e0b', stroke: '', strokeWidth: 0,
    selectable: false, evented: false,
  })
  const text = new Textbox('"在此输入引用内容或重要语录"', {
    left: 28, top: 12, width: 560,
    fontSize: 26, fontFamily: 'serif',
    fill: '#92400e', lineHeight: 1.5,
    fontStyle: 'italic',
    selectable: false, evented: false,
  })
  const group = new Group([bar, text], { left: 200, top: 200 })
  placeAndActivate(group)
}

function addCircle() {
  placeAndActivate(new Circle({
    left: 240, top: 240, radius: 120,
    fill: '#fde68a', stroke: '#f59e0b', strokeWidth: 2,
  }))
}

function addDivider() {
  // 一条水平分割线
  placeAndActivate(new Line([0, 0, 600, 0], {
    left: 200, top: 320,
    stroke: '#94a3b8', strokeWidth: 3,
    strokeLineCap: 'round',
  }))
}

function addBadge() {
  // 圆角徽章：背景胶囊 Rect + 文字（合并 Group）
  const pill = new Rect({
    left: 0, top: 0, width: 180, height: 56,
    rx: 28, ry: 28, fill: '#dbeafe',
    stroke: '#2563eb', strokeWidth: 1.5,
    selectable: false, evented: false,
  })
  const label = new Textbox('标签', {
    left: 0, top: 12, width: 180,
    fontSize: 22, fontFamily: 'sans-serif',
    fill: '#1d4ed8', textAlign: 'center', fontWeight: '600',
    selectable: false, evented: false,
  })
  const group = new Group([pill, label], { left: 240, top: 240 })
  placeAndActivate(group)
}

async function handleInsertAsset(asset: Asset) {
  const canvas = fabricCanvas.value
  if (!canvas) return

  if (asset.assetType === 'image' || asset.mimeType?.startsWith('image/')) {
    await insertImage(canvas, asset.fileUrl, {
      assetType: 'image',
      assetId: asset.assetId,
      assetName: asset.originalFileName || asset.fileName,
      mediaUrl: asset.fileUrl,
    })
  } else {
    insertMediaPlaceholder(canvas, asset.assetType as 'video' | 'audio', {
      assetType: asset.assetType,
      assetId: asset.assetId,
      assetName: asset.originalFileName || asset.fileName,
      mediaUrl: asset.fileUrl,
    })
  }
}

function handleInsertMuseum(resource: MuseumResource) {
  const canvas = fabricCanvas.value
  if (!canvas) return

  if (resource.coverUrl) {
    insertImage(canvas, resource.coverUrl, {
      assetType: 'museum-image',
      assetId: resource.id,
      assetName: resource.title,
      mediaUrl: resource.coverUrl,
    })
  } else {
    const tb = new Textbox(resource.title, {
      left: 200,
      top: 200,
      width: 500,
      fontSize: 32,
      fontFamily: 'serif',
      fill: '#6b190f',
    })
    canvas.add(tb)
    canvas.setActiveObject(tb)
    canvas.requestRenderAll()
  }
}

async function insertImage(
  canvas: Canvas,
  url: string,
  meta: { assetType: string; assetId: number; assetName: string; mediaUrl: string },
) {
  try {
    const img = await FabricImage.fromURL(url, { crossOrigin: 'anonymous' })
    const maxW = LOGICAL_WIDTH * 0.45
    const maxH = LOGICAL_HEIGHT * 0.45
    const scale = Math.min(maxW / (img.width || 1), maxH / (img.height || 1), 1)
    img.set({
      left: 200,
      top: 200,
      scaleX: scale,
      scaleY: scale,
    })
    ;(img as any).assetType = meta.assetType
    ;(img as any).assetId = meta.assetId
    ;(img as any).assetName = meta.assetName
    ;(img as any).mediaUrl = meta.mediaUrl
    canvas.add(img)
    canvas.setActiveObject(img)
    canvas.requestRenderAll()
  } catch {
    appStore.showToast('图片加载失败，可能因为跨域限制', 'error')
  }
}

function insertMediaPlaceholder(
  canvas: Canvas,
  type: 'video' | 'audio',
  meta: { assetType: string; assetId: number; assetName: string; mediaUrl: string },
) {
  const w = 320
  const h = type === 'video' ? 200 : 80
  const bg = new Rect({ width: w, height: h, rx: 12, ry: 12, fill: type === 'video' ? '#1e293b' : '#312e81', originX: 'center', originY: 'center' })
  const icon = new Textbox(type === 'video' ? '▶' : '♫', {
    width: 60,
    fontSize: 36,
    fill: '#ffffff',
    textAlign: 'center',
    originX: 'center',
    originY: 'center',
    top: -10,
    selectable: false,
    editable: false,
  })
  const label = new Textbox(meta.assetName || (type === 'video' ? '视频' : '音频'), {
    width: w - 32,
    fontSize: 14,
    fill: '#94a3b8',
    textAlign: 'center',
    originX: 'center',
    originY: 'center',
    top: 24,
    selectable: false,
    editable: false,
  })
  const group = new Group([bg, icon, label], { left: 200, top: 200 })
  ;(group as any).assetType = meta.assetType
  ;(group as any).assetId = meta.assetId
  ;(group as any).assetName = meta.assetName
  ;(group as any).mediaUrl = meta.mediaUrl
  canvas.add(group)
  canvas.setActiveObject(group)
  canvas.requestRenderAll()
}

// ═══════════════════════════════════════════════════════════
//  保存 / 发布 (editor-bundle with revision)
// ═══════════════════════════════════════════════════════════

function buildCanvasDataMap(): Record<string, Record<string, unknown>> {
  const canvas = fabricCanvas.value
  const map: Record<string, Record<string, unknown>> = {}
  if (currentZone.value && canvas) {
    const cleanJson = getCanvasJsonClean()
    if (cleanJson) canvasDataCache.set(currentZone.value.id, cleanJson)
  }
  for (const zone of zones.value) {
    const data = canvasDataCache.get(zone.id)
    if (data) {
      map[zone.zoneCode] = data
    }
  }
  return map
}

async function handleSave() {
  saving.value = true
  try {
    // 先 flush 属性面板待决补丁，确保保存前所有属性已下发
    await flushAllPendingPatches()
    const canvasDataMap = buildCanvasDataMap()
    const result = await saveEditorBundle(exhibitionId, {
      revision: bundleRevision.value,
      canvasDataMap,
    })
    bundleRevision.value = result.revision
    conflictDetected.value = false
    autosave.clearDirty()
    appStore.showToast('保存成功', 'success')
  } catch (error: any) {
    if (error?.response?.status === 409) {
      conflictDetected.value = true
      appStore.showToast('展厅内容已被其他成员更新，请刷新后重试', 'error')
    } else {
      appStore.showToast(getErrorMessage(error, '保存失败'), 'error')
    }
  } finally {
    saving.value = false
  }
}

async function handlePublish() {
  if (!bundle.value?.exhibition.latestVersionNo) {
    appStore.showToast('请先保存一个版本再发布', 'error')
    return
  }
  publishing.value = true
  try {
    await publishExhibition(exhibitionId, {
      versionNo: bundle.value.exhibition.latestVersionNo,
      visibility: (bundle.value.exhibition.visibility as 'private' | 'class' | 'public') || 'class',
    })
    appStore.showToast('展厅已发布', 'success')
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '发布失败'), 'error')
  } finally {
    publishing.value = false
  }
}

async function handleSubmitForReview(remark: string) {
  const taskId = bundle.value?.exhibition.taskId
  if (!taskId) {
    appStore.showToast('当前展厅未关联任务，无法提交', 'error')
    return
  }
  if (!bundle.value?.exhibition.latestVersionNo) {
    appStore.showToast('请先保存一个版本再提交审核', 'error')
    return
  }
  submittingForReview.value = true
  try {
    // 先 flush 自动保存和属性面板补丁，再调用提交
    await Promise.all([
      autosave.flush(),
      flushAllPendingPatches(),
    ])
    await submitTaskWork(taskId, {
      exhibitionId,
      submitRemark: remark || null,
    })
    showSubmitDialog.value = false
    appStore.showToast('已提交审核，等待教师评分', 'success')
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '提交审核失败'), 'error')
  } finally {
    submittingForReview.value = false
  }
}

// ═══════════════════════════════════════════════════════════
//  数据加载
// ═══════════════════════════════════════════════════════════

async function loadBundle() {
  try {
    const data = await getEditorBundle(exhibitionId)
    bundle.value = data
    bundleRevision.value = data.revision
    allExhibits.value = data.exhibits
    allHotspots.value = data.hotspots
    digitalHumans.value = data.digitalHumans ?? []
    zonePlacements.value = data.zoneDigitalHumans ?? []
    zm.setZones(data.zones)

    for (const zone of data.zones) {
      if (zone.canvasData && typeof zone.canvasData === 'object' && 'objects' in zone.canvasData) {
        canvasDataCache.set(zone.id, zone.canvasData)
      }
    }
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '展厅数据加载失败'), 'error')
  }
}

async function restoreCurrentZone() {
  const zone = currentZone.value
  if (!zone) return
  currentBackgroundUrl.value = zone.backgroundUrl ?? null
  // currentHotspots is computed; auto-updates from allHotspots
  const canvas = fabricCanvas.value
  if (!canvas) return
  const data = canvasDataCache.get(zone.id) ?? zone.canvasData
  if (data && typeof data === 'object' && 'objects' in data) {
    await canvas.loadFromJSON(data)
  }
  await syncExhibitAnchors()
  fitCanvasToContainer()
  canvas.requestRenderAll()
  history.reset()
}

// ═══════════════════════════════════════════════════════════
//  生命周期
// ═══════════════════════════════════════════════════════════

onMounted(async () => {
  await loadBundle()
  initCanvas()
  await restoreCurrentZone()
})

function refreshLayers() {
  layerVersion.value++
}

onBeforeUnmount(() => {
  // 离页前 flush 所有待决补丁（best-effort，不阻塞）
  void flushAllPendingPatches()
  canvasWrapper.value?.removeEventListener('wheel', handleWheel)
  alignGuides.unbind()
  shortcuts.unbind()
  history.unbindCanvasEvents()
  autosave.destroy()
  if (resizeObserver) resizeObserver.disconnect()
  fabricCanvas.value?.dispose()
})
</script>

<style scoped>
.toolbar-btn {
  @apply rounded-md px-2.5 py-1.5 text-base text-gray-600 transition hover:bg-gray-100 disabled:cursor-not-allowed disabled:text-gray-300;
}
.component-btn {
  @apply flex flex-col items-center justify-center gap-1 rounded-md border border-gray-200 px-2 py-2.5 text-gray-600 transition hover:border-brand-300 hover:bg-brand-50 hover:text-brand-700;
}
.component-icon {
  @apply text-lg leading-none text-gray-500;
}
.component-label {
  @apply text-[11px] leading-none text-gray-500;
}
</style>
