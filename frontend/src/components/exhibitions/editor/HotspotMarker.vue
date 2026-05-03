<template>
  <div
    class="absolute flex items-center justify-center rounded-full border-2 border-white/80 bg-red-700/70 text-white shadow-lg backdrop-blur-sm transition-transform hover:scale-110 cursor-pointer"
    :style="positionStyle"
    :title="hotspot.label || hotspot.hotspotType"
  >
    <span class="text-xs font-bold">{{ iconText }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { HotspotDetail } from '@/api/types'

const props = defineProps<{
  hotspot: HotspotDetail
  zoom: number
  stageWidth: number
  stageHeight: number
}>()

const positionStyle = computed(() => {
  const z = props.zoom
  return {
    left: `${(props.hotspot.xPercent / 100) * props.stageWidth * z}px`,
    top: `${(props.hotspot.yPercent / 100) * props.stageHeight * z}px`,
    width: `${(props.hotspot.wPercent / 100) * props.stageWidth * z}px`,
    height: `${(props.hotspot.hPercent / 100) * props.stageHeight * z}px`,
  }
})

const iconText = computed(() => {
  const icons: Record<string, string> = {
    navigation: '→',
    exhibit_popup: '🖼',
    external_link: '🔗',
    narration_trigger: '🎙',
  }
  return icons[props.hotspot.hotspotType] ?? '•'
})
</script>
