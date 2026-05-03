<template>
  <div class="pointer-events-none absolute inset-0">
    <button
      v-for="hotspot in hotspots"
      :key="hotspot.id"
      type="button"
      class="pointer-events-auto absolute flex items-center justify-center rounded-lg border-2 border-white/80 bg-brand-600/80 text-xs font-medium text-white shadow-lg transition hover:bg-brand-700 hover:scale-105"
      :style="{
        left: `${hotspot.xPercent}%`,
        top: `${hotspot.yPercent}%`,
        width: `${hotspot.wPercent}%`,
        height: `${hotspot.hPercent}%`,
      }"
      :title="hotspot.label ?? '热点'"
      @click="$emit('navigate', hotspot)"
    >
      <span v-if="hotspot.icon" class="mr-1">{{ hotspot.icon }}</span>
      <span v-if="hotspot.label">{{ hotspot.label }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import type { HotspotDetail } from '@/api/types'

defineProps<{
  hotspots: HotspotDetail[]
}>()

defineEmits<{
  navigate: [hotspot: HotspotDetail]
}>()
</script>
