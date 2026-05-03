<template>
  <div class="space-y-1">
    <div class="mb-2 flex items-center justify-between">
      <span class="text-xs font-semibold uppercase tracking-widest text-gray-400">展品</span>
      <button type="button" class="rounded px-1.5 py-0.5 text-xs text-red-700 hover:bg-red-50" @click="$emit('add')">+ 添加</button>
    </div>
    <div
      v-for="exhibit in exhibits"
      :key="exhibit.id"
      class="flex cursor-pointer items-center gap-2 rounded-lg px-3 py-2 text-sm transition"
      :class="exhibit.id === selectedExhibitId
        ? 'bg-amber-50 text-amber-700 font-medium'
        : 'text-gray-600 hover:bg-gray-50'"
      @click="$emit('select', exhibit.id)"
    >
      <img v-if="exhibit.coverUrl" :src="exhibit.coverUrl" class="h-8 w-8 shrink-0 rounded object-cover" />
      <span v-else class="flex h-8 w-8 shrink-0 items-center justify-center rounded bg-gray-100 text-xs text-gray-400">{{ exhibitTypeIcon(exhibit.exhibitType) }}</span>
      <span class="min-w-0 flex-1 truncate">{{ exhibit.title }}</span>
      <button type="button" class="shrink-0 text-xs text-gray-300 hover:text-rose-500" @click.stop="$emit('delete', exhibit.id)">✕</button>
    </div>
    <p v-if="exhibits.length === 0" class="py-4 text-center text-xs text-gray-400">当前展区暂无展品</p>
  </div>
</template>

<script setup lang="ts">
import type { ExhibitDetail } from '@/api/types'

defineProps<{
  exhibits: ExhibitDetail[]
  selectedExhibitId: number | null
}>()

defineEmits<{
  select: [id: number]
  add: []
  delete: [id: number]
}>()

function exhibitTypeIcon(type: string): string {
  const icons: Record<string, string> = {
    image: '🖼',
    video: '🎬',
    audio: '🎵',
    document: '📄',
    model: '🧊',
    text: 'T',
  }
  return icons[type] ?? '📦'
}
</script>
