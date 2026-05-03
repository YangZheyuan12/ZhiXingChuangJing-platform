<template>
  <div class="flex items-center justify-center gap-4 border-t border-gray-200 bg-white px-6 py-3">
    <button
      type="button"
      :disabled="!hasPrev"
      class="rounded-lg px-4 py-2 text-sm font-medium transition disabled:cursor-not-allowed disabled:text-gray-300"
      :class="hasPrev ? 'bg-gray-100 text-gray-700 hover:bg-gray-200' : ''"
      @click="$emit('prev')"
    >
      ← 上一展区
    </button>

    <span class="text-sm text-gray-500">
      {{ currentIndex + 1 }} / {{ total }}
    </span>

    <button
      type="button"
      :disabled="!hasNext"
      class="rounded-lg px-4 py-2 text-sm font-medium transition disabled:cursor-not-allowed disabled:text-gray-300"
      :class="hasNext ? 'bg-brand-600 text-white hover:bg-brand-700' : ''"
      @click="$emit('next')"
    >
      下一展区 →
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  currentIndex: number
  total: number
}>()

defineEmits<{
  prev: []
  next: []
}>()

const hasPrev = computed(() => props.currentIndex > 0)
const hasNext = computed(() => props.currentIndex < props.total - 1)
</script>
