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
        <div class="w-[30rem] rounded-2xl bg-white p-6 shadow-2xl">
          <div class="flex items-start justify-between">
            <div>
              <h3 class="text-lg font-semibold text-slate-900">新增展区</h3>
              <p class="mt-1 text-sm text-slate-500">
                展区是展厅的独立空间，可包含多个展品。
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
              <span class="text-sm font-medium text-slate-700">展区标题<span class="text-rose-500">*</span></span>
              <input
                v-model="form.title"
                type="text"
                :disabled="submitting"
                maxlength="60"
                class="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
                placeholder="例如：序厅、主展区、结语"
              />
            </label>

            <label class="block">
              <span class="text-sm font-medium text-slate-700">展区编码<span class="text-rose-500">*</span></span>
              <input
                v-model="form.zoneCode"
                type="text"
                :disabled="submitting"
                maxlength="32"
                class="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
                placeholder="zone_01"
                @blur="normalizeZoneCode"
              />
              <span class="mt-1 block text-xs text-slate-400">只能包含字母数字和下划线，作为内部唯一标识</span>
            </label>

            <label class="block">
              <span class="text-sm font-medium text-slate-700">展区类型</span>
              <select
                v-model="form.zoneType"
                :disabled="submitting"
                class="mt-1.5 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
              >
                <option value="gallery">展示区（gallery）</option>
                <option value="entrance">入口（entrance）</option>
                <option value="closeup">特写（closeup）</option>
                <option value="exit">结语（exit）</option>
                <option value="timeline_node">时间轴节点（timeline）</option>
                <option value="map_point">地图节点（map）</option>
              </select>
            </label>

            <label class="block">
              <span class="text-sm font-medium text-slate-700">副标题 <span class="text-slate-400">（选填）</span></span>
              <input
                v-model="form.subtitle"
                type="text"
                :disabled="submitting"
                maxlength="120"
                class="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
              />
            </label>

            <label class="block">
              <span class="text-sm font-medium text-slate-700">切换动画</span>
              <select
                v-model="form.transitionIn"
                :disabled="submitting"
                class="mt-1.5 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
              >
                <option value="fade">淡入淡出（fade）</option>
                <option value="slide">滑动（slide）</option>
                <option value="zoom">缩放（zoom）</option>
                <option value="none">无动画</option>
              </select>
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
import type { CreateZoneRequest } from '@/api/types'

const props = defineProps<{
  visible: boolean
  submitting?: boolean
  nextSortOrder?: number
}>()

const emit = defineEmits<{
  close: []
  submit: [payload: CreateZoneRequest]
}>()

const form = reactive({
  title: '',
  zoneCode: '',
  zoneType: 'gallery',
  subtitle: '',
  transitionIn: 'fade',
})

watch(
  () => props.visible,
  (val) => {
    if (val) {
      form.title = ''
      form.zoneCode = ''
      form.zoneType = 'gallery'
      form.subtitle = ''
      form.transitionIn = 'fade'
    }
  },
)

const canSubmit = computed(() => !!form.title.trim() && /^[a-zA-Z0-9_]+$/.test(form.zoneCode.trim()))

function normalizeZoneCode() {
  form.zoneCode = form.zoneCode.trim().replace(/[^a-zA-Z0-9_]/g, '_').toLowerCase()
}

function handleCancel() {
  if (props.submitting) return
  emit('close')
}

function handleSubmit() {
  if (props.submitting || !canSubmit.value) return
  normalizeZoneCode()
  emit('submit', {
    title: form.title.trim(),
    zoneCode: form.zoneCode.trim(),
    zoneType: form.zoneType,
    subtitle: form.subtitle.trim() || null,
    transitionIn: form.transitionIn,
    sortOrder: props.nextSortOrder,
  })
}
</script>
