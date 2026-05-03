<template>
  <div class="space-y-1">
    <div class="mb-2 flex items-center justify-between">
      <span class="text-xs font-semibold uppercase tracking-widest text-gray-400">展区</span>
      <button type="button" class="rounded px-1.5 py-0.5 text-xs text-red-700 hover:bg-red-50" @click="$emit('add')">+ 新增</button>
    </div>
    <div
      v-for="zone in zones"
      :key="zone.id"
      class="group flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-sm transition"
      :class="zone.id === currentZoneId
        ? 'bg-red-50 text-red-700 font-medium'
        : 'text-gray-600 hover:bg-gray-50'"
    >
      <button
        type="button"
        :disabled="switching"
        class="flex min-w-0 flex-1 items-center gap-2 disabled:opacity-60"
        @click="$emit('switch', zone)"
      >
        <span class="shrink-0 text-xs">{{ zoneTypeIcon(zone.zoneType) }}</span>
        <span class="min-w-0 flex-1 truncate">{{ zone.title }}</span>
        <span class="shrink-0 text-[10px] text-gray-400">#{{ zone.sortOrder + 1 }}</span>
      </button>
      <button
        type="button"
        class="shrink-0 rounded p-0.5 text-xs text-gray-300 opacity-0 transition hover:text-rose-500 group-hover:opacity-100"
        title="删除展区"
        @click.stop="$emit('delete', zone.id)"
      >
        ✕
      </button>
    </div>
    <p v-if="zones.length === 0" class="py-4 text-center text-xs text-gray-400">暂无展区</p>
  </div>
</template>

<script setup lang="ts">
import type { ZoneDetail } from '@/api/types'

defineProps<{
  zones: ZoneDetail[]
  currentZoneId: number | null
  switching: boolean
}>()

defineEmits<{
  switch: [zone: ZoneDetail]
  add: []
  delete: [id: number]
}>()

function zoneTypeIcon(type: string): string {
  const icons: Record<string, string> = {
    entrance: '🚪',
    gallery: '🖼',
    closeup: '🔍',
    exit: '🚶',
    timeline_node: '📅',
    map_point: '📍',
  }
  return icons[type] ?? '📦'
}
</script>
