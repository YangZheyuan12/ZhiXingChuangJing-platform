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
      :used-slot-codes="usedSlotCodes"
    />
    <ExhibitOverlay
      v-if="exhibits.length > 0"
      class="z-25"
      :exhibits="exhibits"
      :slots="slots"
      :zoom="zoom"
      :selected-exhibit-id="selectedExhibitId"
      @select="(id) => emit('exhibit-select', id)"
      @update-placement="(id, p) => emit('exhibit-placement', id, p)"
    />
    <HotspotOverlay
      class="z-30"
      :hotspots="hotspots"
      :zoom="zoom"
      :selected-id="selectedHotspotId"
      :draggable="hotspotDraggable"
      @select="emit('hotspot-select', $event)"
      @drag-end="(id, x, y) => emit('hotspot-drag-end', id, x, y)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ExhibitDetail, HotspotDetail, SlotConfig } from '@/api/types'
import SceneBackground from './SceneBackground.vue'
import HotspotOverlay from './HotspotOverlay.vue'
import ExhibitSlotOverlay from './ExhibitSlotOverlay.vue'
import ExhibitOverlay from './ExhibitOverlay.vue'

const props = defineProps<{
  backgroundUrl: string | null
  backgroundStyle?: Record<string, unknown> | null
  hotspots: HotspotDetail[]
  slots: SlotConfig[]
  exhibits?: ExhibitDetail[]
  zoom: number
  transitioning: boolean
  activeSlotCode?: string | null
  selectedHotspotId?: number | null
  selectedExhibitId?: number | null
  hotspotDraggable?: boolean
}>()

const emit = defineEmits<{
  'hotspot-select': [id: number]
  'hotspot-drag-end': [id: number, xPercent: number, yPercent: number]
  'exhibit-select': [id: number]
  'exhibit-placement': [id: number, placement: { x: number; y: number; w: number; h: number }]
}>()

const usedSlotCodes = computed(() =>
  (props.exhibits ?? [])
    .filter(e => e.placementMode === 'slot' && e.slotCode)
    .map(e => e.slotCode as string),
)

const stageWrapper = ref<HTMLElement | null>(null)
const canvasEl = ref<HTMLCanvasElement | null>(null)

const stageContainerStyle = computed(() => ({
  width: `${1920 * props.zoom}px`,
  height: `${1080 * props.zoom}px`,
}))

defineExpose({ canvasEl, stageWrapper })
</script>
