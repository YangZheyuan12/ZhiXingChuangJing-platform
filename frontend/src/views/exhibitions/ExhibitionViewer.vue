<template>
  <div v-if="loading" class="flex h-screen items-center justify-center bg-neutral-100">
    <p class="text-sm text-gray-500">加载中...</p>
  </div>

  <div v-else-if="errorMessage" class="flex h-screen items-center justify-center bg-neutral-100">
    <div class="text-center">
      <p class="text-sm text-rose-500">{{ errorMessage }}</p>
      <button type="button" class="mt-4 rounded-md bg-brand-600 px-4 py-2 text-sm text-white hover:bg-brand-700" @click="router.back()">返回</button>
    </div>
  </div>

  <div v-else class="flex h-screen flex-col bg-neutral-100">
    <!-- ═══ 顶栏 ═══ -->
    <header class="flex shrink-0 items-center justify-between border-b border-gray-200 bg-white px-6 py-3">
      <div class="flex items-center gap-3">
        <button type="button" class="rounded-md px-3 py-1.5 text-sm text-gray-500 transition hover:bg-gray-100" @click="router.back()">← 返回</button>
        <h1 class="text-base font-semibold text-gray-900">{{ bundle?.exhibition.title || '展厅浏览' }}</h1>
        <span v-if="currentZone" class="rounded bg-brand-50 px-2 py-0.5 text-xs text-brand-700">{{ currentZone.title }}</span>
      </div>
      <div class="flex items-center gap-4 text-sm text-gray-500">
        <span v-if="bundle?.exhibition.groupName">{{ bundle.exhibition.groupName }}</span>
        <span>{{ bundle?.exhibition.ownerName }}</span>
      </div>
    </header>

    <div class="flex min-h-0 flex-1 overflow-hidden">
      <!-- ═══ 左侧导览面板 ═══ -->
      <aside class="hidden w-56 shrink-0 flex-col border-r border-gray-200 bg-white p-3 md:flex">
        <MiniMap
          :zones="zones"
          :current-zone-id="currentZone?.id ?? null"
          @navigate="handleZoneNavigate"
        />
        <div v-if="zoneExhibits.length" class="mt-4">
          <h3 class="mb-2 text-xs font-semibold uppercase tracking-widest text-gray-400">本展区展品</h3>
          <div class="space-y-1">
            <button
              v-for="ex in zoneExhibits"
              :key="ex.id"
              type="button"
              class="w-full truncate rounded-lg px-3 py-2 text-left text-sm text-gray-600 transition hover:bg-gray-50"
              @click="openExhibitDetail(ex)"
            >
              {{ ex.title }}
            </button>
          </div>
        </div>
      </aside>

      <!-- ═══ 画布区域 ═══ -->
      <main class="relative flex min-w-0 flex-1 flex-col">
        <div class="flex flex-1 items-center justify-center overflow-hidden p-4">
          <div ref="viewerWrapper" class="relative">
            <div v-if="currentZone?.backgroundUrl" class="absolute inset-0">
              <img :src="currentZone.backgroundUrl" class="h-full w-full object-cover" :style="{ width: stageWidth + 'px', height: stageHeight + 'px' }" />
            </div>

            <canvas ref="viewerCanvasEl" class="relative" />

            <HotspotButtons
              :hotspots="currentHotspots"
              @navigate="handleHotspotNavigate"
            />

            <!-- 视频/音频覆盖层 -->
            <div
              v-for="(media, idx) in mediaOverlays"
              :key="idx"
              class="absolute overflow-hidden rounded-xl"
              :style="media.style"
            >
              <video
                v-if="media.type === 'video'"
                :src="media.url"
                controls
                preload="metadata"
                class="h-full w-full bg-black object-contain"
              />
              <audio
                v-else
                :src="media.url"
                controls
                preload="metadata"
                class="w-full"
              />
            </div>
          </div>
        </div>

        <ViewerNavigation
          :current-index="currentZoneIndex"
          :total="zones.length"
          @prev="navigatePrev"
          @next="navigateNext"
        />
      </main>
    </div>

    <!-- ═══ 评论区域 ═══ -->
    <section v-if="viewer && (viewer.teacherReviews.length > 0 || viewer.comments.length > 0)" class="border-t border-gray-200 bg-white px-6 py-8">
      <div class="mx-auto max-w-4xl space-y-8">
        <div v-if="viewer.teacherReviews.length > 0">
          <h2 class="mb-4 text-sm font-semibold uppercase tracking-widest text-gray-400">教师点评</h2>
          <div class="space-y-3">
            <article v-for="review in viewer.teacherReviews" :key="review.id" class="rounded-xl border border-gray-200 p-4">
              <div class="flex items-center justify-between gap-3">
                <span class="font-medium text-gray-900">{{ review.reviewer?.nickname || '老师' }}</span>
                <span class="text-xs text-gray-400">{{ review.createdAt }}</span>
              </div>
              <p class="mt-2 text-sm leading-6 text-gray-600">{{ review.comment }}</p>
              <div v-if="review.score != null" class="mt-2 text-xs text-amber-600">评分：{{ review.score }}</div>
            </article>
          </div>
        </div>

        <div v-if="viewer.comments.length > 0">
          <h2 class="mb-4 text-sm font-semibold uppercase tracking-widest text-gray-400">评论</h2>
          <div class="space-y-3">
            <article v-for="comment in viewer.comments" :key="comment.id" class="rounded-xl border border-gray-200 p-4">
              <div class="flex items-center justify-between gap-3">
                <span class="font-medium text-gray-900">{{ comment.user?.nickname || '匿名' }}</span>
                <span class="text-xs text-gray-400">{{ comment.createdAt }}</span>
              </div>
              <p class="mt-2 text-sm leading-6 text-gray-600">{{ comment.content }}</p>
            </article>
          </div>
        </div>
      </div>
    </section>

    <ExhibitDetailModal :exhibit="selectedExhibitDetail" @close="selectedExhibitDetail = null" />
    <DigitalHumanWidget :visible="!!bundle?.digitalHuman" />
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Canvas } from 'fabric'
import { getExhibitionViewer } from '@/api/modules/exhibitions'
import { getEditorBundle } from '@/api/modules/editor-bundle'
import { getErrorMessage } from '@/utils/request'
import type {
  EditorBundleResponse,
  ExhibitDetail,
  ExhibitionViewerData,
  HotspotDetail,
  ZoneDetail,
} from '@/api/types'
import MiniMap from '@/components/exhibitions/viewer/MiniMap.vue'
import HotspotButtons from '@/components/exhibitions/viewer/HotspotButtons.vue'
import ViewerNavigation from '@/components/exhibitions/viewer/ViewerNavigation.vue'
import ExhibitDetailModal from '@/components/exhibitions/viewer/ExhibitDetailModal.vue'
import DigitalHumanWidget from '@/components/exhibitions/viewer/DigitalHumanWidget.vue'

const LOGICAL_WIDTH = 1920
const LOGICAL_HEIGHT = 1080

const route = useRoute()
const router = useRouter()
const exhibitionId = Number(route.params.exhibitionId)

const loading = ref(true)
const errorMessage = ref('')
const bundle = ref<EditorBundleResponse | null>(null)
const viewer = ref<ExhibitionViewerData | null>(null)

// ─── 展区管理 ───
const zones = computed<ZoneDetail[]>(() => bundle.value?.zones ?? [])
const currentZoneIndex = ref(0)
const currentZone = computed<ZoneDetail | null>(() => zones.value[currentZoneIndex.value] ?? null)

const allExhibits = computed<ExhibitDetail[]>(() => bundle.value?.exhibits ?? [])
const allHotspots = computed<HotspotDetail[]>(() => bundle.value?.hotspots ?? [])

const zoneExhibits = computed(() =>
  currentZone.value
    ? allExhibits.value.filter(e => e.zoneId === currentZone.value!.id)
    : [],
)
const currentHotspots = computed(() =>
  currentZone.value
    ? allHotspots.value.filter(h => h.zoneId === currentZone.value!.id)
    : [],
)

const selectedExhibitDetail = ref<ExhibitDetail | null>(null)

// ─── 画布 ───
const viewerCanvasEl = ref<HTMLCanvasElement | null>(null)
const viewerWrapper = ref<HTMLElement | null>(null)
const fabricCanvas = shallowRef<Canvas | null>(null)
let resizeObserver: ResizeObserver | null = null

const displayZoom = ref(1)
const stageWidth = computed(() => LOGICAL_WIDTH * displayZoom.value)
const stageHeight = computed(() => LOGICAL_HEIGHT * displayZoom.value)

interface MediaOverlay {
  type: 'video' | 'audio'
  url: string
  style: Record<string, string>
}
const mediaOverlays = ref<MediaOverlay[]>([])

// ═══════════════════════════════════════════════════════════
//  数据加载
// ═══════════════════════════════════════════════════════════

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [bundleData, viewerData] = await Promise.all([
      getEditorBundle(exhibitionId),
      getExhibitionViewer(exhibitionId).catch(() => null),
    ])
    bundle.value = bundleData
    viewer.value = viewerData
  } catch (e) {
    errorMessage.value = getErrorMessage(e, '展厅加载失败')
  } finally {
    loading.value = false
  }
}

// ═══════════════════════════════════════════════════════════
//  画布
// ═══════════════════════════════════════════════════════════

function initViewerCanvas() {
  if (!viewerCanvasEl.value) return
  const canvas = new Canvas(viewerCanvasEl.value, {
    width: LOGICAL_WIDTH,
    height: LOGICAL_HEIGHT,
    backgroundColor: 'transparent',
    selection: false,
  })
  fabricCanvas.value = canvas

  fitToContainer()
  resizeObserver = new ResizeObserver(() => fitToContainer())
  if (viewerWrapper.value) resizeObserver.observe(viewerWrapper.value)
}

function fitToContainer() {
  const canvas = fabricCanvas.value
  if (!canvas) return

  const maxW = Math.min(window.innerWidth - 280, 1440)
  const maxH = window.innerHeight - 200
  const zoom = Math.min(maxW / LOGICAL_WIDTH, maxH / LOGICAL_HEIGHT)

  displayZoom.value = zoom
  canvas.setZoom(zoom)
  canvas.setDimensions({
    width: LOGICAL_WIDTH * zoom,
    height: LOGICAL_HEIGHT * zoom,
  })
  canvas.requestRenderAll()
  updateMediaOverlays()
}

// ═══════════════════════════════════════════════════════════
//  展区渲染 & 切换
// ═══════════════════════════════════════════════════════════

async function renderCurrentZone() {
  const canvas = fabricCanvas.value
  const zone = currentZone.value
  if (!canvas || !zone) return

  canvas.clear()
  canvas.backgroundColor = 'transparent'

  const data = zone.canvasData
  if (data && typeof data === 'object' && 'objects' in data) {
    await canvas.loadFromJSON(data)
  }

  canvas.getObjects().forEach((obj) => {
    obj.set({ selectable: false, evented: false })
  })

  fitToContainer()
  canvas.requestRenderAll()
  collectMediaOverlays()
}

function handleZoneNavigate(zone: ZoneDetail) {
  const idx = zones.value.findIndex(z => z.id === zone.id)
  if (idx >= 0) switchToZone(idx)
}

function handleHotspotNavigate(hotspot: HotspotDetail) {
  if (hotspot.targetZoneId) {
    const idx = zones.value.findIndex(z => z.id === hotspot.targetZoneId)
    if (idx >= 0) switchToZone(idx)
  }
}

function navigatePrev() {
  if (currentZoneIndex.value > 0) switchToZone(currentZoneIndex.value - 1)
}

function navigateNext() {
  if (currentZoneIndex.value < zones.value.length - 1) switchToZone(currentZoneIndex.value + 1)
}

async function switchToZone(idx: number) {
  currentZoneIndex.value = idx
  mediaOverlays.value = []
  await renderCurrentZone()
}

function openExhibitDetail(exhibit: ExhibitDetail) {
  selectedExhibitDetail.value = exhibit
}

// ═══════════════════════════════════════════════════════════
//  媒体覆盖层
// ═══════════════════════════════════════════════════════════

function collectMediaOverlays() {
  const canvas = fabricCanvas.value
  if (!canvas) return
  const overlays: MediaOverlay[] = []
  canvas.getObjects().forEach((obj) => {
    const assetType = (obj as any).assetType
    const mediaUrl = (obj as any).mediaUrl
    if (!mediaUrl || (assetType !== 'video' && assetType !== 'audio')) return
    overlays.push({
      type: assetType,
      url: mediaUrl,
      style: computeOverlayStyle(obj),
    })
  })
  mediaOverlays.value = overlays
}

function updateMediaOverlays() {
  const canvas = fabricCanvas.value
  if (!canvas) return
  let idx = 0
  canvas.getObjects().forEach((obj) => {
    const assetType = (obj as any).assetType
    if (assetType !== 'video' && assetType !== 'audio') return
    if (idx < mediaOverlays.value.length) {
      mediaOverlays.value[idx].style = computeOverlayStyle(obj)
    }
    idx++
  })
}

function computeOverlayStyle(obj: any): Record<string, string> {
  const zoom = displayZoom.value
  const left = (obj.left ?? 0) * zoom
  const top = (obj.top ?? 0) * zoom
  const w = (obj.width ?? 0) * (obj.scaleX ?? 1) * zoom
  const h = (obj.height ?? 0) * (obj.scaleY ?? 1) * zoom
  return {
    left: `${left}px`,
    top: `${top}px`,
    width: `${w}px`,
    height: `${h}px`,
  }
}

// ═══════════════════════════════════════════════════════════
//  生命周期
// ═══════════════════════════════════════════════════════════

onMounted(async () => {
  await loadData()
  if (!errorMessage.value && bundle.value) {
    initViewerCanvas()
    await renderCurrentZone()
  }
})

onBeforeUnmount(() => {
  if (resizeObserver) resizeObserver.disconnect()
  fabricCanvas.value?.dispose()
})
</script>
