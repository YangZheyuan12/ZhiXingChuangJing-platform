<template>
  <!-- ═══ 模式 A：展区已摆放数字人立绘，直接在画布里渲染 ═══ -->
  <div
    v-if="placement"
    class="pointer-events-none absolute z-40"
    :style="placementAnchorStyle"
  >
    <div class="relative">
      <!-- 讲解气泡（挂在立绘上方） -->
      <Transition name="bubble">
        <NarrationBubbleCard
          v-if="expanded && currentCard"
          class="pointer-events-auto absolute bottom-full left-1/2 mb-3 w-80 -translate-x-1/2"
          :card="currentCard"
          :display-text="displayText"
          :typing="typing"
          v-model:auto-typing="autoTyping"
          :index="currentIndex"
          :total="cards.length"
          @close="expanded = false"
          @prev="goPrev"
          @next="goNext"
          @replay="replay"
        />
      </Transition>

      <!-- 立绘 = 触发按钮 -->
      <button
        type="button"
        class="pointer-events-auto block bg-transparent p-0"
        :class="cards.length ? 'cursor-pointer' : 'cursor-default'"
        :disabled="!cards.length"
        :title="cards.length ? '点击查看讲解' : placement.name"
        @click="handleToggle"
      >
        <img
          v-if="placement.avatar2dUrl"
          :src="placement.avatar2dUrl"
          :alt="placement.name"
          :style="placementImageStyle"
          class="block h-auto select-none drop-shadow-[0_8px_20px_rgba(0,0,0,0.3)]"
          draggable="false"
        />
        <div
          v-else
          :style="placementImageStyle"
          class="flex flex-col items-center justify-center rounded-3xl border-2 border-dashed border-brand-300 bg-white/80 px-3 text-center text-xs font-medium text-brand-600 backdrop-blur-sm"
        >
          <span class="text-2xl">🧑‍🏫</span>
          <span class="mt-1 line-clamp-2">{{ placement.name }}</span>
        </div>

        <!-- 名字标牌 -->
        <div class="pointer-events-none absolute -bottom-6 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-full bg-slate-900/80 px-2.5 py-0.5 text-xs font-medium text-white backdrop-blur-sm">
          {{ placement.name }}
        </div>

        <!-- 未读徽标 -->
        <span
          v-if="cards.length && !expanded"
          class="absolute right-0 top-0 inline-flex h-6 w-6 items-center justify-center rounded-full bg-rose-500 text-[11px] font-bold text-white shadow ring-2 ring-white"
        >
          {{ cards.length }}
        </span>
      </button>
    </div>
  </div>

  <!-- ═══ 模式 B：未摆放立绘时的兵底 — 右下角悬浮按钮（无讲解内容也展示） ═══ -->
  <div
    v-else
    class="pointer-events-none fixed bottom-6 right-6 z-40 flex flex-col items-end gap-2"
  >
    <Transition name="bubble">
      <!-- 有讲解：正常气泡 -->
      <NarrationBubbleCard
        v-if="expanded && currentCard"
        class="pointer-events-auto w-80"
        :card="currentCard"
        :display-text="displayText"
        :typing="typing"
        v-model:auto-typing="autoTyping"
        :index="currentIndex"
        :total="cards.length"
        @close="expanded = false"
        @prev="goPrev"
        @next="goNext"
        @replay="replay"
      />
      <!-- 无讲解：空态提示气泡 -->
      <div
        v-else-if="expanded && !cards.length"
        class="pointer-events-auto w-80 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xl"
      >
        <div class="flex items-center justify-between gap-2 border-b border-slate-100 bg-slate-50 px-4 py-2.5">
          <p class="text-xs font-semibold text-slate-700">💬 讲解词暂未准备</p>
          <button
            type="button"
            class="rounded-md p-1 text-slate-400 hover:bg-white"
            title="收起"
            @click="expanded = false"
          >✕</button>
        </div>
        <p class="px-4 py-3 text-xs leading-5 text-slate-500">
          本展区暂时没有写讲解词，欢迎自由探索其他展区✨
        </p>
      </div>
    </Transition>

    <button
      type="button"
      class="pointer-events-auto relative flex h-14 w-14 items-center justify-center rounded-full text-xl shadow-xl ring-4 ring-white transition hover:scale-105"
      :class="cards.length
        ? 'bg-gradient-to-br from-brand-500 to-brand-700 text-white'
        : 'bg-gradient-to-br from-slate-400 to-slate-500 text-white/90'"
      :title="cards.length ? '查看讲解' : '暂无讲解词，点击查看提示'"
      @click="handleToggle"
    >
      <span v-if="expanded">🤖</span>
      <span v-else>🎙</span>
      <span
        v-if="cards.length && !expanded"
        class="absolute -top-1 -right-1 inline-flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold shadow"
      >
        {{ cards.length }}
      </span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import type { ExhibitDetail, ZoneDetail, ZoneDigitalHumanPlacement } from '@/api/types'
import NarrationBubbleCard from './NarrationBubbleCard.vue'

const BASE_AVATAR_WIDTH = 160
const BASE_AVATAR_HEIGHT = 240

interface NarrationCard {
  key: string
  title: string
  sourceLabel: string
  content: string
  audioUrl?: string | null
}

const props = defineProps<{
  currentZone?: ZoneDetail | null
  zoneExhibits?: ExhibitDetail[]
  /** 当前展区的数字人摆放，有值时以立绘模式渲染；否则退回右下角兵底。 */
  placement?: ZoneDigitalHumanPlacement | null
  /** 立绘定位用的舞台逻辑尺寸和显示缩放，立绘模式下必填。 */
  stageWidth?: number
  stageHeight?: number
  zoom?: number
  /** 切展区时自动弹首条讲解（默认 true） */
  autoOpen?: boolean
}>()

// ─── 立绘模式定位 / 样式 ───
const placementAnchorStyle = computed(() => {
  if (!props.placement) return {}
  const z = props.zoom ?? 1
  const w = props.stageWidth ?? 1920
  const h = props.stageHeight ?? 1080
  const left = (props.placement.xPercent / 100) * w * z
  const top = (props.placement.yPercent / 100) * h * z
  // 锚点：立绘脚底中心
  return {
    left: `${left}px`,
    top: `${top}px`,
    transform: 'translate(-50%, -100%)',
  }
})

const placementImageStyle = computed(() => {
  if (!props.placement) return {}
  const z = props.zoom ?? 1
  const scale = props.placement.scale || 1
  const flipX = props.placement.facing === 'right' ? -1 : 1
  return {
    width: `${BASE_AVATAR_WIDTH * z * scale}px`,
    height: `${BASE_AVATAR_HEIGHT * z * scale}px`,
    objectFit: 'contain' as const,
    transform: `scaleX(${flipX})`,
    transformOrigin: 'center center',
  }
})

const expanded = ref(false)
const currentIndex = ref(0)
const autoTyping = ref(true)
const displayText = ref('')
const typing = ref(false)
let typingTimer: number | null = null

const cards = computed<NarrationCard[]>(() => {
  const result: NarrationCard[] = []
  const zone = props.currentZone
  if (zone?.narrationText && zone.narrationText.trim()) {
    result.push({
      key: `zone-${zone.id}`,
      title: zone.title || '展区讲解',
      sourceLabel: '展区讲解',
      content: zone.narrationText.trim(),
    })
  }
  const exhibits = props.zoneExhibits ?? []
  exhibits.forEach((exhibit) => {
    exhibit.narrations?.forEach((narration, idx) => {
      if (!narration.content || !narration.content.trim()) return
      result.push({
        key: `exhibit-${exhibit.id}-${narration.id ?? idx}`,
        title: exhibit.title,
        sourceLabel: '展品讲解',
        content: narration.content.trim(),
        audioUrl: narration.audioUrl,
      })
    })
  })
  return result
})

const currentCard = computed<NarrationCard | null>(() =>
  cards.value[currentIndex.value] ?? null,
)

function handleToggle() {
  // 空态气泡也允许展开（显示"讲解词暂未准备"提示）；立绘按钮有 :disabled 兜住，这里保持通用
  expanded.value = !expanded.value
}

function goPrev() {
  if (!cards.value.length) return
  currentIndex.value = (currentIndex.value - 1 + cards.value.length) % cards.value.length
}

function goNext() {
  if (!cards.value.length) return
  currentIndex.value = (currentIndex.value + 1) % cards.value.length
}

function replay() {
  startTyping()
}

function stopTyping() {
  if (typingTimer != null) {
    clearInterval(typingTimer)
    typingTimer = null
  }
  typing.value = false
}

function startTyping() {
  stopTyping()
  const card = currentCard.value
  if (!card) {
    displayText.value = ''
    return
  }
  if (!autoTyping.value) {
    displayText.value = card.content
    return
  }
  displayText.value = ''
  typing.value = true
  const fullText = card.content
  let idx = 0
  typingTimer = window.setInterval(() => {
    idx += 1
    displayText.value = fullText.slice(0, idx)
    if (idx >= fullText.length) {
      stopTyping()
    }
  }, 40)
}

// 当卡片 / 索引 / 可见性变化时，重启打字机
watch(
  [() => currentCard.value?.key, () => expanded.value, () => autoTyping.value],
  () => {
    if (expanded.value && currentCard.value) {
      startTyping()
    } else {
      stopTyping()
      if (currentCard.value) displayText.value = currentCard.value.content
    }
  },
  { immediate: true },
)

// 当 zone 或 cards 列表变化时，重置到第一条
watch(
  () => props.currentZone?.id,
  () => {
    currentIndex.value = 0
  },
)

watch(
  () => cards.value.length,
  (len) => {
    if (currentIndex.value >= len) currentIndex.value = 0
  },
)

// ─── 切展区自动弹首条讲解（默认开启，仅当有讲解时） ───
let autoOpenTimer: number | null = null
watch(
  () => props.currentZone?.id,
  (id) => {
    if (autoOpenTimer != null) {
      clearTimeout(autoOpenTimer)
      autoOpenTimer = null
    }
    // 切换展区时先收起，避免老气泡残留
    expanded.value = false
    if (id == null) return
    if (props.autoOpen === false) return
    // 延迟 1s 后如果当前展区确实有讲解内容，自动弹开首条
    autoOpenTimer = window.setTimeout(() => {
      if (cards.value.length > 0) {
        expanded.value = true
      }
    }, 1000)
  },
)

// ─── 键盘快捷键：Esc 关、左右键翻页 ───
function onKeyDown(e: KeyboardEvent) {
  // 用户正在输入框里打字时不拦截
  const target = e.target as HTMLElement | null
  if (target) {
    const tag = target.tagName
    if (tag === 'INPUT' || tag === 'TEXTAREA' || target.isContentEditable) return
  }
  if (e.key === 'Escape') {
    if (expanded.value) {
      expanded.value = false
      e.preventDefault()
    }
    return
  }
  if (!expanded.value) return
  if (e.key === 'ArrowLeft') {
    if (cards.value.length > 1) {
      goPrev()
      e.preventDefault()
    }
  } else if (e.key === 'ArrowRight') {
    if (cards.value.length > 1) {
      goNext()
      e.preventDefault()
    }
  }
}

onMounted(() => {
  window.addEventListener('keydown', onKeyDown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeyDown)
  if (autoOpenTimer != null) clearTimeout(autoOpenTimer)
  stopTyping()
})
</script>

<style scoped>
.bubble-enter-active,
.bubble-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}
.bubble-enter-from,
.bubble-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.95);
}
</style>
