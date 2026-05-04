<template>
  <div
    class="absolute select-none"
    :style="positionStyle"
    :class="dragging ? 'cursor-grabbing' : draggable ? 'cursor-grab' : 'cursor-default'"
    @pointerdown="onPointerDown"
  >
    <div class="relative">
      <!-- 立绘主体 -->
      <img
        v-if="placement.avatar2dUrl"
        :src="placement.avatar2dUrl"
        :alt="placement.name"
        :style="imageStyle"
        class="block h-auto select-none drop-shadow-[0_8px_20px_rgba(0,0,0,0.25)]"
        draggable="false"
      />
      <div
        v-else
        :style="imageStyle"
        class="flex flex-col items-center justify-center rounded-3xl border-2 border-dashed border-brand-300 bg-white/80 px-3 text-center text-xs font-medium text-brand-600 backdrop-blur-sm"
      >
        <span class="text-2xl">🧑‍🏫</span>
        <span class="mt-1 line-clamp-2">{{ placement.name }}</span>
      </div>

      <!-- 选中标记 -->
      <div
        v-if="selected"
        class="pointer-events-none absolute inset-0 rounded-2xl ring-4 ring-amber-300/70"
      />

      <!-- 名字标牌 -->
      <div
        class="pointer-events-none absolute -bottom-7 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-full bg-slate-900/80 px-2.5 py-0.5 text-xs font-medium text-white backdrop-blur-sm"
      >
        {{ placement.name }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ZoneDigitalHumanPlacement } from '@/api/types'

const BASE_WIDTH = 160
const BASE_HEIGHT = 240
const DRAG_THRESHOLD_PX = 3

const props = defineProps<{
  placement: ZoneDigitalHumanPlacement
  zoom: number
  stageWidth: number
  stageHeight: number
  selected?: boolean
  draggable?: boolean
}>()

const emit = defineEmits<{
  'drag-end': [xPercent: number, yPercent: number]
}>()

// ─── 拖拽状态 ───
const dragging = ref(false)
const dragOffset = ref<{ dx: number; dy: number } | null>(null)
let pointerStart: { x: number; y: number } | null = null
let didDrag = false

const positionStyle = computed(() => {
  const z = props.zoom
  const baseLeft = (props.placement.xPercent / 100) * props.stageWidth * z
  const baseTop = (props.placement.yPercent / 100) * props.stageHeight * z
  const offset = dragOffset.value ?? { dx: 0, dy: 0 }
  // 锚点：脚底中心（通过负 translate 实现），更符合"摆放在地面"的直觉
  return {
    left: `${baseLeft + offset.dx}px`,
    top: `${baseTop + offset.dy}px`,
    transform: 'translate(-50%, -100%)',
    touchAction: 'none',
  }
})

const imageStyle = computed(() => {
  const w = BASE_WIDTH * props.zoom * props.placement.scale
  const h = BASE_HEIGHT * props.zoom * props.placement.scale
  const flipX = props.placement.facing === 'right' ? -1 : 1
  return {
    width: `${w}px`,
    height: `${h}px`,
    objectFit: 'contain' as const,
    transform: `scaleX(${flipX})`,
    transformOrigin: 'center center',
  }
})

function onPointerDown(e: PointerEvent) {
  if (!props.draggable) return
  if (e.button !== 0) return
  pointerStart = { x: e.clientX, y: e.clientY }
  dragOffset.value = { dx: 0, dy: 0 }
  didDrag = false
  dragging.value = true
  window.addEventListener('pointermove', onPointerMove)
  window.addEventListener('pointerup', onPointerUp, { once: true })
  e.preventDefault()
  e.stopPropagation()
}

function onPointerMove(e: PointerEvent) {
  if (!pointerStart) return
  const dx = e.clientX - pointerStart.x
  const dy = e.clientY - pointerStart.y
  if (!didDrag && Math.hypot(dx, dy) > DRAG_THRESHOLD_PX) didDrag = true
  dragOffset.value = { dx, dy }
}

function onPointerUp() {
  window.removeEventListener('pointermove', onPointerMove)
  dragging.value = false
  if (!pointerStart || !dragOffset.value) {
    pointerStart = null
    dragOffset.value = null
    return
  }
  if (didDrag) {
    const z = props.zoom
    const dxPct = (dragOffset.value.dx / (props.stageWidth * z)) * 100
    const dyPct = (dragOffset.value.dy / (props.stageHeight * z)) * 100
    const nextX = clampPct(props.placement.xPercent + dxPct)
    const nextY = clampPct(props.placement.yPercent + dyPct)
    emit('drag-end', nextX, nextY)
  }
  pointerStart = null
  dragOffset.value = null
}

function clampPct(v: number) {
  return Math.max(0, Math.min(100, Number(v.toFixed(2))))
}
</script>
