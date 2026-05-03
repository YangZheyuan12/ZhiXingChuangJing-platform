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
      <div class="flex items-center gap-3">
        <!-- 社交互动按钮组（仅公开展厅可见） -->
        <div v-if="showSocialActions" class="flex items-center gap-1.5">
          <button
            type="button"
            class="inline-flex items-center gap-1.5 rounded-full border px-3 py-1.5 text-sm transition"
            :class="liked ? 'border-rose-300 bg-rose-50 text-rose-600' : 'border-slate-200 text-slate-600 hover:border-rose-200 hover:text-rose-600'"
            :disabled="likePending"
            :title="liked ? '取消点赞' : '点赞'"
            @click="toggleLike"
          >
            <svg class="h-4 w-4" viewBox="0 0 20 20" :fill="liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="1.5">
              <path d="M9.653 16.915l-.005-.003-.019-.01a20.759 20.759 0 01-1.162-.682 22.045 22.045 0 01-2.582-1.9C4.045 12.733 2 10.352 2 7.5a4.5 4.5 0 018-2.828A4.5 4.5 0 0118 7.5c0 2.852-2.044 5.233-3.885 6.82a22.049 22.049 0 01-3.744 2.582l-.019.01-.005.003h-.002a.739.739 0 01-.69.001l-.002-.001z" />
            </svg>
            <span class="tabular-nums">{{ likeCount }}</span>
          </button>

          <button
            type="button"
            class="inline-flex items-center gap-1.5 rounded-full border px-3 py-1.5 text-sm transition"
            :class="favorited ? 'border-amber-300 bg-amber-50 text-amber-700' : 'border-slate-200 text-slate-600 hover:border-amber-200 hover:text-amber-600'"
            :disabled="favoritePending"
            :title="favorited ? '取消收藏' : '收藏'"
            @click="toggleFavorite"
          >
            <svg class="h-4 w-4" viewBox="0 0 20 20" :fill="favorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="1.5">
              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.783-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
            </svg>
            <span class="tabular-nums">{{ favoriteCount }}</span>
          </button>

          <button
            type="button"
            class="inline-flex items-center gap-1.5 rounded-full border border-slate-200 px-3 py-1.5 text-sm text-slate-600 transition hover:border-brand-200 hover:text-brand-600"
            title="查看评论"
            @click="drawerVisible = true"
          >
            <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a9.06 9.06 0 01-2.347-.306c-.584.296-1.925.864-4.181 1.234-.2.032-.352-.176-.273-.362.354-.836.674-1.95.77-2.966C2.623 13.259 2 11.694 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z" clip-rule="evenodd" />
            </svg>
            <span class="tabular-nums">{{ commentCount }}</span>
          </button>
        </div>

        <div class="flex items-center gap-3 border-l border-slate-200 pl-3 text-sm text-gray-500">
          <span v-if="bundle?.exhibition.groupName">{{ bundle.exhibition.groupName }}</span>
          <span>{{ bundle?.exhibition.author?.nickname ?? '' }}</span>
        </div>
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

    <CommentDrawer
      :visible="drawerVisible"
      :comments="viewer?.comments ?? []"
      :teacher-reviews="viewer?.teacherReviews ?? []"
      :can-comment="!!authStore.user"
      :submitting="commentSubmitting"
      @close="drawerVisible = false"
      @submit="handleCreateComment"
    />

    <ExhibitDetailModal :exhibit="selectedExhibitDetail" @close="selectedExhibitDetail = null" />
    <DigitalHumanWidget
      :visible="hasNarrationContent"
      :current-zone="currentZone"
      :zone-exhibits="zoneExhibits"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Canvas } from 'fabric'
import { getExhibitionViewer } from '@/api/modules/exhibitions'
import { getEditorBundle } from '@/api/modules/editor-bundle'
import {
  createCommunityComment,
  favoriteCommunityExhibition,
  likeCommunityExhibition,
  unfavoriteCommunityExhibition,
  unlikeCommunityExhibition,
} from '@/api/modules/community'
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
import CommentDrawer from '@/components/exhibitions/viewer/CommentDrawer.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'

const LOGICAL_WIDTH = 1920
const LOGICAL_HEIGHT = 1080

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()
const exhibitionId = Number(route.params.exhibitionId)

const loading = ref(true)
const errorMessage = ref('')
const bundle = ref<EditorBundleResponse | null>(null)
const viewer = ref<ExhibitionViewerData | null>(null)

// ─── 社交互动状态 ───
const drawerVisible = ref(false)
const liked = ref(false)
const favorited = ref(false)
const likePending = ref(false)
const favoritePending = ref(false)
const commentSubmitting = ref(false)

const showSocialActions = computed(
  () => !!viewer.value && bundle.value?.exhibition.visibility === 'public',
)
const likeCount = computed(() => viewer.value?.exhibition.stats?.likeCount ?? 0)
const favoriteCount = computed(() => viewer.value?.exhibition.stats?.favoriteCount ?? 0)
const commentCount = computed(() => viewer.value?.exhibition.stats?.commentCount ?? 0)

function buildInteractionKey(type: 'like' | 'favorite') {
  return `zxcyj-viewer-${type}-${authStore.user?.id || 'guest'}-${exhibitionId}`
}
function readInteractionState(type: 'like' | 'favorite') {
  return localStorage.getItem(buildInteractionKey(type)) === '1'
}
function writeInteractionState(type: 'like' | 'favorite', value: boolean) {
  localStorage.setItem(buildInteractionKey(type), value ? '1' : '0')
}

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

const hasNarrationContent = computed(() => {
  if (currentZone.value?.narrationText && currentZone.value.narrationText.trim()) return true
  return zoneExhibits.value.some(e => e.narrations?.some(n => n.content && n.content.trim()))
})
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
    liked.value = readInteractionState('like')
    favorited.value = readInteractionState('favorite')
  } catch (e) {
    errorMessage.value = getErrorMessage(e, '展厅加载失败')
  } finally {
    loading.value = false
  }
}

// ═══════════════════════════════════════════════════════════
//  社交互动
// ═══════════════════════════════════════════════════════════

async function toggleLike() {
  if (likePending.value || !viewer.value) return
  if (!authStore.user) {
    appStore.showToast('请先登录后再点赞', 'info')
    return
  }
  const nextLiked = !liked.value
  liked.value = nextLiked
  writeInteractionState('like', nextLiked)
  // 乐观更新计数
  if (viewer.value.exhibition.stats) {
    viewer.value.exhibition.stats.likeCount = Math.max(
      0,
      viewer.value.exhibition.stats.likeCount + (nextLiked ? 1 : -1),
    )
  }
  likePending.value = true
  try {
    if (nextLiked) {
      await likeCommunityExhibition(exhibitionId)
    } else {
      await unlikeCommunityExhibition(exhibitionId)
    }
  } catch (error) {
    // 回滚
    liked.value = !nextLiked
    writeInteractionState('like', liked.value)
    if (viewer.value.exhibition.stats) {
      viewer.value.exhibition.stats.likeCount = Math.max(
        0,
        viewer.value.exhibition.stats.likeCount + (nextLiked ? -1 : 1),
      )
    }
    appStore.showToast(getErrorMessage(error, '点赞失败'), 'error')
  } finally {
    likePending.value = false
  }
}

async function toggleFavorite() {
  if (favoritePending.value || !viewer.value) return
  if (!authStore.user) {
    appStore.showToast('请先登录后再收藏', 'info')
    return
  }
  const nextFavorited = !favorited.value
  favorited.value = nextFavorited
  writeInteractionState('favorite', nextFavorited)
  if (viewer.value.exhibition.stats) {
    viewer.value.exhibition.stats.favoriteCount = Math.max(
      0,
      viewer.value.exhibition.stats.favoriteCount + (nextFavorited ? 1 : -1),
    )
  }
  favoritePending.value = true
  try {
    if (nextFavorited) {
      await favoriteCommunityExhibition(exhibitionId)
    } else {
      await unfavoriteCommunityExhibition(exhibitionId)
    }
  } catch (error) {
    favorited.value = !nextFavorited
    writeInteractionState('favorite', favorited.value)
    if (viewer.value.exhibition.stats) {
      viewer.value.exhibition.stats.favoriteCount = Math.max(
        0,
        viewer.value.exhibition.stats.favoriteCount + (nextFavorited ? -1 : 1),
      )
    }
    appStore.showToast(getErrorMessage(error, '收藏失败'), 'error')
  } finally {
    favoritePending.value = false
  }
}

async function handleCreateComment(content: string) {
  if (commentSubmitting.value) return
  commentSubmitting.value = true
  try {
    const created = await createCommunityComment(exhibitionId, { content })
    if (viewer.value) {
      viewer.value.comments = [...viewer.value.comments, created]
      if (viewer.value.exhibition.stats) {
        viewer.value.exhibition.stats.commentCount += 1
      }
    }
    appStore.showToast('评论已发布', 'success')
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '评论发布失败'), 'error')
  } finally {
    commentSubmitting.value = false
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
