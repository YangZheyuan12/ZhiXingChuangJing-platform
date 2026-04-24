<template>
  <div class="space-y-1">
    <div class="flex items-center justify-between pb-2">
      <h4 class="text-xs font-semibold uppercase tracking-widest text-gray-400">图层</h4>
      <span class="text-[10px] text-gray-300">{{ layers.length }} 个对象</span>
    </div>

    <p v-if="layers.length === 0" class="text-xs text-gray-400">暂无画布对象。</p>

    <div
      v-for="(layer, idx) in layers"
      :key="idx"
      class="group flex items-center gap-2 rounded-lg border px-2.5 py-2 text-xs transition cursor-pointer"
      :class="layer.active ? 'border-brand-300 bg-brand-50 text-brand-700' : 'border-gray-100 bg-white text-gray-600 hover:border-gray-200 hover:bg-gray-50'"
      @click="$emit('select', idx)"
    >
      <span class="shrink-0 text-sm text-gray-300">{{ layer.icon }}</span>
      <span class="min-w-0 flex-1 truncate">{{ layer.label }}</span>

      <div class="flex shrink-0 items-center gap-0.5 opacity-0 transition group-hover:opacity-100">
        <button type="button" class="layer-btn" title="可见性" @click.stop="$emit('toggle-visible', idx)">
          {{ layer.visible ? '👁' : '🚫' }}
        </button>
        <button type="button" class="layer-btn" title="上移" :disabled="idx === 0" @click.stop="$emit('move-up', idx)">↑</button>
        <button type="button" class="layer-btn" title="下移" :disabled="idx === layers.length - 1" @click.stop="$emit('move-down', idx)">↓</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
export interface LayerItem {
  icon: string
  label: string
  active: boolean
  visible: boolean
}

defineProps<{
  layers: LayerItem[]
}>()

defineEmits<{
  select: [index: number]
  'toggle-visible': [index: number]
  'move-up': [index: number]
  'move-down': [index: number]
}>()
</script>

<style scoped>
.layer-btn {
  @apply rounded px-1 py-0.5 text-[10px] transition hover:bg-gray-200 disabled:opacity-30;
}
</style>
