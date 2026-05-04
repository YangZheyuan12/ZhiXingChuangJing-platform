<template>
  <div class="pointer-events-none absolute inset-0">
    <button
      v-for="item in items"
      :key="item.exhibitId"
      type="button"
      class="pointer-events-auto absolute overflow-hidden rounded-lg border-2 bg-white/40 backdrop-blur-sm transition group"
      :class="item.exhibitId === selectedExhibitId ? 'border-brand-500 shadow-lg ring-2 ring-brand-200' : 'border-slate-200/70 hover:border-brand-400 hover:shadow-md'"
      :style="item.style"
      :title="item.title"
      @click.stop="emit('select', item.exhibitId)"
    >
      <img
        v-if="item.coverUrl"
        :src="item.coverUrl"
        class="h-full w-full object-cover"
        alt=""
        draggable="false"
      />
      <div
        v-else
        class="flex h-full w-full flex-col items-center justify-center gap-1 bg-gradient-to-br from-slate-100 to-slate-200 p-2 text-slate-600"
      >
        <span class="text-[10px] uppercase tracking-wider text-slate-400">{{ item.typeLabel }}</span>
        <span class="line-clamp-2 text-center text-xs font-medium leading-tight">{{ item.title }}</span>
      </div>

      <span
        class="absolute left-1 top-1 rounded bg-black/55 px-1.5 py-0.5 text-[10px] font-medium text-white"
      >{{ item.typeLabel }}</span>

      <span
        class="absolute inset-x-0 bottom-0 truncate bg-gradient-to-t from-black/70 to-transparent px-2 py-1 text-left text-[11px] font-medium text-white"
      >{{ item.title }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ExhibitDetail, SlotConfig } from '@/api/types'

const props = defineProps<{
  exhibits: ExhibitDetail[]
  slots: SlotConfig[]
  zoom: number
  selectedExhibitId?: number | null
}>()

const emit = defineEmits<{
  select: [id: number]
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

const items = computed(() => {
  const slotMap = new Map(props.slots.map(s => [s.code, s]))
  return props.exhibits
    .map(ex => {
      let x = 0
      let y = 0
      let w = 0
      let h = 0

      if (ex.placementMode === 'slot' && ex.slotCode) {
        const s = slotMap.get(ex.slotCode)
        if (!s) return null
        x = s.x
        y = s.y
        w = s.w
        h = s.h
      } else {
        // freeform：从 placementJson 读取百分比，缺省给一个中心区域占位
        const p = (ex.placementJson ?? {}) as Record<string, unknown>
        x = typeof p.x === 'number' ? p.x : 35
        y = typeof p.y === 'number' ? p.y : 35
        w = typeof p.w === 'number' ? p.w : 30
        h = typeof p.h === 'number' ? p.h : 30
      }

      const z = props.zoom
      return {
        exhibitId: ex.id,
        title: ex.title,
        coverUrl: ex.coverUrl ?? ex.mediaUrl ?? null,
        typeLabel: TYPE_LABELS[ex.exhibitType] ?? ex.exhibitType,
        style: {
          left: `${(x / 100) * LOGICAL_W * z}px`,
          top: `${(y / 100) * LOGICAL_H * z}px`,
          width: `${(w / 100) * LOGICAL_W * z}px`,
          height: `${(h / 100) * LOGICAL_H * z}px`,
        } as Record<string, string>,
      }
    })
    .filter((it): it is NonNullable<typeof it> => it !== null)
})
</script>
