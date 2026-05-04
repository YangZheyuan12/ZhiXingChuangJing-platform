<template>
  <div v-if="placement" class="pointer-events-none absolute inset-0">
    <DigitalHumanMarker
      :placement="placement"
      :zoom="zoom"
      :stage-width="1920"
      :stage-height="1080"
      :selected="selected"
      :draggable="draggable"
      class="pointer-events-auto"
      @drag-end="(x, y) => emit('dragEnd', x, y)"
    />
  </div>
</template>

<script setup lang="ts">
import type { ZoneDigitalHumanPlacement } from '@/api/types'
import DigitalHumanMarker from './DigitalHumanMarker.vue'

defineProps<{
  placement: ZoneDigitalHumanPlacement | null
  zoom: number
  selected?: boolean
  draggable?: boolean
}>()

const emit = defineEmits<{
  dragEnd: [xPercent: number, yPercent: number]
}>()
</script>
