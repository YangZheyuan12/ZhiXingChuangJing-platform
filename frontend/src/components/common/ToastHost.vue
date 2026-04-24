<template>
  <div class="pointer-events-none fixed right-4 top-4 z-[100] flex w-full max-w-sm flex-col gap-3">
    <TransitionGroup name="toast" tag="div" class="flex flex-col gap-3">
      <div
        v-for="toast in appStore.toasts"
        :key="toast.id"
        class="pointer-events-auto flex items-start gap-3 overflow-hidden rounded-2xl border bg-white/95 px-4 py-3 shadow-lg backdrop-blur"
        :class="toneMap[toast.tone].container"
      >
        <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full" :class="toneMap[toast.tone].iconWrap">
          <svg v-if="toast.tone === 'success'" class="h-4 w-4 text-white" viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="m5 10 3.5 3.5L15 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <svg v-else-if="toast.tone === 'error'" class="h-4 w-4 text-white" viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="m6 6 8 8m0-8-8 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <svg v-else class="h-4 w-4 text-white" viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="M10 7v4m0 3h.01M10 17a7 7 0 1 0 0-14 7 7 0 0 0 0 14Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </span>
        <div class="min-w-0 flex-1">
          <p class="text-xs font-semibold tracking-wide" :class="toneMap[toast.tone].title">
            {{ toneMap[toast.tone].label }}
          </p>
          <p class="mt-0.5 break-words text-sm text-slate-700">{{ toast.message }}</p>
        </div>
        <button
          type="button"
          class="shrink-0 text-slate-300 transition hover:text-slate-500"
          aria-label="关闭"
          @click="appStore.removeToast(toast.id)"
        >
          <svg class="h-4 w-4" viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="m6 6 8 8m0-8-8 8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const toneMap = {
  success: {
    label: '操作成功',
    container: 'border-emerald-100 ring-1 ring-emerald-50',
    iconWrap: 'bg-emerald-500',
    title: 'text-emerald-600',
  },
  error: {
    label: '操作失败',
    container: 'border-rose-100 ring-1 ring-rose-50',
    iconWrap: 'bg-rose-500',
    title: 'text-rose-600',
  },
  info: {
    label: '通知提醒',
    container: 'border-slate-100 ring-1 ring-slate-50',
    iconWrap: 'bg-slate-500',
    title: 'text-slate-600',
  },
}
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.25s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(24px) scale(0.96);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(32px) scale(0.96);
}

.toast-move {
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}
</style>
