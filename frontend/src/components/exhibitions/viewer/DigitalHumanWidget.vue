<template>
  <div v-if="visible" class="pointer-events-none fixed bottom-6 right-6 z-40 flex flex-col items-end gap-2">
    <!-- 讲解气泡 -->
    <Transition name="bubble">
      <div
        v-if="expanded && currentCard"
        class="pointer-events-auto w-80 overflow-hidden rounded-2xl border border-brand-200 bg-white shadow-xl"
      >
        <header class="flex items-center justify-between gap-2 border-b border-slate-100 bg-gradient-to-r from-brand-50 to-sky-50 px-4 py-2.5">
          <div class="flex items-center gap-2 min-w-0">
            <span class="inline-flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-brand-600 text-xs text-white">AI</span>
            <div class="min-w-0">
              <p class="truncate text-xs font-semibold text-slate-900">{{ currentCard.title }}</p>
              <p class="text-[11px] text-slate-500">{{ currentCard.sourceLabel }} · {{ currentIndex + 1 }} / {{ cards.length }}</p>
            </div>
          </div>
          <button
            type="button"
            class="rounded-md p-1 text-slate-400 hover:bg-white/70 hover:text-slate-600"
            title="收起"
            @click="expanded = false"
          >
            <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M5.22 14.78a.75.75 0 001.06 0L10 11.06l3.72 3.72a.75.75 0 101.06-1.06l-3.72-3.72 3.72-3.72a.75.75 0 10-1.06-1.06L10 8.94 6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 000 1.06z" clip-rule="evenodd" />
            </svg>
          </button>
        </header>

        <div class="max-h-56 overflow-y-auto px-4 py-3">
          <p class="whitespace-pre-wrap text-sm leading-6 text-slate-700">{{ displayText }}<span v-if="typing" class="ml-0.5 inline-block w-0.5 animate-pulse bg-brand-500" style="height: 1em; vertical-align: middle;" /></p>
        </div>

        <audio
          v-if="currentCard.audioUrl"
          ref="audioEl"
          :src="currentCard.audioUrl"
          preload="metadata"
          class="w-full px-2"
          controls
        />

        <footer class="flex items-center justify-between gap-2 border-t border-slate-100 bg-slate-50 px-3 py-2">
          <div class="flex items-center gap-1">
            <button
              type="button"
              class="rounded-md p-1.5 text-slate-600 hover:bg-white disabled:opacity-40"
              :disabled="cards.length <= 1"
              title="上一条"
              @click="goPrev"
            >
              <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z" clip-rule="evenodd" />
              </svg>
            </button>
            <button
              type="button"
              class="rounded-md p-1.5 text-slate-600 hover:bg-white disabled:opacity-40"
              :disabled="cards.length <= 1"
              title="下一条"
              @click="goNext"
            >
              <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z" clip-rule="evenodd" />
              </svg>
            </button>
          </div>
          <label class="flex items-center gap-1.5 text-[11px] text-slate-500">
            <input v-model="autoTyping" type="checkbox" class="h-3 w-3 rounded border-slate-300" />
            打字机
          </label>
          <button
            type="button"
            class="rounded-md px-2 py-1 text-xs text-brand-700 hover:bg-white"
            @click="replay"
          >
            重播
          </button>
        </footer>
      </div>
    </Transition>

    <!-- 圆形触发按钮 -->
    <button
      type="button"
      class="pointer-events-auto flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-brand-500 to-brand-700 text-xl text-white shadow-xl ring-4 ring-white transition hover:scale-105"
      :title="cards.length ? '查看讲解' : '暂无讲解词'"
      @click="handleToggle"
    >
      <span v-if="expanded">🤖</span>
      <span v-else class="flex items-center gap-0.5">
        <span>🎙</span>
        <span v-if="cards.length" class="absolute -top-1 -right-1 inline-flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold shadow">
          {{ cards.length }}
        </span>
      </span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ExhibitDetail, ZoneDetail } from '@/api/types'

interface NarrationCard {
  key: string
  title: string
  sourceLabel: string
  content: string
  audioUrl?: string | null
}

const props = defineProps<{
  visible: boolean
  currentZone?: ZoneDetail | null
  zoneExhibits?: ExhibitDetail[]
}>()

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
  if (!cards.value.length) {
    expanded.value = false
    return
  }
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
