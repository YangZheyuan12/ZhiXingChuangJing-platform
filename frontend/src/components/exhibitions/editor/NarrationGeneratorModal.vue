<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="visible" class="fixed inset-0 z-[95] flex items-center justify-center bg-slate-900/50 p-4" @click.self="handleClose">
        <div class="w-full max-w-2xl overflow-hidden rounded-2xl bg-white shadow-2xl">
          <header class="flex items-center justify-between border-b border-slate-200 bg-gradient-to-r from-brand-50 to-sky-50 px-6 py-4">
            <div class="flex items-center gap-2.5">
              <span class="inline-flex h-8 w-8 items-center justify-center rounded-full bg-brand-100 text-brand-700">
                <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M10 3.5a.75.75 0 01.75.75v1.5a.75.75 0 01-1.5 0v-1.5A.75.75 0 0110 3.5zM10 14.25a.75.75 0 01.75.75v1.5a.75.75 0 01-1.5 0v-1.5a.75.75 0 01.75-.75zM3.5 10a.75.75 0 01.75-.75h1.5a.75.75 0 010 1.5h-1.5A.75.75 0 013.5 10zM14.25 10a.75.75 0 01.75-.75h1.5a.75.75 0 010 1.5h-1.5a.75.75 0 01-.75-.75zM5.404 5.404a.75.75 0 011.06 0l1.06 1.061a.75.75 0 11-1.06 1.06l-1.06-1.06a.75.75 0 010-1.061zM12.475 12.475a.75.75 0 011.06 0l1.061 1.06a.75.75 0 11-1.06 1.061l-1.061-1.06a.75.75 0 010-1.061zM14.596 5.404a.75.75 0 010 1.06l-1.06 1.06a.75.75 0 11-1.061-1.06l1.06-1.06a.75.75 0 011.061 0zM7.525 12.475a.75.75 0 010 1.06l-1.06 1.061a.75.75 0 11-1.06-1.06l1.06-1.061a.75.75 0 011.06 0z" />
                </svg>
              </span>
              <div>
                <h2 class="text-base font-semibold text-slate-900">AI 讲解词生成</h2>
                <p class="text-xs text-slate-500">根据展品信息智能生成讲解文案与追问提示</p>
              </div>
            </div>
            <button
              type="button"
              class="rounded-lg p-1.5 text-slate-400 hover:bg-white/80 hover:text-slate-600"
              aria-label="关闭"
              @click="handleClose"
            >
              <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4.28 3.22a.75.75 0 00-1.06 1.06L8.94 10l-5.72 5.72a.75.75 0 101.06 1.06L10 11.06l5.72 5.72a.75.75 0 101.06-1.06L11.06 10l5.72-5.72a.75.75 0 00-1.06-1.06L10 8.94 4.28 3.22z" clip-rule="evenodd" />
              </svg>
            </button>
          </header>

          <div class="max-h-[70vh] overflow-y-auto px-6 py-5 space-y-5">
            <!-- 表单区 -->
            <section class="space-y-4">
              <div class="grid gap-4 sm:grid-cols-2">
                <label class="block">
                  <span class="mb-1.5 block text-xs font-medium text-slate-600">展品名称 <span class="text-rose-500">*</span></span>
                  <input
                    v-model="form.exhibitTitle"
                    type="text"
                    maxlength="120"
                    class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
                  />
                </label>
                <label class="block">
                  <span class="mb-1.5 block text-xs font-medium text-slate-600">目标年级（选填）</span>
                  <input
                    v-model="form.targetGrade"
                    type="text"
                    maxlength="32"
                    placeholder="如：小学五年级、初中一年级"
                    class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
                  />
                </label>
              </div>

              <label class="block">
                <span class="mb-1.5 block text-xs font-medium text-slate-600">展品描述（选填）</span>
                <textarea
                  v-model="form.exhibitDescription"
                  rows="3"
                  maxlength="2000"
                  placeholder="简要描述展品的外形、来源、关键特征等"
                  class="w-full resize-none rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
                />
              </label>

              <div>
                <span class="mb-1.5 block text-xs font-medium text-slate-600">知识点（选填，每行一条）</span>
                <textarea
                  v-model="knowledgePointsText"
                  rows="4"
                  placeholder="例如：&#10;青铜器铸造工艺&#10;西周礼乐文化&#10;饕餮纹的象征意义"
                  class="w-full resize-none rounded-lg border border-slate-300 px-3 py-2 text-sm font-mono focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
                />
              </div>

              <div>
                <span class="mb-2 block text-xs font-medium text-slate-600">讲解风格</span>
                <div class="grid grid-cols-2 gap-2 sm:grid-cols-4">
                  <button
                    v-for="style in STYLE_OPTIONS"
                    :key="style.value"
                    type="button"
                    class="rounded-lg border px-3 py-2 text-left text-xs transition"
                    :class="form.style === style.value
                      ? 'border-brand-500 bg-brand-50 text-brand-700 ring-1 ring-brand-300'
                      : 'border-slate-200 text-slate-600 hover:border-brand-300'"
                    @click="form.style = style.value"
                  >
                    <div class="font-medium">{{ style.label }}</div>
                    <div class="mt-0.5 text-[11px] text-slate-400">{{ style.hint }}</div>
                  </button>
                </div>
              </div>
            </section>

            <!-- 生成结果 -->
            <section v-if="result" class="space-y-3 rounded-xl border border-emerald-200 bg-emerald-50/40 p-4">
              <div class="flex items-center justify-between gap-2">
                <h3 class="text-sm font-semibold text-emerald-800">生成结果</h3>
                <div class="flex gap-2">
                  <button
                    type="button"
                    class="rounded-md bg-white px-2.5 py-1 text-xs text-slate-600 ring-1 ring-slate-200 hover:bg-slate-50"
                    @click="copyNarration"
                  >
                    {{ copyHint || '复制讲解词' }}
                  </button>
                  <button
                    type="button"
                    class="rounded-md bg-emerald-600 px-2.5 py-1 text-xs font-medium text-white hover:bg-emerald-700"
                    @click="useThisNarration"
                  >
                    使用这段文案 →
                  </button>
                </div>
              </div>
              <textarea
                v-model="result.narration"
                rows="8"
                class="w-full resize-none rounded-lg border border-emerald-200 bg-white px-3 py-2 text-sm leading-6 text-slate-700 focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-100"
              />
              <div v-if="result.suggestions && result.suggestions.length" class="space-y-1.5">
                <p class="text-xs font-medium text-emerald-700">追问与延伸</p>
                <ul class="space-y-1 text-xs text-slate-600">
                  <li v-for="(tip, idx) in result.suggestions" :key="idx" class="flex gap-1.5">
                    <span class="text-emerald-500">▸</span>
                    <span>{{ tip }}</span>
                  </li>
                </ul>
              </div>
            </section>

            <p v-if="errorMessage" class="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{{ errorMessage }}</p>
          </div>

          <footer class="flex items-center justify-between gap-3 border-t border-slate-200 bg-slate-50 px-6 py-4">
            <p class="text-xs text-slate-400">基于展品信息智能拼装，生成结果可二次编辑</p>
            <div class="flex gap-2">
              <button
                type="button"
                class="rounded-lg px-4 py-2 text-sm text-slate-600 hover:bg-slate-200"
                @click="handleClose"
              >
                关闭
              </button>
              <button
                type="button"
                :disabled="generating || !form.exhibitTitle.trim()"
                class="inline-flex items-center gap-1.5 rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700 disabled:bg-slate-300"
                @click="handleGenerate"
              >
                <svg v-if="generating" class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" class="opacity-25" />
                  <path fill="currentColor" class="opacity-75" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                {{ generating ? '生成中...' : result ? '重新生成' : '生成讲解词' }}
              </button>
            </div>
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { generateNarration } from '@/api/modules/ai'
import { getErrorMessage } from '@/utils/request'
import type { GenerateNarrationResponse, NarrationStyle } from '@/api/types'

interface StyleOption {
  value: NarrationStyle
  label: string
  hint: string
}

const STYLE_OPTIONS: StyleOption[] = [
  { value: 'narrative', label: '叙述型', hint: '娓娓道来' },
  { value: 'storytelling', label: '故事型', hint: '情境代入' },
  { value: 'academic', label: '学术型', hint: '严谨详实' },
  { value: 'conversational', label: '对话型', hint: '亲切互动' },
]

const props = defineProps<{
  visible: boolean
  initialTitle?: string
  initialDescription?: string
}>()

const emit = defineEmits<{
  close: []
  use: [narration: string, suggestions: string[]]
}>()

const form = reactive({
  exhibitTitle: '',
  exhibitDescription: '',
  targetGrade: '',
  style: 'narrative' as NarrationStyle,
})

const knowledgePointsText = ref('')
const generating = ref(false)
const result = ref<GenerateNarrationResponse | null>(null)
const errorMessage = ref('')
const copyHint = ref('')

watch(
  () => props.visible,
  (val) => {
    if (val) {
      form.exhibitTitle = props.initialTitle ?? ''
      form.exhibitDescription = props.initialDescription ?? ''
      form.targetGrade = ''
      form.style = 'narrative'
      knowledgePointsText.value = ''
      result.value = null
      errorMessage.value = ''
      copyHint.value = ''
    }
  },
)

async function handleGenerate() {
  if (!form.exhibitTitle.trim() || generating.value) return
  generating.value = true
  errorMessage.value = ''
  const knowledgePoints = knowledgePointsText.value
    .split('\n')
    .map(s => s.trim())
    .filter(Boolean)
  try {
    result.value = await generateNarration({
      exhibitTitle: form.exhibitTitle.trim(),
      exhibitDescription: form.exhibitDescription.trim() || null,
      knowledgePoints: knowledgePoints.length ? knowledgePoints : null,
      targetGrade: form.targetGrade.trim() || null,
      style: form.style,
    })
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '讲解词生成失败')
  } finally {
    generating.value = false
  }
}

async function copyNarration() {
  if (!result.value) return
  try {
    await navigator.clipboard.writeText(result.value.narration)
    copyHint.value = '已复制 ✓'
    setTimeout(() => (copyHint.value = ''), 1500)
  } catch {
    copyHint.value = '复制失败'
    setTimeout(() => (copyHint.value = ''), 1500)
  }
}

function useThisNarration() {
  if (!result.value) return
  emit('use', result.value.narration, result.value.suggestions ?? [])
  handleClose()
}

function handleClose() {
  emit('close')
}
</script>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
