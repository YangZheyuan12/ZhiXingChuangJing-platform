<template>
  <div ref="stageWrapper" class="relative overflow-hidden" :style="stageContainerStyle">
    <SceneBackground
      :background-url="backgroundUrl"
      :background-style="backgroundStyle"
      :transitioning="transitioning"
    />
    <div class="absolute inset-0 z-10">
      <canvas ref="canvasEl" />
    </div>
    <ExhibitSlotOverlay
      v-if="slots.length > 0"
      class="z-20"
      :slots="slots"
      :zoom="zoom"
      :active-slot-code="activeSlotCode"
    />
    <HotspotOverlay
      class="z-30"
      :hotspots="hotspots"
      :zoom="zoom"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { HotspotDetail, SlotConfig } from '@/api/types'
import SceneBackground from './SceneBackground.vue'
import HotspotOverlay from './HotspotOverlay.vue'
import ExhibitSlotOverlay from './ExhibitSlotOverlay.vue'

const props = defineProps<{
  backgroundUrl: string | null
  backgroundStyle?: Record<string, unknown> | null
  hotspots: HotspotDetail[]
  slots: SlotConfig[]
  zoom: number
  transitioning: boolean
  activeSlotCode?: string | null
}>()

const stageWrapper = ref<HTMLElement | null>(null)
const canvasEl = ref<HTMLCanvasElement | null>(null)

const stageContainerStyle = computed(() => ({
  width: `${1920 * props.zoom}px`,
  height: `${1080 * props.zoom}px`,
}))

defineExpose({ canvasEl, stageWrapper })
</script>
