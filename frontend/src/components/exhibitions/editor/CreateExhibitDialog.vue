<template>
  <Teleport to="body">
    <Transition
      enter-active-class="duration-150 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="duration-100 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="visible"
        class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 backdrop-blur-sm"
        @click.self="handleCancel"
      >
        <div class="w-[32rem] rounded-2xl bg-white p-6 shadow-2xl">
          <div class="flex items-start justify-between">
            <div>
              <h3 class="text-lg font-semibold text-slate-900">添加展品</h3>
              <p class="mt-1 text-sm text-slate-500">
                当前展区：<span class="font-medium text-slate-700">{{ zoneTitle }}</span>
              </p>
            </div>
            <button
              type="button"
              class="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100"
              @click="handleCancel"
              aria-label="关闭"
            >
              <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4.28 3.22a.75.75 0 00-1.06 1.06L8.94 10l-5.72 5.72a.75.75 0 101.06 1.06L10 11.06l5.72 5.72a.75.75 0 101.06-1.06L11.06 10l5.72-5.72a.75.75 0 00-1.06-1.06L10 8.94 4.28 3.22z" clip-rule="evenodd" />
              </svg>
            </button>
          </div>

          <div class="mt-5 space-y-4">
            <label class="block">
              <span class="text-sm font-medium text-slate-700">展品名称<span class="text-rose-500">*</span></span>
              <input
                v-model="form.title"
                type="text"
                :disabled="submitting"
                maxlength="120"
                class="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
                placeholder="例如：青花瓷香炉"
              />
            </label>

            <div class="grid grid-cols-2 gap-3">
              <label class="block">
                <span class="text-sm font-medium text-slate-700">类型</span>
                <select
                  v-model="form.exhibitType"
                  :disabled="submitting"
                  class="mt-1.5 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
                >
                  <option value="image">图片</option>
                  <option value="video">视频</option>
                  <option value="audio">音频</option>
                  <option value="document">文档</option>
                  <option value="model">3D 模型</option>
                  <option value="text">纯文本</option>
                </select>
              </label>
              <label class="block">
                <span class="text-sm font-medium text-slate-700">位置模式</span>
                <select
                  v-model="form.placementMode"
                  :disabled="submitting"
                  class="mt-1.5 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
                >
                  <option value="slot">插槽（slot）</option>
                  <option value="freeform">自由位置</option>
                </select>
              </label>
            </div>

            <label v-if="form.placementMode === 'slot'" class="block">
              <span class="text-sm font-medium text-slate-700">目标插槽</span>
              <select
                v-model="form.slotCode"
                :disabled="submitting || !availableSlots.length"
                class="mt-1.5 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
              >
                <option :value="null">（不绑定插槽）</option>
                <option v-for="slot in availableSlots" :key="slot.code" :value="slot.code">
                  {{ slot.label || slot.code }}
                </option>
              </select>
              <span v-if="!availableSlots.length" class="mt-1 block text-xs text-slate-400">
                当前展区暂未配置插槽
              </span>
            </label>

            <label class="block">
              <span class="text-sm font-medium text-slate-700">副标题 <span class="text-slate-400">（选填）</span></span>
              <input
                v-model="form.subtitle"
                type="text"
                :disabled="submitting"
                maxlength="200"
                class="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
              />
            </label>

            <div>
              <span class="text-sm font-medium text-slate-700">
                媒体文件 <span class="text-slate-400">（{{ mediaHint }}，选填）</span>
              </span>
              <div class="mt-1.5 flex items-stretch gap-1.5">
                <input
                  v-model="form.mediaUrl"
                  type="text"
                  :disabled="submitting || mediaUploading"
                  class="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
                  placeholder="https://... 或点击右侧上传"
                />
                <button
                  type="button"
                  :disabled="submitting || mediaUploading"
                  class="shrink-0 rounded-lg border border-slate-300 bg-white px-3 text-sm text-slate-600 transition hover:border-brand-300 hover:bg-brand-50 hover:text-brand-700 disabled:opacity-50"
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
              <div v-if="form.mediaUrl && form.exhibitType === 'image'" class="mt-2 overflow-hidden rounded-md border border-slate-100">
                <img :src="form.mediaUrl" class="h-24 w-full object-cover" alt="媒体预览" />
              </div>
              <p v-if="mediaUploadError" class="mt-1 text-xs text-rose-500">{{ mediaUploadError }}</p>
            </div>

            <div v-if="form.exhibitType !== 'image' && form.exhibitType !== 'text'">
              <span class="text-sm font-medium text-slate-700">
                封面图 <span class="text-slate-400">（选填，仅作为画布预览）</span>
              </span>
              <div class="mt-1.5 flex items-stretch gap-1.5">
                <input
                  v-model="form.coverUrl"
                  type="text"
                  :disabled="submitting || coverUploading"
                  class="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
                  placeholder="https://... 或点击右侧上传图片"
                />
                <button
                  type="button"
                  :disabled="submitting || coverUploading"
                  class="shrink-0 rounded-lg border border-slate-300 bg-white px-3 text-sm text-slate-600 transition hover:border-brand-300 hover:bg-brand-50 hover:text-brand-700 disabled:opacity-50"
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
              <div v-if="form.coverUrl" class="mt-2 overflow-hidden rounded-md border border-slate-100">
                <img :src="form.coverUrl" class="h-20 w-full object-cover" alt="封面预览" />
              </div>
              <p v-if="coverUploadError" class="mt-1 text-xs text-rose-500">{{ coverUploadError }}</p>
            </div>

            <label class="block">
              <span class="text-sm font-medium text-slate-700">简介 <span class="text-slate-400">（选填）</span></span>
              <textarea
                v-model="form.description"
                rows="3"
                :disabled="submitting"
                maxlength="500"
                class="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
              />
            </label>
          </div>

          <div class="mt-6 flex justify-end gap-3">
            <button
              type="button"
              class="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100 disabled:opacity-50"
              :disabled="submitting"
              @click="handleCancel"
            >
              取消
            </button>
            <button
              type="button"
              class="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700 disabled:opacity-60"
              :disabled="!canSubmit || submitting"
              @click="handleSubmit"
            >
              {{ submitting ? '创建中...' : '确认创建' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { CreateExhibitRequest, SlotConfig } from '@/api/types'
import { uploadAsset } from '@/api/modules/assets'
import { getErrorMessage } from '@/utils/request'

const props = defineProps<{
  visible: boolean
  submitting?: boolean
  zoneId: number | null
  zoneTitle: string
  availableSlots?: SlotConfig[]
}>()

const emit = defineEmits<{
  close: []
  submit: [payload: CreateExhibitRequest]
}>()

const form = reactive({
  title: '',
  subtitle: '',
  exhibitType: 'image',
  placementMode: 'freeform',
  slotCode: null as string | null,
  mediaUrl: '',
  coverUrl: '',
  mediaAssetId: null as number | null,
  description: '',
})

const mediaFileInputRef = ref<HTMLInputElement | null>(null)
const coverFileInputRef = ref<HTMLInputElement | null>(null)
const mediaUploading = ref(false)
const coverUploading = ref(false)
const mediaUploadError = ref('')
const coverUploadError = ref('')

const mediaAccept = computed(() => {
  switch (form.exhibitType) {
    case 'image': return 'image/*'
    case 'video': return 'video/*'
    case 'audio': return 'audio/*'
    case 'document': return '.pdf,.doc,.docx,.txt,.md'
    case 'model': return '.glb,.gltf,.obj,.fbx'
    default: return '*/*'
  }
})

const mediaHint = computed(() => {
  switch (form.exhibitType) {
    case 'image': return '图片将同时作为封面'
    case 'video': return '视频文件'
    case 'audio': return '音频文件'
    case 'document': return 'PDF/Word/文本'
    case 'model': return 'GLTF/GLB/OBJ'
    case 'text': return '不需上传文件'
    default: return '选填'
  }
})

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
    if (data.fileUrl) form.mediaUrl = data.fileUrl
    if (data.assetId) form.mediaAssetId = data.assetId
    // 图片类型同步到 coverUrl，避免用户重复上传
    if (form.exhibitType === 'image' && data.fileUrl) form.coverUrl = data.fileUrl
  } catch (err) {
    mediaUploadError.value = getErrorMessage(err, '上传失败')
  } finally {
    mediaUploading.value = false
  }
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
    if (data.fileUrl) form.coverUrl = data.fileUrl
  } catch (err) {
    coverUploadError.value = getErrorMessage(err, '上传失败')
  } finally {
    coverUploading.value = false
  }
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      form.title = ''
      form.subtitle = ''
      form.exhibitType = 'image'
      form.placementMode = (props.availableSlots?.length ?? 0) > 0 ? 'slot' : 'freeform'
      form.slotCode = null
      form.mediaUrl = ''
      form.coverUrl = ''
      form.mediaAssetId = null
      form.description = ''
      mediaUploadError.value = ''
      coverUploadError.value = ''
    }
  },
)

const availableSlots = computed(() => props.availableSlots ?? [])
const canSubmit = computed(() => !!form.title.trim() && props.zoneId != null)

function handleCancel() {
  if (props.submitting) return
  emit('close')
}

function handleSubmit() {
  if (!canSubmit.value || props.submitting) return
  emit('submit', {
    zoneId: props.zoneId!,
    title: form.title.trim(),
    subtitle: form.subtitle.trim() || null,
    exhibitType: form.exhibitType,
    placementMode: form.placementMode,
    slotCode: form.placementMode === 'slot' ? form.slotCode : null,
    mediaUrl: form.mediaUrl.trim() || null,
    coverUrl: form.coverUrl.trim() || null,
    mediaAssetId: form.mediaAssetId,
    description: form.description.trim() || null,
  })
}
</script>
