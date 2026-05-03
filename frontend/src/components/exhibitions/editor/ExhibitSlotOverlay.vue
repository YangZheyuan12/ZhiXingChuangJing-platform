<template>
  <div class="pointer-events-none absolute inset-0">
    <div
      v-for="slot in slots"
      :key="slot.code"
      class="absolute rounded-lg border-2 border-dashed transition-colors"
      :class="slot.code === activeSlotCode ? 'border-red-700 bg-red-700/10' : 'border-gray-300/50 bg-gray-100/20'"
      :style="slotStyle(slot)"
      :title="slot.label"
    >
      <span class="absolute left-1 top-0.5 text-[10px] text-gray-400">{{ slot.label }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SlotConfig } from '@/api/types'

const props = defineProps<{
  slots: SlotConfig[]
  zoom: number
  activeSlotCode?: string | null
}>()

function slotStyle(slot: SlotConfig): Record<string, string> {
  const z = props.zoom
  return {
    left: `${(slot.x / 100) * 1920 * z}px`,
    top: `${(slot.y / 100) * 1080 * z}px`,
    width: `${(slot.w / 100) * 1920 * z}px`,
    height: `${(slot.h / 100) * 1080 * z}px`,
  }
}
</script>
