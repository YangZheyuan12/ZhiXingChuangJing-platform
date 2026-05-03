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
        <div class="w-[28rem] rounded-2xl bg-white p-6 shadow-2xl">
          <div class="flex items-start justify-between">
            <div>
              <h3 class="text-lg font-semibold text-slate-900">提交审核</h3>
              <p class="mt-1 text-sm text-slate-500">
                提交后教师将对当前版本进行审核评分。提交期间你仍可继续编辑草稿。
              </p>
            </div>
            <button
              type="button"
              class="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
              @click="handleCancel"
              aria-label="关闭"
            >
              <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fill-rule="evenodd"
                  d="M4.28 3.22a.75.75 0 00-1.06 1.06L8.94 10l-5.72 5.72a.75.75 0 101.06 1.06L10 11.06l5.72 5.72a.75.75 0 101.06-1.06L11.06 10l5.72-5.72a.75.75 0 00-1.06-1.06L10 8.94 4.28 3.22z"
                  clip-rule="evenodd"
                />
              </svg>
            </button>
          </div>

          <div class="mt-5 rounded-xl bg-amber-50 px-4 py-3 text-xs text-amber-700">
            <p class="flex items-center gap-2 font-medium">
              <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fill-rule="evenodd"
                  d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z"
                  clip-rule="evenodd"
                />
              </svg>
              提交规则
            </p>
            <ul class="mt-2 space-y-1 pl-5 list-disc">
              <li>展厅必须保存过至少一个版本（含手动保存）</li>
              <li>提交将基于当前最新版本号</li>
              <li>提交后展厅状态变为「已提交」</li>
            </ul>
          </div>

          <div class="mt-4">
            <label for="submit-remark" class="block text-sm font-medium text-slate-700">
              提交说明 <span class="text-slate-400">（选填）</span>
            </label>
            <textarea
              id="submit-remark"
              v-model="remark"
              rows="4"
              :maxlength="500"
              :disabled="submitting"
              class="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100 disabled:bg-slate-50"
              placeholder="描述本次提交的主要变更或亮点..."
            />
            <p class="mt-1 text-right text-xs text-slate-400">{{ remark.length }} / 500</p>
          </div>

          <div class="mt-6 flex justify-end gap-3">
            <button
              type="button"
              class="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-100 disabled:opacity-50"
              :disabled="submitting"
              @click="handleCancel"
            >
              取消
            </button>
            <button
              type="button"
              class="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-brand-700 disabled:opacity-60"
              :disabled="submitting"
              @click="handleSubmit"
            >
              {{ submitting ? '提交中...' : '确认提交' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  visible: boolean
  submitting?: boolean
}>()

const emit = defineEmits<{
  close: []
  submit: [remark: string]
}>()

const remark = ref('')

watch(
  () => props.visible,
  (val) => {
    if (val) {
      remark.value = ''
    }
  },
)

function handleCancel() {
  if (props.submitting) return
  emit('close')
}

function handleSubmit() {
  if (props.submitting) return
  emit('submit', remark.value.trim())
}
</script>
