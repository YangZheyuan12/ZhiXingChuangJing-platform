<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="Class Submissions"
      title="班级作品管理"
      description="查看本任务下学生提交的所有作品，按状态筛选并进入审核评分页面。"
      :back-to="`/tasks/${taskId}`"
      back-label="返回任务详情"
    />

    <p v-if="errorMessage" class="rounded-xl bg-rose-50 px-4 py-3 text-sm text-rose-600">{{ errorMessage }}</p>

    <div class="grid gap-6 md:grid-cols-4">
      <MetricTile label="提交总数" :value="totalCount" hint="所有版本（含历史提交）" />
      <MetricTile label="待审核" :value="pendingCount" hint="status = submitted" />
      <MetricTile label="已通过" :value="approvedCount" hint="status = approved" />
      <MetricTile label="已退回" :value="returnedCount" hint="status = returned" />
    </div>

    <section class="panel-card p-6">
      <SectionHeader title="提交列表" description="按状态筛选并跳转到审核详情。" />

      <div class="mt-4 flex flex-wrap items-center gap-2">
        <button
          v-for="filter in statusFilters"
          :key="filter.value"
          type="button"
          class="rounded-full px-4 py-1.5 text-sm transition"
          :class="
            activeFilter === filter.value
              ? 'bg-brand-600 text-white shadow-sm'
              : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
          "
          @click="activeFilter = filter.value"
        >
          {{ filter.label }}
          <span v-if="filter.value !== 'all'" class="ml-1 text-xs opacity-70">({{ countByStatus(filter.value) }})</span>
        </button>
      </div>

      <div v-if="loading" class="mt-6 flex items-center justify-center py-12 text-sm text-slate-400">
        加载中...
      </div>

      <EmptyStatePanel
        v-else-if="filteredSubmissions.length === 0"
        title="暂无提交记录"
        :description="activeFilter === 'all' ? '本任务还没有学生提交作品。' : `没有处于「${currentFilterLabel}」状态的提交。`"
      />

      <div v-else class="mt-4 overflow-hidden rounded-2xl border border-slate-200">
        <table class="w-full text-sm">
          <thead class="bg-slate-50 text-left text-xs font-medium uppercase tracking-wide text-slate-500">
            <tr>
              <th class="px-4 py-3">提交人</th>
              <th class="px-4 py-3">展厅</th>
              <th class="px-4 py-3">版本</th>
              <th class="px-4 py-3">状态</th>
              <th class="px-4 py-3">提交时间</th>
              <th class="px-4 py-3">评分</th>
              <th class="px-4 py-3 text-right">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 bg-white">
            <tr v-for="submission in filteredSubmissions" :key="submission.id" class="hover:bg-slate-50">
              <td class="px-4 py-3">
                <div class="font-medium text-slate-900">
                  {{ submission.submitter.nickname || submission.submitter.name || '匿名' }}
                </div>
                <div class="text-xs text-slate-400">ID: {{ submission.submitter.id }}</div>
              </td>
              <td class="px-4 py-3">
                <RouterLink
                  :to="`/exhibitions/${submission.exhibitionId}/view`"
                  target="_blank"
                  class="text-brand-600 hover:underline"
                >
                  展厅 #{{ submission.exhibitionId }}
                </RouterLink>
              </td>
              <td class="px-4 py-3 text-slate-600">v{{ submission.versionNo }}</td>
              <td class="px-4 py-3">
                <span class="rounded-full px-2.5 py-0.5 text-xs font-medium" :class="statusClass(submission.submissionStatus)">
                  {{ statusLabel(submission.submissionStatus) }}
                </span>
              </td>
              <td class="px-4 py-3 text-slate-500">{{ formatDateTime(submission.submittedAt) }}</td>
              <td class="px-4 py-3 text-slate-600">
                <span v-if="getScore(submission) !== null" class="font-semibold text-brand-700">
                  {{ getScore(submission) }}
                </span>
                <span v-else class="text-slate-300">—</span>
              </td>
              <td class="px-4 py-3 text-right">
                <RouterLink
                  :to="`/submissions/${submission.id}`"
                  class="inline-flex items-center gap-1 rounded-lg border border-brand-200 px-3 py-1 text-xs font-medium text-brand-700 transition hover:bg-brand-50"
                >
                  {{ submission.submissionStatus === 'submitted' ? '去审核' : '查看详情' }}
                  <svg class="h-3 w-3" viewBox="0 0 20 20" fill="currentColor">
                    <path
                      fill-rule="evenodd"
                      d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z"
                      clip-rule="evenodd"
                    />
                  </svg>
                </RouterLink>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getTaskSubmissions } from '@/api/modules/tasks'
import { getErrorMessage } from '@/utils/request'
import type { SubmissionDetail } from '@/api/types'
import EmptyStatePanel from '@/components/common/EmptyStatePanel.vue'
import MetricTile from '@/components/common/MetricTile.vue'
import PageHero from '@/components/common/PageHero.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import { formatDateTime } from '@/utils/format'

type FilterValue = 'all' | 'submitted' | 'reviewed' | 'approved' | 'returned'

const route = useRoute()
const taskId = Number(route.params.taskId)

const loading = ref(true)
const errorMessage = ref('')
const submissions = ref<SubmissionDetail[]>([])
const activeFilter = ref<FilterValue>('all')

const statusFilters: { value: FilterValue; label: string }[] = [
  { value: 'all', label: '全部' },
  { value: 'submitted', label: '待审核' },
  { value: 'reviewed', label: '已评分' },
  { value: 'approved', label: '已通过' },
  { value: 'returned', label: '已退回' },
]

const currentFilterLabel = computed(
  () => statusFilters.find((f) => f.value === activeFilter.value)?.label ?? '全部',
)

const totalCount = computed(() => submissions.value.length)
const pendingCount = computed(() => countByStatus('submitted'))
const approvedCount = computed(() => countByStatus('approved'))
const returnedCount = computed(() => countByStatus('returned'))

const filteredSubmissions = computed(() => {
  if (activeFilter.value === 'all') return submissions.value
  return submissions.value.filter((s) => s.submissionStatus === activeFilter.value)
})

function countByStatus(status: FilterValue): number {
  if (status === 'all') return submissions.value.length
  return submissions.value.filter((s) => s.submissionStatus === status).length
}

function statusClass(status: string): string {
  const map: Record<string, string> = {
    submitted: 'bg-amber-100 text-amber-700',
    reviewed: 'bg-blue-100 text-blue-700',
    approved: 'bg-emerald-100 text-emerald-700',
    returned: 'bg-rose-100 text-rose-700',
  }
  return map[status] ?? 'bg-slate-100 text-slate-600'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = {
    submitted: '待审核',
    reviewed: '已评分',
    approved: '已通过',
    returned: '已退回',
  }
  return map[status] ?? status
}

function getScore(submission: SubmissionDetail): number | null {
  if (!submission.reviews || submission.reviews.length === 0) return null
  const score = submission.reviews[0].score
  return typeof score === 'number' ? score : null
}

onMounted(async () => {
  if (!Number.isFinite(taskId) || taskId <= 0) {
    errorMessage.value = '任务 ID 无效'
    loading.value = false
    return
  }
  try {
    submissions.value = await getTaskSubmissions(taskId)
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '加载提交列表失败')
  } finally {
    loading.value = false
  }
})
</script>
