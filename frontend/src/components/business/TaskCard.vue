<template>
  <RouterLink
    :to="to"
    class="group block rounded-[1.75rem] border border-white/60 bg-white/90 p-5 shadow-panel transition hover:-translate-y-0.5 hover:border-brand-200"
  >
    <div class="flex items-start justify-between gap-4">
      <div>
        <p class="text-xs uppercase tracking-[0.24em] text-slate-400">Task {{ task.id }}</p>
        <h3 class="mt-2 text-xl font-semibold text-slate-900 transition group-hover:text-brand-700">{{ task.title }}</h3>
      </div>
      <StatusPill :value="task.status" />
    </div>
    <p class="mt-4 line-clamp-2 text-sm leading-6 text-slate-500">{{ task.description }}</p>
    <div class="mt-5 grid gap-3 rounded-2xl bg-slate-50/80 p-4 text-sm text-slate-600 md:grid-cols-2">
      <p>授课教师：{{ task.teacher.nickname || task.teacher.name }}</p>
      <p>开始时间：{{ formatDateTime(task.startTime) }}</p>
      <p class="md:col-span-2">截止时间：{{ formatDateTime(task.dueTime) }}</p>
    </div>
  </RouterLink>
</template>

<script setup lang="ts">
import type { TaskSummary } from '@/api/types'
import StatusPill from '@/components/common/StatusPill.vue'
import { formatDateTime } from '@/utils/format'

defineProps<{
  task: TaskSummary
  to: string
}>()
</script>
