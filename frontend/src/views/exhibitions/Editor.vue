<template>
  <div class="flex h-screen flex-col bg-neutral-50 text-gray-900">
    <!-- ═══ 顶栏 ═══ -->
    <header class="flex shrink-0 items-center justify-between border-b border-gray-200 bg-white px-4 py-2.5">
      <div class="flex items-center gap-3">
        <button type="button" class="rounded-md px-3 py-1.5 text-sm text-gray-500 transition hover:bg-gray-100" @click="router.back()">← 返回</button>
        <h1 class="text-sm font-semibold text-gray-800">{{ detail?.title || '展厅编辑器' }}</h1>
      </div>

      <div class="flex items-center gap-2">
        <!-- 自动保存状态 -->
        <span v-if="autosaving" class="text-xs text-amber-500">保存中...</span>
        <span v-else-if="autosaveError" class="text-xs text-rose-500" :title="autosaveError">保存失败</span>
        <span v-else-if="lastAutosaveAt" class="text-xs text-gray-400">已自动保存 {{ autosaveTimeAgo }}</span>

        <!-- Undo / Redo -->
        <button type="button" :disabled="!canUndo" class="toolbar-btn" title="撤销 (Ctrl+Z)" @click="undo">↶</button>
        <button type="button" :disabled="!canRedo" class="toolbar-btn" title="重做 (Ctrl+Y)" @click="redo">↷</button>

        <div class="mx-1 h-5 w-px bg-gray-200" />

        <!-- 保存 / 发布 -->
        <button type="button" :disabled="saving" class="rounded-md bg-brand-600 px-4 py-1.5 text-sm font-medium text-white transition hover:bg-brand-700 disabled:bg-gray-300" @click="handleSave">
          {{ saving ? '保存中...' : '保存草稿' }}
        </button>
        <button type="button" :disabled="publishing" class="rounded-md border border-brand-200 px-4 py-1.5 text-sm font-medium text-brand-700 transition hover:bg-brand-50 disabled:border-gray-200 disabled:text-gray-400" @click="handlePublish">
          {{ publishing ? '发布中...' : '发布' }}
        </button>
      </div>
    </header>

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
          <!-- 组件库 -->
          <template v-if="activeLeftTab === 'components'">
            <div class="space-y-2">
              <button type="button" class="component-btn" @click="addTextbox">
                <span class="text-lg">T</span>
                <span class="text-xs">文本框</span>
              </button>
              <button type="button" class="component-btn" @click="addRect">
                <span class="text-lg">▬</span>
                <span class="text-xs">面板</span>
              </button>
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

          <!-- 模板 -->
          <template v-else-if="activeLeftTab === 'templates'">
            <TemplatePicker @apply="handleApplyTemplate" />
          </template>
        </div>
      </aside>

      <!-- ═══ 画布区 ═══ -->
      <main class="relative flex min-w-0 flex-1 flex-col bg-neutral-100">
        <div class="flex shrink-0 items-center justify-between border-b border-gray-200 bg-white px-4 py-1.5">
          <span class="text-xs text-gray-400">{{ LOGICAL_WIDTH }} × {{ LOGICAL_HEIGHT }}</span>
          <div class="flex items-center gap-1">
            <button type="button" class="toolbar-btn text-xs" title="缩小" @click="zoomBy(-0.1)">−</button>
            <span class="w-12 text-center text-xs text-gray-400">{{ Math.round(currentZoom * 100) }}%</span>
            <button type="button" class="toolbar-btn text-xs" title="放大" @click="zoomBy(0.1)">+</button>
            <button type="button" class="toolbar-btn text-xs" title="适应窗口" @click="fitCanvasToContainer()">⊡</button>
          </div>
        </div>
        <div ref="canvasWrapper" class="flex flex-1 items-center justify-center overflow-hidden p-4">
          <canvas ref="canvasEl" />
        </div>
      </main>

      <!-- ═══ 右侧栏：属性面板 ═══ -->
      <aside class="w-64 shrink-0 overflow-y-auto border-l border-gray-200 bg-white p-4">
        <h3 class="mb-4 text-xs font-semibold uppercase tracking-widest text-gray-400">属性</h3>
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
          </div>
        </template>
        <p v-else class="text-xs text-gray-400">选中画布元素后可编辑属性。</p>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Canvas, Rect, Textbox, FabricImage, Group, type FabricObject } from 'fabric'
import {
  getExhibitionDetail,
  getExhibitionVersions,
  publishExhibition,
  saveExhibitionVersion,
} from '@/api/modules/exhibitions'
import type {
  Asset,
  ExhibitionDetail,
  ExhibitionVersion,
  MuseumResource,
  SaveExhibitionVersionRequest,
} from '@/api/types'
import { getErrorMessage } from '@/utils/request'
import { useAppStore } from '@/stores/app'
import { useCanvasHistory } from '@/composables/useCanvasHistory'
import { useCanvasAutosave } from '@/composables/useCanvasAutosave'
import { useCanvasShortcuts } from '@/composables/useCanvasShortcuts'
import AssetPicker from '@/components/exhibitions/editor/AssetPicker.vue'
import LayerPanel from '@/components/exhibitions/editor/LayerPanel.vue'
import TextStylePanel from '@/components/exhibitions/editor/TextStylePanel.vue'
import type { LayerItem } from '@/components/exhibitions/editor/LayerPanel.vue'
import TemplatePicker from '@/components/exhibitions/editor/TemplatePicker.vue'
import { useAlignmentGuides } from '@/composables/useAlignmentGuides'

// ─── 常量 ───
const LOGICAL_WIDTH = 1920
const LOGICAL_HEIGHT = 1080
const CUSTOM_PROPS = ['assetType', 'assetId', 'mediaUrl', 'assetName']

// ─── 路由 & Store ───
const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const exhibitionId = Number(route.params.exhibitionId)

// ─── 基础状态 ───
const detail = ref<ExhibitionDetail | null>(null)
const versions = ref<ExhibitionVersion[]>([])
const saving = ref(false)
const publishing = ref(false)
const currentZoom = ref(1)

// ─── 左侧栏 ───
const leftTabs = [
  { label: '组件库', value: 'components' as const },
  { label: '素材库', value: 'assets' as const },
  { label: '图层', value: 'layers' as const },
  { label: '模板', value: 'templates' as const },
]
const activeLeftTab = ref<'components' | 'assets' | 'layers' | 'templates'>('components')

// ─── 画布 ───
const canvasEl = ref<HTMLCanvasElement | null>(null)
const canvasWrapper = ref<HTMLElement | null>(null)
const fabricCanvas = shallowRef<Canvas | null>(null)
let resizeObserver: ResizeObserver | null = null

// ─── 选中元素属性 ───
const selectedObject = shallowRef<FabricObject | null>(null)
const selectedProps = reactive({ x: 0, y: 0, w: 0, h: 0 })
const propertyFields = [
  { key: 'x' as const, label: 'X 坐标' },
  { key: 'y' as const, label: 'Y 坐标' },
  { key: 'w' as const, label: '宽度' },
  { key: 'h' as const, label: '高度' },
]

// ─── Composables ───
const history = useCanvasHistory(fabricCanvas)
const { canUndo, canRedo, undo, redo } = history

const autosave = useCanvasAutosave(
  fabricCanvas,
  () => exhibitionId,
  () => buildSavePayload(),
)
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
})

const alignGuides = useAlignmentGuides(fabricCanvas, LOGICAL_WIDTH, LOGICAL_HEIGHT)

// ═══════════════════════════════════════════════════════════
//  画布初始化 & 坐标系
// ═══════════════════════════════════════════════════════════

function initCanvas() {
  if (!canvasEl.value) return
  const canvas = new Canvas(canvasEl.value, {
    width: LOGICAL_WIDTH,
    height: LOGICAL_HEIGHT,
    backgroundColor: '#f7efe9',
    selection: true,
  })
  fabricCanvas.value = canvas

  canvas.on('selection:created', syncSelectedProps)
  canvas.on('selection:updated', syncSelectedProps)
  canvas.on('selection:cleared', () => { selectedObject.value = null })
  canvas.on('object:modified', syncSelectedProps)
  canvas.on('object:added', refreshLayers)
  canvas.on('object:removed', refreshLayers)

  resizeObserver = new ResizeObserver(() => fitCanvasToContainer())
  if (canvasWrapper.value) resizeObserver.observe(canvasWrapper.value)

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
  const canvas = fabricCanvas.value
  if (!canvas) return []
  const objects = canvas.getObjects()
  const activeObj = canvas.getActiveObject()
  return objects.map((obj) => {
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

function handleLayerSelect(idx: number) {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const obj = canvas.getObjects()[idx]
  if (obj) {
    canvas.setActiveObject(obj)
    canvas.requestRenderAll()
  }
}

function handleLayerToggleVisible(idx: number) {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const obj = canvas.getObjects()[idx]
  if (obj) {
    obj.set('visible', !obj.visible)
    canvas.requestRenderAll()
  }
}

function handleLayerMoveUp(idx: number) {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const objects = canvas.getObjects()
  if (idx <= 0) return
  const obj = objects[idx]
  canvas.moveObjectTo(obj, idx - 1)
  canvas.requestRenderAll()
}

function handleLayerMoveDown(idx: number) {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const objects = canvas.getObjects()
  if (idx >= objects.length - 1) return
  const obj = objects[idx]
  canvas.moveObjectTo(obj, idx + 1)
  canvas.requestRenderAll()
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
  canvas.requestRenderAll()
  syncSelectedProps()
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
//  组件模板
// ═══════════════════════════════════════════════════════════

function handleApplyTemplate(templateId: string) {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const W = LOGICAL_WIDTH
  const H = LOGICAL_HEIGHT

  const templates: Record<string, () => void> = {
    'title-page': () => {
      canvas.add(new Rect({ left: 0, top: 0, width: W, height: H, fill: '#1e293b', selectable: false }))
      canvas.add(new Textbox('展厅标题', { left: W / 2 - 300, top: H * 0.32, width: 600, fontSize: 72, fontFamily: 'serif', fill: '#ffffff', textAlign: 'center' }))
      canvas.add(new Textbox('副标题或简介文字', { left: W / 2 - 250, top: H * 0.52, width: 500, fontSize: 28, fontFamily: 'sans-serif', fill: '#94a3b8', textAlign: 'center' }))
    },
    'image-text': () => {
      canvas.add(new Rect({ left: 60, top: 120, width: 800, height: 600, rx: 20, ry: 20, fill: '#e2d6cc', stroke: '#c5b9ad', strokeWidth: 1 }))
      canvas.add(new Textbox('在此添加标题', { left: 940, top: 180, width: 860, fontSize: 36, fontFamily: 'serif', fill: '#1e293b' }))
      canvas.add(new Textbox('在此添加段落内容，描述图片或文物的背景故事。', { left: 940, top: 260, width: 860, fontSize: 20, fontFamily: 'sans-serif', fill: '#64748b', lineHeight: 1.6 }))
    },
    'dual-panel': () => {
      canvas.add(new Rect({ left: 60, top: 120, width: 880, height: 700, rx: 20, ry: 20, fill: '#f1ebe5', stroke: '#d6cec6', strokeWidth: 1 }))
      canvas.add(new Rect({ left: 980, top: 120, width: 880, height: 700, rx: 20, ry: 20, fill: '#f1ebe5', stroke: '#d6cec6', strokeWidth: 1 }))
      canvas.add(new Textbox('左侧面板标题', { left: 120, top: 200, width: 760, fontSize: 28, fontFamily: 'serif', fill: '#1e293b', textAlign: 'center' }))
      canvas.add(new Textbox('右侧面板标题', { left: 1040, top: 200, width: 760, fontSize: 28, fontFamily: 'serif', fill: '#1e293b', textAlign: 'center' }))
    },
    'quote-block': () => {
      canvas.add(new Rect({ left: 200, top: 200, width: W - 400, height: 500, rx: 24, ry: 24, fill: '#fef3c7', stroke: '#fcd34d', strokeWidth: 2 }))
      canvas.add(new Textbox('"', { left: 240, top: 220, width: 100, fontSize: 120, fontFamily: 'serif', fill: '#d97706' }))
      canvas.add(new Textbox('在此输入引用的名言或重要文段', { left: 300, top: 340, width: W - 600, fontSize: 32, fontFamily: 'serif', fill: '#92400e', lineHeight: 1.5 }))
      canvas.add(new Textbox('— 出处', { left: 300, top: 540, width: W - 600, fontSize: 20, fontFamily: 'sans-serif', fill: '#b45309', textAlign: 'right' }))
    },
    'gallery-grid': () => {
      const gw = 560; const gh = 380; const gap = 40
      const startX = (W - gw * 2 - gap) / 2; const startY = (H - gh * 2 - gap) / 2
      for (let row = 0; row < 2; row++) {
        for (let col = 0; col < 2; col++) {
          canvas.add(new Rect({
            left: startX + col * (gw + gap),
            top: startY + row * (gh + gap),
            width: gw, height: gh, rx: 16, ry: 16, fill: '#e2d6cc', stroke: '#c5b9ad', strokeWidth: 1,
          }))
        }
      }
    },
  }

  const apply = templates[templateId]
  if (apply) {
    apply()
    canvas.requestRenderAll()
  }
}

// ═══════════════════════════════════════════════════════════
//  元素插入
// ═══════════════════════════════════════════════════════════

function addTextbox() {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const tb = new Textbox('请输入文本', {
    left: 200,
    top: 200,
    width: 400,
    fontSize: 28,
    fontFamily: 'sans-serif',
    fill: '#1e293b',
  })
  canvas.add(tb)
  canvas.setActiveObject(tb)
  canvas.requestRenderAll()
}

function addRect() {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const rect = new Rect({
    left: 200,
    top: 200,
    width: 400,
    height: 240,
    rx: 16,
    ry: 16,
    fill: '#e2d6cc',
    stroke: '#c5b9ad',
    strokeWidth: 1,
  })
  canvas.add(rect)
  canvas.setActiveObject(rect)
  canvas.requestRenderAll()
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
//  保存 / 发布 / 恢复
// ═══════════════════════════════════════════════════════════

function buildSavePayload(): SaveExhibitionVersionRequest {
  const canvas = fabricCanvas.value
  return {
    saveType: 'manual',
    versionNote: '',
    canvasConfig: {
      width: LOGICAL_WIDTH,
      height: LOGICAL_HEIGHT,
      background: '#f7efe9',
      zoom: 1,
    },
    versionData: canvas
      ? (canvas as any).toJSON(CUSTOM_PROPS)
      : {},
  }
}

async function handleSave() {
  saving.value = true
  try {
    const payload = buildSavePayload()
    payload.versionNote = '手动保存'
    const version = await saveExhibitionVersion(exhibitionId, payload)
    appStore.showToast(`版本 V${version.versionNo} 已保存`, 'success')
    await loadVersions()
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '保存失败'), 'error')
  } finally {
    saving.value = false
  }
}

async function handlePublish() {
  if (!detail.value?.latestVersionNo) {
    appStore.showToast('请先保存一个版本再发布', 'error')
    return
  }
  publishing.value = true
  try {
    await publishExhibition(exhibitionId, {
      versionNo: detail.value.latestVersionNo,
      visibility: (detail.value.visibility as 'private' | 'class' | 'public') || 'class',
    })
    appStore.showToast('展厅已发布', 'success')
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '发布失败'), 'error')
  } finally {
    publishing.value = false
  }
}

async function restoreLatestVersion() {
  const canvas = fabricCanvas.value
  if (!canvas || versions.value.length === 0) return
  const latest = versions.value[0]
  if (!latest.versionData) return

  autosave.pauseAutosave()
  try {
    const vd = latest.versionData as Record<string, unknown>
    const savedW = latest.canvasConfig?.width || LOGICAL_WIDTH
    const savedH = latest.canvasConfig?.height || LOGICAL_HEIGHT

    await canvas.loadFromJSON(vd)

    // 旧版数据兼容：如果存储时的逻辑尺寸和当前不同，按比例缩放所有对象
    if (savedW !== LOGICAL_WIDTH || savedH !== LOGICAL_HEIGHT) {
      const scaleX = LOGICAL_WIDTH / savedW
      const scaleY = LOGICAL_HEIGHT / savedH
      canvas.getObjects().forEach((obj) => {
        obj.set({
          left: (obj.left ?? 0) * scaleX,
          top: (obj.top ?? 0) * scaleY,
          scaleX: (obj.scaleX ?? 1) * scaleX,
          scaleY: (obj.scaleY ?? 1) * scaleY,
        })
        obj.setCoords()
      })
    }

    fitCanvasToContainer()
    canvas.requestRenderAll()
    history.reset()
  } catch {
    appStore.showToast('版本恢复失败', 'error')
  } finally {
    autosave.resumeAutosave()
  }
}

// ═══════════════════════════════════════════════════════════
//  数据加载
// ═══════════════════════════════════════════════════════════

async function loadDetail() {
  try {
    detail.value = await getExhibitionDetail(exhibitionId)
  } catch {
    appStore.showToast('展厅信息加载失败', 'error')
  }
}

async function loadVersions() {
  try {
    versions.value = await getExhibitionVersions(exhibitionId)
  } catch {
    versions.value = []
  }
}

// ═══════════════════════════════════════════════════════════
//  生命周期
// ═══════════════════════════════════════════════════════════

onMounted(async () => {
  await Promise.all([loadDetail(), loadVersions()])
  initCanvas()
  await restoreLatestVersion()
})

function refreshLayers() {
  // trigger layerList recomputation by touching the canvas ref
  const c = fabricCanvas.value
  if (c) fabricCanvas.value = c
}

onBeforeUnmount(() => {
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
  @apply flex w-full items-center gap-3 rounded-md border border-gray-200 px-3 py-2.5 text-left transition hover:border-gray-300 hover:bg-gray-50;
}
</style>
