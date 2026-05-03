<template>
  <div class="pointer-events-none absolute inset-0">
    <HotspotMarker
      v-for="hs in hotspots"
      :key="hs.id"
      :hotspot="hs"
      :zoom="zoom"
      :stage-width="1920"
      :stage-height="1080"
      class="pointer-events-auto"
    />
  </div>
</template>

<script setup lang="ts">
import type { HotspotDetail } from '@/api/types'
import HotspotMarker from './HotspotMarker.vue'

defineProps<{
  hotspots: HotspotDetail[]
  zoom: number
}>()
</script>
