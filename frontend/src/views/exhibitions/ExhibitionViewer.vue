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

  <div v-else class="flex min-h-screen flex-col bg-neutral-100">
    <!-- ═══ 顶栏 ═══ -->
    <header class="flex shrink-0 items-center justify-between border-b border-gray-200 bg-white px-6 py-3">
      <div class="flex items-center gap-3">
        <button type="button" class="rounded-md px-3 py-1.5 text-sm text-gray-500 transition hover:bg-gray-100" @click="router.back()">← 返回</button>
        <h1 class="text-base font-semibold text-gray-900">{{ viewer?.exhibition.title || '展厅浏览' }}</h1>
      </div>
      <div class="flex items-center gap-4 text-sm text-gray-500">
        <span v-if="viewer?.exhibition.groupName">{{ viewer.exhibition.groupName }}</span>
        <span>{{ viewer?.exhibition.ownerName }}</span>
      </div>
    </header>

    <!-- ═══ 画布区域 ═══ -->
    <main class="flex flex-1 items-center justify-center p-6">
      <div ref="viewerWrapper" class="relative">
        <canvas ref="viewerCanvasEl" />

        <!-- 视频/音频覆盖层：在画布对应位置覆盖真实的 HTML5 播放器 -->
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
    </main>

    <!-- ═══ 评论区域 ═══ -->
    <section v-if="viewer && (viewer.teacherReviews.length > 0 || viewer.comments.length > 0)" class="border-t border-gray-200 bg-white px-6 py-8">
      <div class="mx-auto max-w-4xl space-y-8">
        <!-- 教师点评 -->
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

        <!-- 学生评论 -->
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
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Canvas } from 'fabric'
import { getExhibitionViewer, getExhibitionVersions } from '@/api/modules/exhibitions'
import { getErrorMessage } from '@/utils/request'
import type { ExhibitionViewerData, ExhibitionVersion } from '@/api/types'

const LOGICAL_WIDTH = 1920
const LOGICAL_HEIGHT = 1080

const route = useRoute()
const router = useRouter()
const exhibitionId = Number(route.params.exhibitionId)

const loading = ref(true)
const errorMessage = ref('')
const viewer = ref<ExhibitionViewerData | null>(null)
const versions = ref<ExhibitionVersion[]>([])

const viewerCanvasEl = ref<HTMLCanvasElement | null>(null)
const viewerWrapper = ref<HTMLElement | null>(null)
const fabricCanvas = shallowRef<Canvas | null>(null)
let resizeObserver: ResizeObserver | null = null

const displayZoom = ref(1)

interface MediaOverlay {
  type: 'video' | 'audio'
  url: string
  style: Record<string, string>
}

const mediaOverlays = ref<MediaOverlay[]>([])

async function loadViewer() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [viewerData, versionList] = await Promise.all([
      getExhibitionViewer(exhibitionId),
      getExhibitionVersions(exhibitionId).catch(() => [] as ExhibitionVersion[]),
    ])
    viewer.value = viewerData
    versions.value = versionList
  } catch (e) {
    errorMessage.value = getErrorMessage(e, '展厅加载失败')
  } finally {
    loading.value = false
  }
}

function initViewerCanvas() {
  if (!viewerCanvasEl.value || !viewer.value) return

  const canvas = new Canvas(viewerCanvasEl.value, {
    width: LOGICAL_WIDTH,
    height: LOGICAL_HEIGHT,
    backgroundColor: viewer.value.renderData?.canvasConfig?.background || '#f7efe9',
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

  const maxW = Math.min(window.innerWidth - 48, 1440)
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

async function renderVersionData() {
  const canvas = fabricCanvas.value
  if (!canvas || !viewer.value) return

  const vd = viewer.value.renderData
  if (!vd?.canvasConfig) return

  const rawData = (viewer.value as any).renderData?.canvasConfig
  const savedW = rawData?.width || LOGICAL_WIDTH
  const savedH = rawData?.height || LOGICAL_HEIGHT

  // 尝试从 versionData 加载
  const versionJson = findVersionJson()
  if (versionJson) {
    await canvas.loadFromJSON(versionJson)

    // 兼容旧版不同逻辑尺寸
    if (savedW !== LOGICAL_WIDTH || savedH !== LOGICAL_HEIGHT) {
      const sx = LOGICAL_WIDTH / savedW
      const sy = LOGICAL_HEIGHT / savedH
      canvas.getObjects().forEach((obj) => {
        obj.set({
          left: (obj.left ?? 0) * sx,
          top: (obj.top ?? 0) * sy,
          scaleX: (obj.scaleX ?? 1) * sx,
          scaleY: (obj.scaleY ?? 1) * sy,
        })
        obj.setCoords()
      })
    }
  }

  // 禁止交互
  canvas.getObjects().forEach((obj) => {
    obj.set({ selectable: false, evented: false })
  })

  fitToContainer()
  canvas.requestRenderAll()
  collectMediaOverlays()
}

function findVersionJson(): Record<string, unknown> | null {
  // 优先从已发布的版本中获取原始 Fabric JSON
  const publishedNo = viewer.value?.exhibition?.publishedVersionNo
  if (publishedNo && versions.value.length > 0) {
    const published = versions.value.find((v) => v.versionNo === publishedNo)
    if (published?.versionData && typeof published.versionData === 'object' && 'objects' in published.versionData) {
      return published.versionData
    }
  }
  // 回退：取最新版本
  if (versions.value.length > 0) {
    const latest = versions.value[0]
    if (latest?.versionData && typeof latest.versionData === 'object' && 'objects' in latest.versionData) {
      return latest.versionData
    }
  }
  // 最后回退：尝试 renderData 本身
  const rd = viewer.value?.renderData as any
  if (rd?.objects) return rd
  return null
}

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

onMounted(async () => {
  await loadViewer()
  if (!errorMessage.value && viewer.value) {
    initViewerCanvas()
    await renderVersionData()
  }
})

onBeforeUnmount(() => {
  if (resizeObserver) resizeObserver.disconnect()
  fabricCanvas.value?.dispose()
})
</script>
