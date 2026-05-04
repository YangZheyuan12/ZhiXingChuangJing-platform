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
    <DigitalHumanOverlay
      class="z-[25]"
      :placement="digitalHumanPlacement ?? null"
      :zoom="zoom"
      :draggable="digitalHumanDraggable"
      @drag-end="(x, y) => emit('digital-human-drag-end', x, y)"
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
import type { ExhibitDetail, HotspotDetail, SlotConfig, ZoneDigitalHumanPlacement } from '@/api/types'
import SceneBackground from './SceneBackground.vue'
import HotspotOverlay from './HotspotOverlay.vue'
import ExhibitSlotOverlay from './ExhibitSlotOverlay.vue'
import DigitalHumanOverlay from './DigitalHumanOverlay.vue'

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
  digitalHumanPlacement?: ZoneDigitalHumanPlacement | null
  digitalHumanDraggable?: boolean
}>()

const emit = defineEmits<{
  'hotspot-select': [id: number]
  'hotspot-drag-end': [id: number, xPercent: number, yPercent: number]
  'digital-human-drag-end': [xPercent: number, yPercent: number]
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
