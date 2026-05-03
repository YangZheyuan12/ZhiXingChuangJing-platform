<template>
  <div
    class="absolute inset-0 overflow-hidden transition-opacity duration-300"
    :class="transitioning ? 'opacity-0' : 'opacity-100'"
  >
    <img
      v-if="backgroundUrl"
      :src="backgroundUrl"
      class="h-full w-full object-contain"
      :style="bgInlineStyle"
      alt="场景背景"
    />
    <div v-else class="flex h-full w-full items-center justify-center bg-gradient-to-br from-stone-100 to-stone-200">
      <span class="text-sm text-stone-400">暂无背景图</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  backgroundUrl: string | null
  backgroundStyle?: Record<string, unknown> | null
  transitioning?: boolean
}>()

const bgInlineStyle = computed(() => {
  const style: Record<string, string> = {}
  if (props.backgroundStyle?.opacity != null) {
    style.opacity = String(props.backgroundStyle.opacity)
  }
  return style
})
</script>
