<template>
  <div class="mx-auto max-w-6xl min-h-screen bg-stone-50 p-6">
    <PageHero
      eyebrow="Task Center"
      title="课程任务中心"
      description="查看当前课程任务、截止时间与任务说明。教师与管理员可从页面按钮或工作台快捷入口进入独立发布页。"
    >
      <template #badge>
        当前可见 {{ tasks.length }} 个任务
      </template>
      <template #actions>
        <RouterLink
          v-if="canCreateTask"
          to="/tasks/create"
          class="page-primary-button"
        >
          发布任务
        </RouterLink>
      </template>
    </PageHero>

    <p v-if="errorMessage" class="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
      {{ errorMessage }}
    </p>

    <section class="mt-8 mb-10 grid grid-cols-1 gap-4 md:grid-cols-3">
      <MetricTile
        v-for="item in stats"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.tip"
      />
    </section>

    <section>
      <div class="mb-6">
        <div>
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-stone-400">Task List</p>
          <h2 class="mt-2 text-xl font-bold text-stone-800">任务列表</h2>
          <p class="mt-2 text-sm text-stone-500">查看当前可参与或可管理的任务项目。</p>
        </div>
      </div>

      <div v-if="loading" class="rounded-xl border border-stone-200 bg-white px-4 py-6 text-sm text-stone-500">
        任务加载中...
      </div>

      <div v-else-if="tasks.length === 0" class="rounded-xl border border-dashed border-stone-300 bg-white p-8 text-center">
        <p class="text-sm font-semibold uppercase tracking-[0.3em] text-stone-400">Empty</p>
        <h3 class="mt-3 text-lg font-bold text-stone-800">当前没有可见任务</h3>
        <p class="mt-2 text-sm text-stone-500">教师发布任务后，这里会自动展示项目卡片。</p>
      </div>

      <div v-else class="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
        <RouterLink
          v-for="task in tasks"
          :key="task.id"
          :to="`/tasks/${task.id}`"
          class="relative overflow-hidden rounded-xl border border-stone-200 border-t-4 border-t-red-800 bg-white p-6 transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:shadow-red-900/5"
        >
          <div class="pointer-events-none absolute -right-8 -top-8 h-24 w-24 rounded-full bg-red-100/60 blur-2xl" />
          <div class="relative">
            <div class="flex items-start justify-between gap-4">
              <p class="text-xs font-bold tracking-wider text-stone-400">TASK {{ task.id }}</p>
              <span :class="statusPillClass(task.status)">
                {{ getStatusLabel(task.status) }}
              </span>
            </div>

            <h3 class="mt-2 text-lg font-bold text-stone-800">{{ task.title }}</h3>
            <p class="mt-3 line-clamp-2 text-sm text-stone-500">
              {{ task.description || '暂无任务说明。' }}
            </p>

            <div class="mt-6 grid gap-2 text-xs text-stone-500">
              <div class="flex items-center justify-between">
                <span>开始时间</span>
                <span class="text-right text-red-700">{{ formatDateTime(task.startTime) }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span>截止时间</span>
                <span class="text-right text-red-700">{{ formatDateTime(task.dueTime) }}</span>
              </div>
            </div>

            <div class="mt-6 flex items-center justify-between border-t border-stone-100 pt-4 text-xs text-stone-500">
              <span>教师 {{ getTeacherName(task) }}</span>
              <span>{{ isClosingSoon(task.dueTime) ? '即将截止' : '持续开放' }}</span>
            </div>
          </div>
        </RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getTaskList } from '@/api/modules/tasks'
import type { TaskSummary } from '@/api/types'
import MetricTile from '@/components/common/MetricTile.vue'
import PageHero from '@/components/common/PageHero.vue'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'
import { getErrorMessage } from '@/utils/request'

const authStore = useAuthStore()

const loading = ref(false)
const errorMessage = ref('')
const tasks = ref<TaskSummary[]>([])

const canCreateTask = computed(() => ['teacher', 'admin'].includes(authStore.user?.role || ''))
const publishedCount = computed(() => tasks.value.filter((task) => task.status === 'published').length)
const closingSoonCount = computed(() => tasks.value.filter((task) => isClosingSoon(task.dueTime)).length)
const stats = computed(() => [
  { label: '当前任务', value: tasks.value.length, tip: '当前账号可浏览到的课程任务数量。' },
  { label: '进行中任务', value: publishedCount.value, tip: '已发布且仍在推进中的任务量。' },
  { label: '即将截止', value: closingSoonCount.value, tip: '未来 7 天内即将结束的任务数量。' },
])

async function fetchTasks() {
  loading.value = true
  errorMessage.value = ''

  try {
    const page = await getTaskList({ page: 1, pageSize: 20 })
    tasks.value = page.list
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '任务列表加载失败')
  } finally {
    loading.value = false
  }
}

function getStatusLabel(status?: string | null) {
  switch (status) {
    case 'published':
      return '已发布'
    case 'draft':
      return '草稿'
    case 'closed':
      return '已关闭'
    case 'archived':
      return '已归档'
    default:
      return status || '未知'
  }
}

function statusPillClass(status?: string | null) {
  if (status === 'published') {
    return 'rounded-full bg-red-50 px-3 py-1 text-xs font-medium text-red-700'
  }

  if (status === 'closed' || status === 'archived') {
    return 'rounded-full bg-stone-100 px-3 py-1 text-xs font-medium text-stone-600'
  }

  return 'rounded-full bg-amber-50 px-3 py-1 text-xs font-medium text-amber-700'
}

function getTeacherName(task: TaskSummary) {
  return task.teacher.nickname || task.teacher.name || '--'
}

function isClosingSoon(value?: string | null) {
  if (!value) {
    return false
  }

  const dueTime = new Date(value).getTime()
  if (Number.isNaN(dueTime)) {
    return false
  }

  const diff = dueTime - Date.now()
  const sevenDays = 7 * 24 * 60 * 60 * 1000
  return diff >= 0 && diff <= sevenDays
}

onMounted(fetchTasks)
</script>
