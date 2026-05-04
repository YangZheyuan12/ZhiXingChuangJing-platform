<template>
  <div
    class="absolute flex items-center justify-center rounded-full text-white shadow-lg backdrop-blur-sm transition-transform"
    :class="[
      dragging ? 'cursor-grabbing' : draggable ? 'cursor-grab' : 'cursor-pointer',
      selected
        ? 'border-2 border-amber-300 bg-amber-500/80 scale-110 ring-4 ring-amber-200/50'
        : 'border-2 border-white/80 bg-red-700/70 hover:scale-110 hover:border-amber-200',
    ]"
    :style="positionStyle"
    :title="hotspot.label || hotspot.hotspotType"
    @pointerdown="onPointerDown"
    @click.stop="onClick"
  >
    <span class="text-xs font-bold pointer-events-none select-none">{{ iconText }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { HotspotDetail } from '@/api/types'
import { resolveHotspotIcon } from '@/utils/hotspotIcon'

const props = defineProps<{
  hotspot: HotspotDetail
  zoom: number
  stageWidth: number
  stageHeight: number
  selected?: boolean
  draggable?: boolean
}>()

const emit = defineEmits<{
  click: [id: number]
  dragEnd: [id: number, xPercent: number, yPercent: number]
}>()

// ─── 拖拽状态 ───
const dragging = ref(false)
const dragOffset = ref<{ dx: number; dy: number } | null>(null) // 像素偏移（屏幕坐标）
let pointerStart: { x: number; y: number } | null = null
let didDrag = false
const DRAG_THRESHOLD_PX = 3

const positionStyle = computed(() => {
  const z = props.zoom
  const baseLeft = (props.hotspot.xPercent / 100) * props.stageWidth * z
  const baseTop = (props.hotspot.yPercent / 100) * props.stageHeight * z
  const offset = dragOffset.value ?? { dx: 0, dy: 0 }
  return {
    left: `${baseLeft + offset.dx}px`,
    top: `${baseTop + offset.dy}px`,
    width: `${(props.hotspot.wPercent / 100) * props.stageWidth * z}px`,
    height: `${(props.hotspot.hPercent / 100) * props.stageHeight * z}px`,
    touchAction: 'none',
  }
})

const iconText = computed(() => resolveHotspotIcon(props.hotspot))

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
    // 像素偏移 → 百分比增量
    const z = props.zoom
    const dxPct = (dragOffset.value.dx / (props.stageWidth * z)) * 100
    const dyPct = (dragOffset.value.dy / (props.stageHeight * z)) * 100
    const nextX = clampPct(props.hotspot.xPercent + dxPct)
    const nextY = clampPct(props.hotspot.yPercent + dyPct)
    emit('dragEnd', props.hotspot.id, nextX, nextY)
  }
  pointerStart = null
  dragOffset.value = null
}

function clampPct(v: number) {
  return Math.max(0, Math.min(100, Number(v.toFixed(2))))
}

function onClick() {
  if (didDrag) {
    didDrag = false
    return
  }
  emit('click', props.hotspot.id)
}
</script>
