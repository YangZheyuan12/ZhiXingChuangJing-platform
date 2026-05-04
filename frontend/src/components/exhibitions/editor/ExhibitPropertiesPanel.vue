<template>
  <div v-if="exhibit" class="space-y-4">
    <h3 class="text-xs font-semibold uppercase tracking-widest text-gray-400">展品属性</h3>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">标题</span>
      <input
        :value="exhibit.title"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        @change="emitUpdate('title', ($event.target as HTMLInputElement).value)"
      />
    </label>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">副标题</span>
      <input
        :value="exhibit.subtitle ?? ''"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        @change="emitUpdate('subtitle', ($event.target as HTMLInputElement).value || null)"
      />
    </label>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">类型</span>
      <select
        :value="exhibit.exhibitType"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        @change="emitUpdate('exhibitType', ($event.target as HTMLSelectElement).value)"
      >
        <option value="image">图片</option>
        <option value="video">视频</option>
        <option value="audio">音频</option>
        <option value="document">文档</option>
        <option value="model">3D模型</option>
        <option value="text">文本</option>
      </select>
    </label>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">描述</span>
      <textarea
        :value="exhibit.description ?? ''"
        rows="3"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        @change="emitUpdate('description', ($event.target as HTMLTextAreaElement).value || null)"
      />
    </label>

    <div>
      <span class="mb-1 block text-xs font-medium text-gray-600">封面图</span>
      <div class="flex items-stretch gap-1.5">
        <input
          :value="exhibit.coverUrl ?? ''"
          class="flex-1 rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
          placeholder="输入 URL 或上传图片"
          @change="emitUpdate('coverUrl', ($event.target as HTMLInputElement).value || null)"
        />
        <button
          type="button"
          :disabled="coverUploading"
          class="shrink-0 rounded-lg border border-gray-200 bg-white px-3 text-xs text-gray-600 transition hover:border-brand-300 hover:bg-brand-50 hover:text-brand-700 disabled:opacity-50"
          @click="triggerCoverUpload"
        >{{ coverUploading ? '上传中…' : '上传' }}</button>
      </div>
      <input
        ref="coverFileInputRef"
        type="file"
        accept="image/*"
        class="hidden"
        @change="handleCoverFileChange"
      />
      <div v-if="exhibit.coverUrl" class="mt-2 overflow-hidden rounded-md border border-gray-100 bg-slate-50">
        <img :src="exhibit.coverUrl" class="h-20 w-full object-cover" alt="封面预览" />
      </div>
      <p v-if="coverUploadError" class="mt-1 text-xs text-rose-500">{{ coverUploadError }}</p>
    </div>

    <div>
      <span class="mb-1 block text-xs font-medium text-gray-600">媒体文件</span>
      <div class="flex items-stretch gap-1.5">
        <input
          :value="exhibit.mediaUrl ?? ''"
          class="flex-1 rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
          placeholder="输入 URL 或上传文件"
          @change="emitUpdate('mediaUrl', ($event.target as HTMLInputElement).value || null)"
        />
        <button
          type="button"
          :disabled="mediaUploading"
          class="shrink-0 rounded-lg border border-gray-200 bg-white px-3 text-xs text-gray-600 transition hover:border-brand-300 hover:bg-brand-50 hover:text-brand-700 disabled:opacity-50"
          @click="triggerMediaUpload"
        >{{ mediaUploading ? '上传中…' : '上传' }}</button>
      </div>
      <input
        ref="mediaFileInputRef"
        type="file"
        :accept="mediaAccept"
        class="hidden"
        @change="handleMediaFileChange"
      />
      <p v-if="mediaUploadError" class="mt-1 text-xs text-rose-500">{{ mediaUploadError }}</p>
    </div>

    <div>
      <div class="mb-2 flex items-center justify-between">
        <span class="text-xs font-medium text-gray-600">讲解词 ({{ exhibit.narrations.length }})</span>
        <div class="flex items-center gap-2">
          <button
            type="button"
            class="inline-flex items-center gap-1 rounded-md bg-brand-50 px-2 py-0.5 text-xs text-brand-700 transition hover:bg-brand-100"
            title="用 AI 快速生成讲解词"
            @click="aiModalVisible = true"
          >
            <svg class="h-3 w-3" viewBox="0 0 20 20" fill="currentColor">
              <path d="M10 3.5a.75.75 0 01.75.75v1.5a.75.75 0 01-1.5 0v-1.5A.75.75 0 0110 3.5zM10 14.25a.75.75 0 01.75.75v1.5a.75.75 0 01-1.5 0v-1.5a.75.75 0 01.75-.75zM3.5 10a.75.75 0 01.75-.75h1.5a.75.75 0 010 1.5h-1.5A.75.75 0 013.5 10zM14.25 10a.75.75 0 01.75-.75h1.5a.75.75 0 010 1.5h-1.5a.75.75 0 01-.75-.75zM5.404 5.404a.75.75 0 011.06 0l1.06 1.061a.75.75 0 11-1.06 1.06l-1.06-1.06a.75.75 0 010-1.061zM12.475 12.475a.75.75 0 011.06 0l1.061 1.06a.75.75 0 11-1.06 1.061l-1.061-1.06a.75.75 0 010-1.061zM14.596 5.404a.75.75 0 010 1.06l-1.06 1.06a.75.75 0 11-1.061-1.06l1.06-1.06a.75.75 0 011.061 0zM7.525 12.475a.75.75 0 010 1.06l-1.06 1.061a.75.75 0 11-1.06-1.06l1.06-1.061a.75.75 0 011.06 0z" />
            </svg>
            AI 生成
          </button>
          <button type="button" class="text-xs text-red-700 hover:underline" @click="$emit('add-narration')">+ 添加</button>
        </div>
      </div>
      <div v-for="n in exhibit.narrations" :key="n.id" class="mb-1 rounded border border-gray-100 bg-gray-50 px-2 py-1.5 text-xs text-gray-600">
        <span class="font-medium">{{ n.narrationType }}</span>: {{ n.content.slice(0, 60) }}{{ n.content.length > 60 ? '...' : '' }}
      </div>
    </div>

    <NarrationGeneratorModal
      :visible="aiModalVisible"
      :initial-title="exhibit.title"
      :initial-description="exhibit.description ?? ''"
      @close="aiModalVisible = false"
      @use="handleUseAiNarration"
    />

    <div>
      <div class="mb-2 flex items-center justify-between">
        <span class="text-xs font-medium text-gray-600">互动题 ({{ exhibit.interactions.length }})</span>
        <button type="button" class="text-xs text-red-700 hover:underline" @click="$emit('add-interaction')">+ 添加</button>
      </div>
      <div v-for="q in exhibit.interactions" :key="q.id" class="mb-1 rounded border border-gray-100 bg-gray-50 px-2 py-1.5 text-xs text-gray-600">
        <span class="font-medium">{{ q.interactionType }}</span>: {{ q.questionText.slice(0, 60) }}{{ q.questionText.length > 60 ? '...' : '' }}
      </div>
    </div>
  </div>
  <div v-else class="flex items-center justify-center py-10 text-xs text-gray-400">
    请选择一个展品
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ExhibitDetail } from '@/api/types'
import NarrationGeneratorModal from './NarrationGeneratorModal.vue'
import { uploadAsset } from '@/api/modules/assets'
import { getErrorMessage } from '@/utils/request'

const props = defineProps<{
  exhibit: ExhibitDetail | null
}>()

const emit = defineEmits<{
  update: [field: string, value: unknown]
  'add-narration': []
  'add-interaction': []
  'ai-narration': [narration: string, suggestions: string[]]
}>()

const aiModalVisible = ref(false)

const coverFileInputRef = ref<HTMLInputElement | null>(null)
const mediaFileInputRef = ref<HTMLInputElement | null>(null)
const coverUploading = ref(false)
const mediaUploading = ref(false)
const coverUploadError = ref('')
const mediaUploadError = ref('')

const mediaAccept = computed(() => {
  switch (props.exhibit?.exhibitType) {
    case 'image': return 'image/*'
    case 'video': return 'video/*'
    case 'audio': return 'audio/*'
    case 'document': return '.pdf,.doc,.docx,.txt,.md'
    case 'model': return '.glb,.gltf,.obj,.fbx'
    default: return '*/*'
  }
})

function emitUpdate(field: string, value: unknown) {
  emit('update', field, value)
}

function handleUseAiNarration(narration: string, suggestions: string[]) {
  emit('ai-narration', narration, suggestions)
}

function triggerCoverUpload() {
  coverUploadError.value = ''
  coverFileInputRef.value?.click()
}

async function handleCoverFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    coverUploadError.value = '请选择图片文件'
    return
  }
  coverUploading.value = true
  coverUploadError.value = ''
  try {
    const data = await uploadAsset(file, { bizType: 'exhibit-cover' })
    if (data.fileUrl) emit('update', 'coverUrl', data.fileUrl)
  } catch (err) {
    coverUploadError.value = getErrorMessage(err, '上传失败')
  } finally {
    coverUploading.value = false
  }
}

function triggerMediaUpload() {
  mediaUploadError.value = ''
  mediaFileInputRef.value?.click()
}

async function handleMediaFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  mediaUploading.value = true
  mediaUploadError.value = ''
  try {
    const data = await uploadAsset(file, { bizType: 'exhibit-media' })
    if (data.fileUrl) {
      emit('update', 'mediaUrl', data.fileUrl)
      // 图片类型同步到 coverUrl
      if (props.exhibit?.exhibitType === 'image') {
        emit('update', 'coverUrl', data.fileUrl)
      }
    }
  } catch (err) {
    mediaUploadError.value = getErrorMessage(err, '上传失败')
  } finally {
    mediaUploading.value = false
  }
}
</script>
