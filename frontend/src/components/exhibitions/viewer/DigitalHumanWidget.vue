<template>
  <div v-if="visible" class="fixed bottom-6 right-6 z-40 flex flex-col items-end gap-2">
    <div v-if="expanded" class="w-72 rounded-2xl border border-gray-200 bg-white p-4 shadow-xl">
      <div class="mb-3 flex items-center justify-between">
        <h4 class="text-sm font-semibold text-gray-800">数字人讲解</h4>
        <button type="button" class="text-xs text-gray-400 hover:text-gray-600" @click="expanded = false">收起</button>
      </div>
      <p class="text-xs leading-5 text-gray-500">数字人讲解功能将在后续版本中上线，敬请期待。</p>
    </div>
    <button
      type="button"
      class="flex h-12 w-12 items-center justify-center rounded-full bg-brand-600 text-lg text-white shadow-lg transition hover:bg-brand-700 hover:scale-105"
      title="数字人讲解"
      @click="expanded = !expanded"
    >
      🎙
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  visible: boolean
}>()

const expanded = ref(false)
</script>
