<template>
  <div class="pointer-events-none absolute inset-0">
    <HotspotMarker
      v-for="hs in hotspots"
      :key="hs.id"
      :hotspot="hs"
      :zoom="zoom"
      :stage-width="1920"
      :stage-height="1080"
      :selected="selectedId === hs.id"
      :draggable="draggable"
      class="pointer-events-auto"
      @click="emit('select', $event)"
      @drag-end="(id, x, y) => emit('dragEnd', id, x, y)"
    />
  </div>
</template>

<script setup lang="ts">
import type { HotspotDetail } from '@/api/types'
import HotspotMarker from './HotspotMarker.vue'

defineProps<{
  hotspots: HotspotDetail[]
  zoom: number
  selectedId?: number | null
  draggable?: boolean
}>()

const emit = defineEmits<{
  select: [id: number]
  dragEnd: [id: number, xPercent: number, yPercent: number]
}>()
</script>
