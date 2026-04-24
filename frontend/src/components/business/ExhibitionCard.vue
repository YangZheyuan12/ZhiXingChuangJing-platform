<template>
  <RouterLink
    :to="to"
    class="group block overflow-hidden rounded-[1.75rem] border border-white/60 bg-white/90 shadow-panel transition hover:-translate-y-0.5 hover:border-brand-200"
  >
    <div class="bg-[radial-gradient(circle_at_top_left,rgba(159,43,34,0.16),transparent_42%),linear-gradient(135deg,#fffdfc,#f8f2ef)] px-5 py-5">
      <div class="flex items-start justify-between gap-4">
        <div>
          <p class="text-xs uppercase tracking-[0.24em] text-slate-400">Exhibition {{ exhibition.id }}</p>
          <h3 class="mt-2 text-xl font-semibold text-slate-900 transition group-hover:text-brand-700">{{ exhibition.title }}</h3>
        </div>
        <StatusPill :value="variant === 'community' ? 'public' : exhibition.status" />
      </div>
      <p class="mt-4 line-clamp-2 text-sm leading-6 text-slate-500">{{ exhibition.summary || '围绕课程任务构建的数字展厅项目。' }}</p>
    </div>
    <div class="px-5 py-4">
      <div class="flex flex-wrap gap-2">
        <span
          v-for="tag in exhibition.tags.slice(0, 3)"
          :key="tag"
          class="rounded-full bg-brand-50 px-3 py-1 text-xs text-brand-700"
        >
          {{ tag }}
        </span>
        <span v-if="exhibition.tags.length === 0" class="rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-500">
          未设置标签
        </span>
      </div>
      <div class="mt-5 grid grid-cols-2 gap-3 text-sm text-slate-500">
        <div>
          <p class="text-xs uppercase tracking-[0.18em] text-slate-400">Author</p>
          <p class="mt-1">{{ exhibition.author.nickname || exhibition.author.name }}</p>
        </div>
        <div>
          <p class="text-xs uppercase tracking-[0.18em] text-slate-400">Group</p>
          <p class="mt-1">{{ exhibition.groupName || '--' }}</p>
        </div>
        <div>
          <p class="text-xs uppercase tracking-[0.18em] text-slate-400">Likes</p>
          <p class="mt-1">{{ exhibition.stats.likeCount }}</p>
        </div>
        <div>
          <p class="text-xs uppercase tracking-[0.18em] text-slate-400">Comments</p>
          <p class="mt-1">{{ exhibition.stats.commentCount }}</p>
        </div>
      </div>
    </div>
  </RouterLink>
</template>

<script setup lang="ts">
import type { ExhibitionSummary } from '@/api/types'
import StatusPill from '@/components/common/StatusPill.vue'

withDefaults(defineProps<{
  exhibition: ExhibitionSummary
  to: string
  variant?: 'studio' | 'community'
}>(), {
  variant: 'studio',
})
</script>
