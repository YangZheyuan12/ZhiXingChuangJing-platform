<template>
  <div class="space-y-6">
    <section class="panel-card p-8 text-center">
      <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-500">
        <svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M12 6v6l4 2" />
          <circle cx="12" cy="12" r="9" />
        </svg>
      </div>
      <h2 class="mt-4 text-xl font-semibold text-slate-800">{{ moduleLabel }} · 建设中</h2>
      <p class="mt-2 text-sm text-slate-500">
        该管理模块功能正在规划中，后续将独立提供审核、统计与配置能力。
      </p>
      <p class="mt-1 text-xs text-slate-400">你可以暂时通过创作端访问对应模块的常规功能。</p>
      <div class="mt-5 flex justify-center gap-3">
        <RouterLink to="/admin" class="rounded-full bg-indigo-500 px-4 py-2 text-sm font-medium text-white transition hover:bg-indigo-600">
          返回管理总览
        </RouterLink>
        <RouterLink to="/" class="rounded-full border border-slate-200 px-4 py-2 text-sm text-slate-600 transition hover:border-indigo-300 hover:text-indigo-600">
          前往创作端
        </RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const moduleLabel = computed(() => (route.meta.title as string) || '管理模块')
</script>
