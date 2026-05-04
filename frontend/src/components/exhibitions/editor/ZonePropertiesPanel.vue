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

    <div>
      <span class="mb-1 block text-xs font-medium text-gray-600">展板背景图</span>
      <div class="flex items-stretch gap-1.5">
        <input
          :value="zone.backgroundUrl ?? ''"
          class="flex-1 rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
          placeholder="输入图片 URL 或点击右侧上传"
          @input="$emit('update', 'backgroundUrl', ($event.target as HTMLInputElement).value || null)"
        />
        <button
          type="button"
          :disabled="uploading"
          class="shrink-0 rounded-lg border border-gray-200 bg-white px-3 text-xs text-gray-600 transition hover:border-brand-300 hover:bg-brand-50 hover:text-brand-700 disabled:opacity-50"
          @click="triggerUpload"
        >{{ uploading ? '上传中…' : '上传' }}</button>
      </div>
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        class="hidden"
        @change="handleFileChange"
      />
      <div v-if="zone.backgroundUrl" class="relative mt-2 overflow-hidden rounded-md border border-gray-100 bg-slate-50">
        <img :src="zone.backgroundUrl" class="h-24 w-full object-cover" alt="背景预览" />
        <button
          type="button"
          class="absolute right-1 top-1 rounded bg-white/90 px-1.5 py-0.5 text-[10px] text-gray-500 hover:text-rose-600"
          title="移除背景图"
          @click="$emit('update', 'backgroundUrl', null)"
        >移除</button>
      </div>
      <p v-if="uploadError" class="mt-1 text-xs text-rose-500">{{ uploadError }}</p>
    </div>

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
import { ref } from 'vue'
import type { ZoneDetail } from '@/api/types'
import { uploadAsset } from '@/api/modules/assets'
import { getErrorMessage } from '@/utils/request'

defineProps<{
  zone: ZoneDetail | null
}>()

const emit = defineEmits<{
  update: [field: string, value: unknown]
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const uploadError = ref('')

function triggerUpload() {
  uploadError.value = ''
  fileInputRef.value?.click()
}

async function handleFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    uploadError.value = '请选择图片文件'
    return
  }
  uploading.value = true
  uploadError.value = ''
  try {
    const data = await uploadAsset(file, { bizType: 'zone-background' })
    if (data.fileUrl) {
      emit('update', 'backgroundUrl', data.fileUrl)
    }
  } catch (err) {
    uploadError.value = getErrorMessage(err, '上传失败')
  } finally {
    uploading.value = false
  }
}
</script>
