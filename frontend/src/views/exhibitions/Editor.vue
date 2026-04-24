<template>
  <div class="flex h-screen flex-col bg-neutral-50 text-gray-900">
    <header class="flex h-14 shrink-0 items-center justify-between border-b border-gray-200 bg-white px-4">
      <div class="flex min-w-0 items-center gap-3">
        <button
          type="button"
          class="rounded-md border border-gray-200 bg-white px-3 py-1.5 text-sm text-gray-700 transition hover:border-gray-300 hover:text-gray-900"
          @click="handleBack"
        >
          返回
        </button>
        <div class="min-w-0">
          <h1 class="truncate text-sm font-medium text-gray-900">{{ exhibitionTitle }}</h1>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <span v-if="statusMessage" class="hidden text-xs text-gray-500 md:block">{{ statusMessage }}</span>
        <button
          type="button"
          :disabled="saving"
          class="rounded-md border border-gray-200 bg-white px-3 py-1.5 text-sm text-gray-700 transition hover:border-gray-300 hover:text-gray-900 disabled:cursor-not-allowed disabled:text-gray-400"
          @click="handleSave"
        >
          {{ saving ? '保存中...' : '存草稿' }}
        </button>
        <button
          type="button"
          :disabled="publishing"
          class="rounded-md bg-blue-600 px-3 py-1.5 text-sm text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-400"
          @click="handlePublish"
        >
          {{ publishing ? '发布中...' : '发布' }}
        </button>
      </div>
    </header>

    <div class="flex min-h-0 flex-1">
      <aside class="flex h-full w-64 shrink-0 flex-col border-r border-gray-200 bg-white">
        <div class="grid grid-cols-2 border-b border-gray-200">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            type="button"
            class="border-b px-4 py-3 text-sm transition"
            :class="activeTab === tab.value ? 'border-gray-900 text-gray-900' : 'border-transparent text-gray-500 hover:text-gray-900'"
            @click="activeTab = tab.value"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="min-h-0 flex-1 overflow-y-auto p-4">
          <section v-if="activeTab === 'components'" class="space-y-3">
            <div class="space-y-1">
              <h2 class="text-xs font-medium uppercase tracking-[0.18em] text-gray-500">组件库</h2>
              <p class="text-sm text-gray-600">拖拽前先插入基础画布元素。</p>
            </div>

            <button
              type="button"
              class="flex w-full items-center justify-between rounded-md border border-gray-200 bg-white px-3 py-3 text-left transition hover:border-gray-300"
              @click="addTextElement"
            >
              <span>
                <span class="block text-sm font-medium text-gray-900">文本</span>
                <span class="mt-1 block text-xs text-gray-500">插入一段可编辑文字</span>
              </span>
              <span class="text-xs text-gray-400">T</span>
            </button>

            <button
              type="button"
              class="flex w-full items-center justify-between rounded-md border border-gray-200 bg-white px-3 py-3 text-left transition hover:border-gray-300"
              @click="addPanelElement"
            >
              <span>
                <span class="block text-sm font-medium text-gray-900">信息面板</span>
                <span class="mt-1 block text-xs text-gray-500">插入基础矩形容器</span>
              </span>
              <span class="text-xs text-gray-400">□</span>
            </button>
          </section>

          <section v-else class="space-y-3">
            <div class="space-y-1">
              <h2 class="text-xs font-medium uppercase tracking-[0.18em] text-gray-500">博物馆素材</h2>
              <p class="text-sm text-gray-600">将资源标题作为画布素材块插入。</p>
            </div>

            <p v-if="materialLoading" class="text-sm text-gray-500">素材加载中...</p>
            <p v-else-if="materialError" class="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">
              {{ materialError }}
            </p>
            <p v-else-if="museumResources.length === 0" class="rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-500">
              当前没有可用素材。
            </p>

            <button
              v-for="resource in museumResources"
              :key="resource.id"
              type="button"
              class="w-full rounded-md border border-gray-200 bg-white px-3 py-3 text-left transition hover:border-gray-300"
              @click="insertMuseumResource(resource)"
            >
              <span class="block text-sm font-medium text-gray-900">{{ resource.title }}</span>
              <span class="mt-1 block text-xs text-gray-500">{{ resource.museumName || resource.category }}</span>
            </button>
          </section>
        </div>
      </aside>

      <main class="flex min-w-0 flex-1 flex-col">
        <div class="border-b border-gray-200 bg-white px-4 py-2 text-xs text-gray-500">
          画布尺寸 {{ canvasMetrics.width }} × {{ canvasMetrics.height }}
        </div>
        <div class="min-h-0 flex-1 bg-gray-100 p-4">
          <div ref="canvasContainerRef" class="h-full w-full overflow-hidden border border-gray-200 bg-white">
            <canvas id="exhibition-canvas" ref="canvasRef"></canvas>
          </div>
        </div>
      </main>

      <aside class="flex h-full w-72 shrink-0 flex-col border-l border-gray-200 bg-white">
        <div class="border-b border-gray-200 px-4 py-3">
          <h2 class="text-sm font-medium text-gray-900">属性</h2>
        </div>

        <div class="min-h-0 flex-1 overflow-y-auto p-4">
          <div v-if="activeObject" class="space-y-4">
            <div class="grid grid-cols-2 gap-3">
              <label class="block">
                <span class="form-label">X</span>
                <input
                  v-model.number="activeObjectMetrics.x"
                  type="number"
                  class="form-control"
                  @change="applyActiveObjectMetrics"
                />
              </label>
              <label class="block">
                <span class="form-label">Y</span>
                <input
                  v-model.number="activeObjectMetrics.y"
                  type="number"
                  class="form-control"
                  @change="applyActiveObjectMetrics"
                />
              </label>
              <label class="block">
                <span class="form-label">宽</span>
                <input
                  v-model.number="activeObjectMetrics.width"
                  type="number"
                  min="1"
                  class="form-control"
                  @change="applyActiveObjectMetrics"
                />
              </label>
              <label class="block">
                <span class="form-label">高</span>
                <input
                  v-model.number="activeObjectMetrics.height"
                  type="number"
                  min="1"
                  class="form-control"
                  @change="applyActiveObjectMetrics"
                />
              </label>
            </div>

            <div class="rounded-md border border-gray-200 bg-gray-50 px-3 py-3 text-sm text-gray-600">
              <p>类型：{{ activeObject.type || 'object' }}</p>
            </div>
          </div>

          <div v-else class="rounded-md border border-dashed border-gray-200 bg-gray-50 px-4 py-4 text-sm text-gray-500">
            请在画布中选中一个元素。
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Canvas, Rect, Textbox, type FabricObject } from 'fabric'
import { getExhibitionDetail, getExhibitionVersions, publishExhibition, saveExhibitionVersion } from '@/api/modules/exhibitions'
import { getMuseumResources } from '@/api/modules/museum'
import type { ExhibitionDetail, ExhibitionVersion, MuseumResource, SaveExhibitionVersionRequest } from '@/api/types'
import { useAppStore } from '@/stores/app'
import { getErrorMessage } from '@/utils/request'

type EditorTab = 'components' | 'museum'
type VersionDataPayload = Record<string, unknown> | string

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const tabs: Array<{ label: string; value: EditorTab }> = [
  { label: '组件库', value: 'components' },
  { label: '博物馆素材', value: 'museum' },
]

const exhibitionId = computed(() => Number(route.params.exhibitionId))
const exhibitionDetail = ref<ExhibitionDetail | null>(null)
const exhibitionTitle = computed(() => exhibitionDetail.value?.title || '展厅编辑器')

const activeTab = ref<EditorTab>('components')
const saving = ref(false)
const publishing = ref(false)
const statusMessage = ref('')

const materialLoading = ref(false)
const materialError = ref('')
const museumResources = ref<MuseumResource[]>([])

const canvasRef = ref<HTMLCanvasElement | null>(null)
const canvasContainerRef = ref<HTMLElement | null>(null)
const fabricCanvas = shallowRef<Canvas | null>(null)
const activeObject = shallowRef<FabricObject | null>(null)

const canvasMetrics = reactive({
  width: 0,
  height: 0,
})

const activeObjectMetrics = reactive({
  x: 0,
  y: 0,
  width: 0,
  height: 0,
})

let resizeObserver: ResizeObserver | null = null

onMounted(async () => {
  initCanvas()
  await Promise.all([fetchExhibitionDetail(), fetchMuseumResources(), restoreLatestVersion()])
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  resizeObserver = null
  if (fabricCanvas.value) {
    void fabricCanvas.value.dispose()
    fabricCanvas.value = null
  }
})

function initCanvas() {
  if (!canvasRef.value || !canvasContainerRef.value) {
    return
  }

  const canvas = new Canvas(canvasRef.value, {
    backgroundColor: '#ffffff',
    preserveObjectStacking: true,
    selection: true,
  })

  fabricCanvas.value = canvas
  resizeCanvas()

  canvas.on('selection:created', syncSelectionState)
  canvas.on('selection:updated', syncSelectionState)
  canvas.on('selection:cleared', clearSelectionState)
  canvas.on('object:moving', syncSelectionState)
  canvas.on('object:scaling', syncSelectionState)
  canvas.on('object:modified', syncSelectionState)

  resizeObserver = new ResizeObserver(() => {
    resizeCanvas()
  })
  resizeObserver.observe(canvasContainerRef.value)
}

function resizeCanvas() {
  if (!fabricCanvas.value || !canvasContainerRef.value) {
    return
  }

  const width = Math.max(320, Math.floor(canvasContainerRef.value.clientWidth))
  const height = Math.max(320, Math.floor(canvasContainerRef.value.clientHeight))

  canvasMetrics.width = width
  canvasMetrics.height = height

  fabricCanvas.value.setDimensions({ width, height })
  fabricCanvas.value.requestRenderAll()
}

async function fetchExhibitionDetail() {
  try {
    exhibitionDetail.value = await getExhibitionDetail(exhibitionId.value)
  } catch (error) {
    statusMessage.value = getErrorMessage(error, '展厅信息加载失败')
  }
}

async function fetchMuseumResources() {
  materialLoading.value = true
  materialError.value = ''

  try {
    const page = await getMuseumResources({ page: 1, pageSize: 12 })
    museumResources.value = page.list
  } catch (error) {
    materialError.value = getErrorMessage(error, '博物馆素材加载失败')
  } finally {
    materialLoading.value = false
  }
}

async function restoreLatestVersion() {
  try {
    const versions = await getExhibitionVersions(exhibitionId.value)
    const latestVersion = pickLatestVersion(versions)

    if (latestVersion?.versionData) {
      await loadVersionData(latestVersion.versionData)
      appStore.showToast(`已载入版本 V${latestVersion.versionNo}`, 'info')
    } else if (fabricCanvas.value) {
      addWelcomePlaceholder()
    }
  } catch (error) {
    statusMessage.value = getErrorMessage(error, '历史版本加载失败')
    addWelcomePlaceholder()
  }
}

function pickLatestVersion(versions: ExhibitionVersion[]) {
  return versions.reduce<ExhibitionVersion | null>((latest, current) => {
    if (!latest || current.versionNo > latest.versionNo) {
      return current
    }
    return latest
  }, null)
}

function addWelcomePlaceholder() {
  if (!fabricCanvas.value || fabricCanvas.value.getObjects().length > 0) {
    return
  }

  const placeholder = new Textbox('在左侧插入组件或文博素材', {
    left: 80,
    top: 72,
    width: 320,
    fontSize: 28,
    fill: '#111827',
    editable: true,
  })

  fabricCanvas.value.add(placeholder)
  fabricCanvas.value.setActiveObject(placeholder)
  fabricCanvas.value.requestRenderAll()
  syncSelectionState()
}

async function loadVersionData(versionData: VersionDataPayload) {
  if (!fabricCanvas.value) {
    return
  }

  await fabricCanvas.value.loadFromJSON(versionData)
  fabricCanvas.value.requestRenderAll()
  syncSelectionState()
}

function addTextElement() {
  if (!fabricCanvas.value) {
    return
  }

  const text = new Textbox('请输入展陈说明', {
    left: 80,
    top: 80,
    width: 240,
    fontSize: 22,
    fill: '#111827',
  })

  fabricCanvas.value.add(text)
  fabricCanvas.value.setActiveObject(text)
  fabricCanvas.value.requestRenderAll()
  syncSelectionState()
}

function addPanelElement() {
  if (!fabricCanvas.value) {
    return
  }

  const rect = new Rect({
    left: 96,
    top: 96,
    width: 260,
    height: 160,
    fill: '#ffffff',
    stroke: '#d1d5db',
    strokeWidth: 1,
  })

  fabricCanvas.value.add(rect)
  fabricCanvas.value.setActiveObject(rect)
  fabricCanvas.value.requestRenderAll()
  syncSelectionState()
}

function insertMuseumResource(resource: MuseumResource) {
  if (!fabricCanvas.value) {
    return
  }

  const resourceBlock = new Textbox(resource.title, {
    left: 120,
    top: 120,
    width: 260,
    fontSize: 20,
    fill: '#111827',
    backgroundColor: '#ffffff',
    editable: true,
  })

  fabricCanvas.value.add(resourceBlock)
  fabricCanvas.value.setActiveObject(resourceBlock)
  fabricCanvas.value.requestRenderAll()
  syncSelectionState()
}

function syncSelectionState() {
  const selected = fabricCanvas.value?.getActiveObject() ?? null
  activeObject.value = selected
  syncActiveObjectMetrics(selected)
}

function clearSelectionState() {
  activeObject.value = null
  syncActiveObjectMetrics(null)
}

function syncActiveObjectMetrics(target: FabricObject | null = activeObject.value) {
  if (!target) {
    activeObjectMetrics.x = 0
    activeObjectMetrics.y = 0
    activeObjectMetrics.width = 0
    activeObjectMetrics.height = 0
    return
  }

  activeObjectMetrics.x = roundNumber(target.left ?? 0)
  activeObjectMetrics.y = roundNumber(target.top ?? 0)
  activeObjectMetrics.width = roundNumber(target.getScaledWidth())
  activeObjectMetrics.height = roundNumber(target.getScaledHeight())
}

function applyActiveObjectMetrics() {
  if (!activeObject.value || !fabricCanvas.value) {
    return
  }

  const target = activeObject.value
  const rawWidth = Math.max(1, activeObjectMetrics.width)
  const rawHeight = Math.max(1, activeObjectMetrics.height)
  const baseWidth = typeof target.width === 'number' && target.width > 0 ? target.width : rawWidth
  const baseHeight = typeof target.height === 'number' && target.height > 0 ? target.height : rawHeight

  target.set({
    left: activeObjectMetrics.x,
    top: activeObjectMetrics.y,
    scaleX: rawWidth / baseWidth,
    scaleY: rawHeight / baseHeight,
  })

  target.setCoords()
  fabricCanvas.value.requestRenderAll()
  syncActiveObjectMetrics(target)
}

async function handleSave() {
  if (!fabricCanvas.value) {
    return
  }

  saving.value = true
  statusMessage.value = ''

  try {
    const payload = buildSavePayload('manual')
    const version = await saveExhibitionVersion(exhibitionId.value, payload)
    appStore.showToast(`草稿已保存为 V${version.versionNo}`, 'success')
  } catch (error) {
    statusMessage.value = getErrorMessage(error, '草稿保存失败')
  } finally {
    saving.value = false
  }
}

async function handlePublish() {
  if (!fabricCanvas.value) {
    return
  }

  publishing.value = true
  statusMessage.value = ''

  try {
    const savedVersion = await saveExhibitionVersion(exhibitionId.value, buildSavePayload('publish'))
    await publishExhibition(exhibitionId.value, {
      versionNo: savedVersion.versionNo,
      visibility: 'public',
    })
    appStore.showToast(`版本 V${savedVersion.versionNo} 已发布`, 'success')
  } catch (error) {
    statusMessage.value = getErrorMessage(error, '发布失败')
  } finally {
    publishing.value = false
  }
}

function buildSavePayload(saveType: SaveExhibitionVersionRequest['saveType']): SaveExhibitionVersionRequest {
  const canvas = fabricCanvas.value

  if (!canvas) {
    throw new Error('画布尚未初始化')
  }

  return {
    saveType,
    versionNote: saveType === 'publish' ? '编辑器发布版本' : '编辑器手动保存',
    canvasConfig: {
      width: canvasMetrics.width,
      height: canvasMetrics.height,
      background: '#ffffff',
      zoom: canvas.getZoom(),
    },
    versionData: canvas.toJSON() as Record<string, unknown>,
  }
}

async function handleBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }

  await router.replace('/exhibitions')
}

function roundNumber(value: number) {
  return Math.round(value * 100) / 100
}

defineExpose({
  loadVersionData,
  handleSave,
})
</script>
