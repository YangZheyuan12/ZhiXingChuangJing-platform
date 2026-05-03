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

            <label class="block">
              <span class="text-sm font-medium text-slate-700">媒体 URL <span class="text-slate-400">（选填）</span></span>
              <input
                v-model="form.mediaUrl"
                type="text"
                :disabled="submitting"
                class="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
                placeholder="https://..."
              />
            </label>

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
import { computed, reactive, watch } from 'vue'
import type { CreateExhibitRequest, SlotConfig } from '@/api/types'

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
  description: '',
})

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
      form.description = ''
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
    description: form.description.trim() || null,
  })
}
</script>
