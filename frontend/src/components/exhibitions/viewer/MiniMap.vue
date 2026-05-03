<template>
  <div class="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
    <h3 class="border-b border-gray-100 px-3 py-2 text-xs font-semibold text-gray-500">展区导览</h3>
    <div class="flex flex-col gap-1 p-2">
      <button
        v-for="zone in zones"
        :key="zone.id"
        type="button"
        class="flex items-center gap-2 rounded-lg px-3 py-2 text-left text-sm transition"
        :class="zone.id === currentZoneId
          ? 'bg-brand-50 font-medium text-brand-700'
          : 'text-gray-600 hover:bg-gray-50'"
        @click="$emit('navigate', zone)"
      >
        <span class="inline-block h-2 w-2 rounded-full" :class="zone.id === currentZoneId ? 'bg-brand-500' : 'bg-gray-300'" />
        <span class="truncate">{{ zone.title }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ZoneDetail } from '@/api/types'

defineProps<{
  zones: ZoneDetail[]
  currentZoneId: number | null
}>()

defineEmits<{
  navigate: [zone: ZoneDetail]
}>()
</script>
