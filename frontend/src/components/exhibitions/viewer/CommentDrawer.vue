<template>
  <Teleport to="body">
    <Transition name="drawer-fade">
      <div v-if="visible" class="fixed inset-0 z-[90] bg-slate-900/40" @click.self="$emit('close')" />
    </Transition>

    <Transition name="drawer-slide">
      <aside
        v-if="visible"
        class="fixed right-0 top-0 z-[91] flex h-full w-full max-w-md flex-col bg-white shadow-2xl"
      >
        <header class="flex shrink-0 items-center justify-between border-b border-slate-200 px-5 py-4">
          <h2 class="text-base font-semibold text-slate-900">
            评论 <span class="ml-1 text-sm text-slate-400">({{ comments.length }})</span>
          </h2>
          <button
            type="button"
            class="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
            aria-label="关闭"
            @click="$emit('close')"
          >
            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M4.28 3.22a.75.75 0 00-1.06 1.06L8.94 10l-5.72 5.72a.75.75 0 101.06 1.06L10 11.06l5.72 5.72a.75.75 0 101.06-1.06L11.06 10l5.72-5.72a.75.75 0 00-1.06-1.06L10 8.94 4.28 3.22z" clip-rule="evenodd" />
            </svg>
          </button>
        </header>

        <section class="flex-1 overflow-y-auto px-5 py-4">
          <div v-if="teacherReviews.length" class="mb-5">
            <h3 class="mb-3 text-xs font-semibold uppercase tracking-widest text-amber-600">教师点评</h3>
            <div class="space-y-3">
              <article
                v-for="review in teacherReviews"
                :key="review.id"
                class="rounded-xl border border-amber-200 bg-amber-50/60 p-3"
              >
                <div class="flex items-center justify-between gap-2">
                  <span class="text-sm font-medium text-amber-900">{{ review.reviewer?.nickname || '老师' }}</span>
                  <span v-if="review.score != null" class="rounded bg-amber-100 px-2 py-0.5 text-xs font-semibold text-amber-700">
                    {{ review.score }} 分
                  </span>
                </div>
                <p class="mt-1.5 text-sm leading-6 text-slate-700">{{ review.comment || '暂无点评内容' }}</p>
                <p class="mt-1 text-xs text-amber-600/70">{{ review.createdAt }}</p>
              </article>
            </div>
          </div>

          <div v-if="comments.length === 0 && teacherReviews.length === 0" class="flex h-48 flex-col items-center justify-center text-sm text-slate-400">
            <svg class="mb-2 h-10 w-10 text-slate-200" viewBox="0 0 24 24" fill="currentColor">
              <path d="M4.913 2.658c2.075-.27 4.19-.408 6.337-.408 2.147 0 4.262.139 6.337.408 1.922.25 3.291 1.861 3.405 3.727a4.4 4.4 0 00-1.032-.211 50.89 50.89 0 00-8.42 0c-2.358.196-4.04 2.19-4.04 4.434v4.286a4.47 4.47 0 002.433 3.984L7.28 21.53A.75.75 0 016 21v-4.03a48.527 48.527 0 01-1.087-.128C2.905 16.58 1.5 14.833 1.5 12.862V6.638c0-1.97 1.405-3.718 3.413-3.979z" />
            </svg>
            还没有评论，来抢沙发吧
          </div>

          <div v-else-if="comments.length > 0">
            <h3 class="mb-3 text-xs font-semibold uppercase tracking-widest text-slate-400">观众评论</h3>
            <div class="space-y-3">
              <article
                v-for="comment in comments"
                :key="comment.id"
                class="rounded-xl border border-slate-200 p-3"
              >
                <div class="flex items-center justify-between gap-2">
                  <span class="text-sm font-medium text-slate-900">{{ comment.user?.nickname || '匿名' }}</span>
                  <span class="text-xs text-slate-400">{{ comment.createdAt }}</span>
                </div>
                <p class="mt-1.5 whitespace-pre-wrap text-sm leading-6 text-slate-600">{{ comment.content }}</p>
              </article>
            </div>
          </div>
        </section>

        <footer class="shrink-0 border-t border-slate-200 bg-slate-50 px-5 py-4">
          <div v-if="!canComment" class="rounded-lg bg-white px-3 py-2 text-center text-xs text-slate-400">
            登录后即可发表评论
          </div>
          <template v-else>
            <textarea
              v-model="draft"
              rows="3"
              :maxlength="500"
              :disabled="submitting"
              class="w-full resize-none rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100 disabled:bg-slate-100"
              placeholder="写下你对这个展厅的感受..."
            />
            <div class="mt-2 flex items-center justify-between">
              <p class="text-xs text-slate-400">{{ draft.length }} / 500</p>
              <button
                type="button"
                class="rounded-lg bg-brand-600 px-4 py-1.5 text-sm font-medium text-white transition hover:bg-brand-700 disabled:bg-slate-300"
                :disabled="submitting || !draft.trim()"
                @click="handleSubmit"
              >
                {{ submitting ? '发布中...' : '发布' }}
              </button>
            </div>
          </template>
        </footer>
      </aside>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Comment, SubmissionReview } from '@/api/types'

const props = defineProps<{
  visible: boolean
  comments: Comment[]
  teacherReviews: SubmissionReview[]
  canComment: boolean
  submitting?: boolean
}>()

const emit = defineEmits<{
  close: []
  submit: [content: string]
}>()

const draft = ref('')

watch(
  () => props.visible,
  (val) => {
    if (val) draft.value = ''
  },
)

function handleSubmit() {
  const content = draft.value.trim()
  if (!content || props.submitting) return
  emit('submit', content)
}
</script>

<style scoped>
.drawer-fade-enter-active,
.drawer-fade-leave-active {
  transition: opacity 0.2s ease;
}
.drawer-fade-enter-from,
.drawer-fade-leave-to {
  opacity: 0;
}

.drawer-slide-enter-active,
.drawer-slide-leave-active {
  transition: transform 0.25s cubic-bezier(0.32, 0.72, 0, 1);
}
.drawer-slide-enter-from,
.drawer-slide-leave-to {
  transform: translateX(100%);
}
</style>
