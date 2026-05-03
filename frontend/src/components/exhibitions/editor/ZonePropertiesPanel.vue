<template>
  <div v-if="zone" class="space-y-4">
    <h3 class="text-xs font-semibold uppercase tracking-widest text-gray-400">展区属性</h3>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">标题</span>
      <input
        :value="zone.title"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        @input="$emit('update', 'title', ($event.target as HTMLInputElement).value)"
      />
    </label>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">副标题</span>
      <input
        :value="zone.subtitle ?? ''"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        @input="$emit('update', 'subtitle', ($event.target as HTMLInputElement).value || null)"
      />
    </label>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">过渡方式</span>
      <select
        :value="zone.transitionIn"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        @change="$emit('update', 'transitionIn', ($event.target as HTMLSelectElement).value)"
      >
        <option value="fade">淡入</option>
        <option value="slide-left">左滑</option>
        <option value="slide-right">右滑</option>
        <option value="zoom-in">放大进入</option>
      </select>
    </label>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">背景图 URL</span>
      <input
        :value="zone.backgroundUrl ?? ''"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        placeholder="输入图片 URL 或上传"
        @input="$emit('update', 'backgroundUrl', ($event.target as HTMLInputElement).value || null)"
      />
    </label>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">讲解词</span>
      <textarea
        :value="zone.narrationText ?? ''"
        rows="4"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        placeholder="输入该展区的讲解词"
        @input="$emit('update', 'narrationText', ($event.target as HTMLTextAreaElement).value || null)"
      />
    </label>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">描述</span>
      <textarea
        :value="zone.description ?? ''"
        rows="3"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        placeholder="展区描述"
        @input="$emit('update', 'description', ($event.target as HTMLTextAreaElement).value || null)"
      />
    </label>
  </div>
  <div v-else class="flex items-center justify-center py-10 text-xs text-gray-400">
    请选择一个展区
  </div>
</template>

<script setup lang="ts">
import type { ZoneDetail } from '@/api/types'

defineProps<{
  zone: ZoneDetail | null
}>()

defineEmits<{
  update: [field: string, value: unknown]
}>()
</script>
