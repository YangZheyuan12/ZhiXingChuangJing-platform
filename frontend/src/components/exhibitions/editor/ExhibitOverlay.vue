<template>
  <div class="pointer-events-none absolute inset-0">
    <div
      v-for="item in items"
      :key="item.exhibitId"
      class="pointer-events-auto absolute overflow-hidden rounded-lg border-2 bg-white/40 backdrop-blur-sm transition-shadow"
      :class="[
        item.exhibitId === selectedExhibitId ? 'border-brand-500 shadow-lg ring-2 ring-brand-200' : 'border-slate-200/70 hover:border-brand-400 hover:shadow-md',
        dragging?.exhibitId === item.exhibitId ? 'cursor-grabbing' : 'cursor-grab',
      ]"
      :style="item.style"
      :title="item.title"
      @pointerdown="onBodyPointerDown($event, item)"
    >
      <img
        v-if="item.coverUrl"
        :src="item.coverUrl"
        class="pointer-events-none h-full w-full object-cover"
        alt=""
        draggable="false"
      />
      <div
        v-else
        class="pointer-events-none flex h-full w-full flex-col items-center justify-center gap-1 bg-gradient-to-br from-slate-100 to-slate-200 p-2 text-slate-600"
      >
        <span class="text-[10px] uppercase tracking-wider text-slate-400">{{ item.typeLabel }}</span>
        <span class="line-clamp-2 text-center text-xs font-medium leading-tight">{{ item.title }}</span>
      </div>

      <span
        class="pointer-events-none absolute left-1 top-1 rounded bg-black/55 px-1.5 py-0.5 text-[10px] font-medium text-white"
      >{{ item.typeLabel }}</span>

      <span
        class="pointer-events-none absolute inset-x-0 bottom-0 truncate bg-gradient-to-t from-black/70 to-transparent px-2 py-1 text-left text-[11px] font-medium text-white"
      >{{ item.title }}</span>

      <!-- 四角缩放 handle（仅选中时显示） -->
      <template v-if="item.exhibitId === selectedExhibitId">
        <span
          v-for="corner in CORNERS"
          :key="corner"
          class="absolute h-3 w-3 rounded-full border-2 border-white bg-brand-500 shadow"
          :class="cornerClass(corner)"
          @pointerdown.stop="onCornerPointerDown($event, item, corner)"
        />
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { ExhibitDetail, SlotConfig } from '@/api/types'

const props = defineProps<{
  exhibits: ExhibitDetail[]
  slots: SlotConfig[]
  zoom: number
  selectedExhibitId?: number | null
}>()

const emit = defineEmits<{
  select: [id: number]
  'update-placement': [id: number, placement: { x: number; y: number; w: number; h: number }]
}>()

const TYPE_LABELS: Record<string, string> = {
  image: '图片',
  video: '视频',
  audio: '音频',
  text: '文本',
  '3d': '3D',
  artifact: '文物',
  document: '文档',
}

const LOGICAL_W = 1920
const LOGICAL_H = 1080
const DRAG_THRESHOLD_PX = 3
const MIN_SIZE_PCT = 3 // 最小 3% 避免拖成一个点

type Corner = 'tl' | 'tr' | 'bl' | 'br'
const CORNERS: Corner[] = ['tl', 'tr', 'bl', 'br']

interface Box { x: number; y: number; w: number; h: number }
interface Item extends Box {
  exhibitId: number
  title: string
  coverUrl: string | null
  typeLabel: string
  style: Record<string, string>
}

// 拖拽期间的本地 box 覆盖，避免每帧后端回写
// key: exhibitId, value: { x, y, w, h } 百分比
const overrides = reactive<Map<number, Box>>(new Map())

const dragging = ref<{
  exhibitId: number
  mode: 'move' | 'resize'
  corner?: Corner
  startClientX: number
  startClientY: number
  startBox: Box
  didDrag: boolean
} | null>(null)

function baseBoxFor(ex: ExhibitDetail): Box | null {
  const slotMap = new Map(props.slots.map(s => [s.code, s]))
  if (ex.placementMode === 'slot' && ex.slotCode) {
    const s = slotMap.get(ex.slotCode)
    if (!s) return null
    return { x: s.x, y: s.y, w: s.w, h: s.h }
  }
  const p = (ex.placementJson ?? {}) as Record<string, unknown>
  return {
    x: typeof p.x === 'number' ? p.x : 35,
    y: typeof p.y === 'number' ? p.y : 35,
    w: typeof p.w === 'number' ? p.w : 30,
    h: typeof p.h === 'number' ? p.h : 30,
  }
}

const items = computed<Item[]>(() => {
  return props.exhibits
    .map<Item | null>(ex => {
      const base = baseBoxFor(ex)
      if (!base) return null
      const ov = overrides.get(ex.id)
      const { x, y, w, h } = ov ?? base
      const z = props.zoom
      return {
        exhibitId: ex.id,
        title: ex.title,
        coverUrl: ex.coverUrl ?? ex.mediaUrl ?? null,
        typeLabel: TYPE_LABELS[ex.exhibitType] ?? ex.exhibitType,
        x, y, w, h,
        style: {
          left: `${(x / 100) * LOGICAL_W * z}px`,
          top: `${(y / 100) * LOGICAL_H * z}px`,
          width: `${(w / 100) * LOGICAL_W * z}px`,
          height: `${(h / 100) * LOGICAL_H * z}px`,
        },
      }
    })
    .filter((it): it is Item => it !== null)
})

function cornerClass(corner: Corner): string {
  // handle 居中对齐到角点（关于边框），大小 12px → offset -6px
  switch (corner) {
    case 'tl': return 'left-0 top-0 -translate-x-1/2 -translate-y-1/2 cursor-nwse-resize'
    case 'tr': return 'right-0 top-0 translate-x-1/2 -translate-y-1/2 cursor-nesw-resize'
    case 'bl': return 'left-0 bottom-0 -translate-x-1/2 translate-y-1/2 cursor-nesw-resize'
    case 'br': return 'right-0 bottom-0 translate-x-1/2 translate-y-1/2 cursor-nwse-resize'
  }
}

function onBodyPointerDown(e: PointerEvent, item: Item) {
  if (e.button !== 0) return
  dragging.value = {
    exhibitId: item.exhibitId,
    mode: 'move',
    startClientX: e.clientX,
    startClientY: e.clientY,
    startBox: { x: item.x, y: item.y, w: item.w, h: item.h },
    didDrag: false,
  }
  window.addEventListener('pointermove', onPointerMove)
  window.addEventListener('pointerup', onPointerUp, { once: true })
}

function onCornerPointerDown(e: PointerEvent, item: Item, corner: Corner) {
  if (e.button !== 0) return
  dragging.value = {
    exhibitId: item.exhibitId,
    mode: 'resize',
    corner,
    startClientX: e.clientX,
    startClientY: e.clientY,
    startBox: { x: item.x, y: item.y, w: item.w, h: item.h },
    didDrag: false,
  }
  window.addEventListener('pointermove', onPointerMove)
  window.addEventListener('pointerup', onPointerUp, { once: true })
}

function onPointerMove(e: PointerEvent) {
  const d = dragging.value
  if (!d) return
  const dx = e.clientX - d.startClientX
  const dy = e.clientY - d.startClientY
  if (!d.didDrag && Math.hypot(dx, dy) > DRAG_THRESHOLD_PX) d.didDrag = true
  const z = props.zoom
  const dxPct = (dx / (LOGICAL_W * z)) * 100
  const dyPct = (dy / (LOGICAL_H * z)) * 100
  const { x, y, w, h } = d.startBox
  let nx = x, ny = y, nw = w, nh = h
  if (d.mode === 'move') {
    nx = x + dxPct
    ny = y + dyPct
  } else if (d.mode === 'resize') {
    switch (d.corner) {
      case 'tl': nx = x + dxPct; ny = y + dyPct; nw = w - dxPct; nh = h - dyPct; break
      case 'tr': ny = y + dyPct; nw = w + dxPct; nh = h - dyPct; break
      case 'bl': nx = x + dxPct; nw = w - dxPct; nh = h + dyPct; break
      case 'br': nw = w + dxPct; nh = h + dyPct; break
    }
    nw = Math.max(MIN_SIZE_PCT, nw)
    nh = Math.max(MIN_SIZE_PCT, nh)
  }
  // 限制在画布范围
  nx = Math.max(0, Math.min(100 - MIN_SIZE_PCT, nx))
  ny = Math.max(0, Math.min(100 - MIN_SIZE_PCT, ny))
  if (nx + nw > 100) nw = 100 - nx
  if (ny + nh > 100) nh = 100 - ny
  overrides.set(d.exhibitId, {
    x: round2(nx), y: round2(ny), w: round2(nw), h: round2(nh),
  })
}

function onPointerUp() {
  window.removeEventListener('pointermove', onPointerMove)
  const d = dragging.value
  dragging.value = null
  if (!d) return
  if (!d.didDrag) {
    // 单纯点击 → 选中
    overrides.delete(d.exhibitId)
    emit('select', d.exhibitId)
    return
  }
  const finalBox = overrides.get(d.exhibitId)
  if (finalBox) {
    emit('update-placement', d.exhibitId, finalBox)
    // 保留 override，等上游 props.exhibits 更新后再清
    // （否则临时回跳）
  }
}

function round2(v: number): number {
  return Math.round(v * 100) / 100
}

// 上游 props.exhibits 更新后，清除对应 override（即后端返回的 placementJson 已到位）
watch(() => props.exhibits, (list) => {
  for (const id of [...overrides.keys()]) {
    const ex = list.find(e => e.id === id)
    if (!ex) { overrides.delete(id); continue }
    const base = baseBoxFor(ex)
    const ov = overrides.get(id)!
    if (base && boxEquals(base, ov, 0.5)) {
      overrides.delete(id)
    }
  }
}, { deep: true })

function boxEquals(a: Box, b: Box, eps: number): boolean {
  return Math.abs(a.x - b.x) < eps && Math.abs(a.y - b.y) < eps && Math.abs(a.w - b.w) < eps && Math.abs(a.h - b.h) < eps
}
</script>
