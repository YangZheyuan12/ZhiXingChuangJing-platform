<template>
  <div class="flex items-center gap-2 overflow-x-auto border-t border-gray-200 bg-white px-4 py-2">
    <button
      v-for="zone in zones"
      :key="zone.id"
      type="button"
      class="shrink-0 rounded-lg border-2 px-4 py-2 text-xs transition"
      :class="zone.id === currentZoneId
        ? 'border-red-700 bg-red-50 text-red-700 font-medium'
        : 'border-transparent bg-gray-50 text-gray-500 hover:bg-gray-100'"
      @click="$emit('switch', zone)"
    >
      {{ zone.title }}
    </button>
    <button
      type="button"
      class="shrink-0 rounded-lg border-2 border-dashed border-gray-200 px-4 py-2 text-xs text-gray-400 hover:border-gray-300 hover:text-gray-500"
      @click="$emit('add')"
    >
      + 添加展区
    </button>
  </div>
</template>

<script setup lang="ts">
import type { ZoneDetail } from '@/api/types'

defineProps<{
  zones: ZoneDetail[]
  currentZoneId: number | null
}>()

defineEmits<{
  switch: [zone: ZoneDetail]
  add: []
}>()
</script>
