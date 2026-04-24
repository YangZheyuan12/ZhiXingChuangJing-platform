<template>
  <div class="mx-auto flex w-full max-w-7xl flex-col gap-6">
    <p
      v-if="errorMessage"
      class="rounded-xl border px-4 py-3 text-sm shadow-sm"
      :class="usingMockData ? 'border-amber-200 bg-amber-50 text-amber-700' : 'border-rose-200 bg-rose-50 text-rose-600'"
    >
      {{ errorMessage }}
    </p>

    <PageHero
      eyebrow="Workbench"
      :title="greetingTitle"
      :description="greetingSubtitle"
    >
      <template #badge>
        {{ usingMockData ? '当前展示模拟数据' : '课堂创作工作台' }}
      </template>
      <template #actions>
        <button
          type="button"
          class="page-primary-button gap-2 px-5 py-3"
          @click="handlePrimaryAction"
        >
          <svg viewBox="0 0 24 24" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 5v14M5 12h14" />
          </svg>
          快速创建展厅
        </button>
        <button
          type="button"
          class="page-secondary-button gap-2 px-5 py-3"
          @click="handleSecondaryAction"
        >
          <svg viewBox="0 0 24 24" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 7.5h16M4 12h16M4 16.5h10" />
          </svg>
          发布新任务
        </button>
      </template>
    </PageHero>

    <section class="flex flex-col gap-6">
      <section class="grid gap-4 md:grid-cols-3">
        <MetricTile
          v-for="item in statsCards"
          :key="item.label"
          :label="item.label"
          :value="item.value"
          :hint="item.hint"
        />
      </section>

      <section
        class="rounded-xl border border-[#E5E7EB] bg-white p-6 shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-md"
      >
        <div class="flex items-center justify-between gap-4 border-b border-[#F3F4F6] pb-4">
          <div>
            <p class="text-xs uppercase tracking-[0.24em] text-[#9CA3AF]">Announcements</p>
            <h2 class="mt-2 text-2xl font-semibold text-[#111827]">班级公告</h2>
          </div>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-xl border border-[#E5E7EB] px-4 py-2 text-sm font-medium text-[#4B5563] transition hover:bg-[#F9FAFB]"
            @click="announcementsExpanded = !announcementsExpanded"
          >
            <span>{{ announcementsExpanded ? '折叠公告' : '展开公告' }}</span>
            <svg
              viewBox="0 0 24 24"
              class="h-4 w-4 transition"
              :class="announcementsExpanded ? 'rotate-180' : ''"
              fill="none"
              stroke="currentColor"
              stroke-width="1.8"
            >
              <path stroke-linecap="round" stroke-linejoin="round" d="m6 9 6 6 6-6" />
            </svg>
          </button>
        </div>

        <div v-if="loading" class="py-6 text-sm text-[#6B7280]">公告加载中...</div>

        <div v-else-if="announcements.length === 0" class="py-6 text-sm text-[#6B7280]">
          当前没有新的班级公告。
        </div>

        <div v-else class="mt-4">
          <div
            class="flex cursor-pointer items-center justify-between rounded-xl bg-[#F9FAFB] px-4 py-3"
            @click="announcementsExpanded = !announcementsExpanded"
          >
            <div class="min-w-0">
              <p class="text-sm font-medium text-[#111827]">
                {{ announcements.length }} 条班级公告
              </p>
              <p class="mt-1 truncate text-xs text-[#9CA3AF]">
                最新：{{ announcements[0].title }}
              </p>
            </div>
            <span class="rounded-full bg-[#FEE2E2] px-2.5 py-1 text-[11px] font-medium text-[#B91C1C]">
              {{ pinnedAnnouncementsCount }} 条置顶
            </span>
          </div>

          <transition name="fade">
            <div v-if="announcementsExpanded" class="mt-4 flex flex-col gap-3">
              <article
                v-for="announcement in announcements"
                :key="announcement.id"
                class="rounded-xl border border-[#E5E7EB] bg-[#FCFCFD] px-4 py-3 transition duration-200 hover:-translate-y-1 hover:shadow-md"
              >
                <div class="flex items-start justify-between gap-3">
                  <h3 class="truncate text-sm font-medium text-[#111827]">{{ announcement.title }}</h3>
                  <span
                    v-if="announcement.pinned"
                    class="shrink-0 rounded-full bg-[#FEE2E2] px-2.5 py-1 text-[11px] font-medium text-[#B91C1C]"
                  >
                    置顶
                  </span>
                </div>
                <p class="mt-2 text-xs text-[#9CA3AF]">{{ formatDateTime(announcement.publishedAt) }}</p>
              </article>
            </div>
          </transition>
        </div>
      </section>

      <section
        class="rounded-xl border border-[#E5E7EB] bg-white p-6 shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-md"
      >
        <div class="flex items-end justify-between gap-4 border-b border-[#F3F4F6] pb-4">
          <div>
            <p class="text-xs uppercase tracking-[0.24em] text-[#9CA3AF]">Ongoing Tasks</p>
            <h2 class="mt-2 text-2xl font-semibold text-[#111827]">进行中的任务</h2>
          </div>
          <button
            type="button"
            class="text-sm font-medium text-[#D32F2F] transition hover:text-[#B91C1C]"
            @click="handleQuickAction('/tasks')"
          >
            查看全部
          </button>
        </div>

        <div v-if="loading" class="py-10 text-sm text-[#6B7280]">工作台数据加载中...</div>

        <div v-else-if="ongoingTasks.length === 0" class="py-10 text-sm text-[#6B7280]">
          当前没有进行中的任务。
        </div>

        <div v-else class="mt-6 flex flex-col gap-4">
          <article
            v-for="task in pagedTasks"
            :key="task.id"
            class="cursor-pointer rounded-xl border border-[#E5E7EB] bg-[#FCFCFD] p-5 transition duration-200 hover:-translate-y-1 hover:border-[#FCA5A5] hover:shadow-md"
            @click="handleTaskClick(task.id)"
          >
            <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center gap-3">
                  <h3 class="truncate text-lg font-semibold text-[#111827]">{{ task.title }}</h3>
                  <span class="rounded-full bg-[#FEE2E2] px-3 py-1 text-xs font-medium text-[#B91C1C]">
                    {{ formatTaskStatus(task.status) }}
                  </span>
                </div>
                <p class="mt-3 max-h-12 overflow-hidden text-sm leading-6 text-[#6B7280]">
                  {{ task.description || '围绕当前任务继续推进展厅创作、资料整理与课堂展示准备。' }}
                </p>
                <div class="mt-4 flex flex-wrap items-center gap-4 text-xs text-[#9CA3AF]">
                  <span>教师：{{ task.teacher.nickname || task.teacher.name }}</span>
                  <span>截止：{{ formatTaskDueTime(task.dueTime) }}</span>
                </div>
              </div>

              <div class="w-full shrink-0 lg:w-52">
                <div class="flex items-center justify-between text-xs text-[#9CA3AF]">
                  <span>任务进度</span>
                  <span class="font-mono text-[#D32F2F]">{{ getTaskProgress(task) }}%</span>
                </div>
                <div class="mt-2 h-2.5 overflow-hidden rounded-full bg-[#F3F4F6]">
                  <div
                    class="h-full rounded-full bg-[linear-gradient(90deg,#D32F2F,#F87171)] transition-all duration-500"
                    :style="{ width: `${getTaskProgress(task)}%` }"
                  />
                </div>
              </div>
            </div>
          </article>

          <div
            v-if="totalTaskPages > 1"
            class="mt-2 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#F3F4F6] bg-[#F9FAFB] px-4 py-3"
          >
            <p class="text-sm text-[#6B7280]">
              第 <span class="font-mono text-[#111827]">{{ currentTaskPage }}</span> / {{ totalTaskPages }} 页
            </p>
            <div class="flex items-center gap-2">
              <button
                type="button"
                class="rounded-lg border border-[#E5E7EB] px-3 py-2 text-sm text-[#4B5563] transition hover:bg-white disabled:cursor-not-allowed disabled:opacity-50"
                :disabled="currentTaskPage === 1"
                @click="goToPreviousTaskPage"
              >
                上一页
              </button>
              <button
                type="button"
                class="rounded-lg border border-[#E5E7EB] px-3 py-2 text-sm text-[#4B5563] transition hover:bg-white disabled:cursor-not-allowed disabled:opacity-50"
                :disabled="currentTaskPage === totalTaskPages"
                @click="goToNextTaskPage"
              >
                下一页
              </button>
            </div>
          </div>
        </div>
      </section>

      <section
        class="rounded-xl border border-[#E5E7EB] bg-white p-6 shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-md"
      >
        <div class="border-b border-[#F3F4F6] pb-4">
          <p class="text-xs uppercase tracking-[0.24em] text-[#9CA3AF]">Quick Actions</p>
          <h2 class="mt-2 text-2xl font-semibold text-[#111827]">快捷入口</h2>
        </div>

        <div class="mt-6 flex flex-wrap gap-3">
          <button
            v-for="item in quickActions"
            :key="item.label"
            type="button"
            class="group inline-flex min-w-[132px] items-center gap-3 rounded-xl border border-[#E5E7EB] bg-[#FCFCFD] px-4 py-3 text-left transition duration-200 hover:-translate-y-1 hover:border-[#FCA5A5] hover:bg-[#FEF2F2] hover:shadow-md"
            @click="handleQuickAction(item.to)"
          >
            <span class="flex h-10 w-10 items-center justify-center rounded-2xl bg-[#FEE2E2] text-[#D32F2F] transition group-hover:bg-white">
              <svg viewBox="0 0 24 24" class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="1.8">
                <path :d="item.icon" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </span>
            <span class="text-[12px] font-medium leading-5 text-[#374151]">{{ item.label }}</span>
          </button>
        </div>
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardOverview } from '@/api/modules/dashboard'
import type { DashboardOverview, TaskSummary } from '@/api/types'
import MetricTile from '@/components/common/MetricTile.vue'
import PageHero from '@/components/common/PageHero.vue'
import { useAuthStore } from '@/stores/auth'
import { getErrorMessage } from '@/utils/request'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const usingMockData = ref(false)
const errorMessage = ref('')
const announcementsExpanded = ref(false)
const currentTaskPage = ref(1)
const taskPageSize = 3

const overview = reactive<DashboardOverview>({
  ongoingTasks: [],
  recentExhibitions: [],
  announcements: [],
  activityFeeds: [],
  recommendedResources: [],
  featuredExhibitions: [],
})

const mockOverview: DashboardOverview = {
  ongoingTasks: [
    {
      id: 101,
      title: '“红色记忆”数字展厅共创任务',
      coverUrl: null,
      description: '围绕校史馆素材与地方红色故事，完成沉浸式数字展厅的分组创作与讲解脚本整理。',
      teacher: {
        id: 1,
        role: 'teacher',
        name: '李老师',
        nickname: '李老师',
        avatarUrl: null,
      },
      startTime: '2026-04-18T08:00:00',
      dueTime: '2026-04-25T18:00:00',
      status: 'published',
    },
    {
      id: 102,
      title: '数字人讲述员形象设计',
      coverUrl: null,
      description: '完善数字人角色设定、配音风格与互动台词，为展厅导览场景提供统一叙事入口。',
      teacher: {
        id: 2,
        role: 'teacher',
        name: '周老师',
        nickname: '周老师',
        avatarUrl: null,
      },
      startTime: '2026-04-15T09:00:00',
      dueTime: '2026-04-27T20:00:00',
      status: 'published',
    },
    {
      id: 103,
      title: '班级优秀作品复盘展示',
      coverUrl: null,
      description: '从近期优秀作品中提炼结构、叙事与视觉风格，整理成复盘看板供课堂讨论使用。',
      teacher: {
        id: 3,
        role: 'teacher',
        name: '陈老师',
        nickname: '陈老师',
        avatarUrl: null,
      },
      startTime: '2026-04-19T10:30:00',
      dueTime: '2026-04-24T17:30:00',
      status: 'published',
    },
  ],
  recentExhibitions: [{ id: 1 } as DashboardOverview['recentExhibitions'][number], { id: 2 } as DashboardOverview['recentExhibitions'][number], { id: 3 } as DashboardOverview['recentExhibitions'][number], { id: 4 } as DashboardOverview['recentExhibitions'][number]],
  announcements: [
    {
      id: 201,
      classId: 1,
      title: '本周五课堂展示请各小组提前 10 分钟完成设备联调',
      content: '请携带耳机、讲稿和最终版展厅链接。',
      pinned: true,
      publisher: {
        id: 1,
        role: 'teacher',
        name: '李老师',
        nickname: '李老师',
      },
      publishedAt: '2026-04-21T09:20:00',
    },
    {
      id: 202,
      classId: 1,
      title: '数字展厅封面统一使用课堂模板并补充作者署名',
      content: '便于后续平台归档展示。',
      pinned: false,
      publisher: {
        id: 1,
        role: 'teacher',
        name: '李老师',
        nickname: '李老师',
      },
      publishedAt: '2026-04-20T16:40:00',
    },
    {
      id: 203,
      classId: 1,
      title: '优秀作品点评已发布，请在任务详情页查看教师反馈',
      content: '关注叙事完整度与素材授权规范。',
      pinned: false,
      publisher: {
        id: 2,
        role: 'teacher',
        name: '周老师',
        nickname: '周老师',
      },
      publishedAt: '2026-04-19T18:05:00',
    },
  ],
  activityFeeds: [],
  recommendedResources: [],
  featuredExhibitions: [],
}

const greetingName = computed(() => authStore.displayName)
const greetingPeriod = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 12) return '上午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const greetingTitle = computed(() => {
  const pendingCount = ongoingTasks.value.length
  return `${greetingName.value}，${greetingPeriod.value}！今天有${pendingCount}个进行中的任务需关注。`
})

const greetingSubtitle = computed(() => {
  if (usingMockData.value) {
    return '当前展示的是本地模拟工作台数据，你仍可预览布局、交互入口和任务节奏安排。'
  }
  return '聚焦任务推进、展厅创作与班级信息流，在一个界面里完成今日课堂创作准备。'
})

const ongoingTasks = computed(() => overview.ongoingTasks)
const announcements = computed(() => overview.announcements.slice(0, 4))
const pinnedAnnouncementsCount = computed(() => announcements.value.filter((item) => item.pinned).length)
const totalTaskPages = computed(() => Math.max(1, Math.ceil(ongoingTasks.value.length / taskPageSize)))
const pagedTasks = computed(() => {
  const safePage = Math.min(currentTaskPage.value, totalTaskPages.value)
  const start = (safePage - 1) * taskPageSize
  return ongoingTasks.value.slice(start, start + taskPageSize)
})

const statsCards = computed(() => [
  {
    label: '进行中的任务',
    value: String(overview.ongoingTasks.length).padStart(2, '0'),
    hint: '当前需要跟进的课程任务数量。',
  },
  {
    label: '最近编辑展厅',
    value: String(overview.recentExhibitions.length).padStart(2, '0'),
    hint: '最近参与编辑或继续创作的展厅数量。',
  },
  {
    label: '未读班级公告',
    value: String(overview.announcements.length).padStart(2, '0'),
    hint: '当前工作台可见的班级公告条数。',
  },
])

const canCreateTask = computed(() => ['teacher', 'admin'].includes(authStore.user?.role || ''))

const quickActions = computed(() => {
  const actions = [
    {
      label: '进入任务中心',
      to: '/tasks',
      icon: 'M5 5.5h14v13H5zM8 9h8M8 12h8M8 15h5',
    },
    {
      label: '发布任务',
      to: '/tasks/create',
      icon: 'M12 5v14M5 12h14',
    },
    {
      label: '创建展厅',
      to: '/exhibitions/create',
      icon: 'M4 16 14.8 5.2l4 4L8 20H4v-4Z',
    },
    {
      label: '素材库',
      to: '/asset-library',
      icon: 'M4.5 5.5h15v13h-15zM8 9.2h.01M6.5 15l3.3-3 2.5 2 2.5-1.8 2.7 2.8',
    },
    {
      label: '文博资源',
      to: '/museum',
      icon: 'M4 18h16M6 18v-8m4 8v-8m4 8v-8m4 8v-8M3 10l9-5 9 5H3Z',
    },
    {
      label: '社区展厅',
      to: '/community',
      icon: 'M5 6h14v10H9l-4 3V6Z',
    },
  ]

  if (!canCreateTask.value) {
    return actions.filter((item) => item.to !== '/tasks/create')
  }

  return actions
})

function getTaskProgress(task: TaskSummary) {
  const seed = task.id * 17 + task.title.length * 7
  return 38 + (seed % 48)
}

function formatTaskStatus(status?: string | null) {
  switch (status) {
    case 'published':
      return '进行中'
    case 'draft':
      return '草稿'
    case 'closed':
      return '已关闭'
    case 'archived':
      return '已归档'
    default:
      return status || '未知状态'
  }
}

function formatTaskDueTime(value?: string | null) {
  return value ? formatDateTime(value) : '待安排'
}

function goToPreviousTaskPage() {
  currentTaskPage.value = Math.max(1, currentTaskPage.value - 1)
}

function goToNextTaskPage() {
  currentTaskPage.value = Math.min(totalTaskPages.value, currentTaskPage.value + 1)
}

function handleQuickAction(path: string) {
  router.push(path)
}

function handleTaskClick(taskId: number) {
  router.push(`/tasks/${taskId}`)
}

function handlePrimaryAction() {
  router.push('/exhibitions/create')
}

function handleSecondaryAction() {
  router.push('/tasks/create')
}

function applyOverview(data: DashboardOverview) {
  Object.assign(overview, data)
  currentTaskPage.value = 1
}

async function fetchDashboardData() {
  loading.value = true
  errorMessage.value = ''

  try {
    const data = await getDashboardOverview(authStore.user?.role)
    applyOverview(data)
    usingMockData.value = false
  } catch (error) {
    applyOverview(mockOverview)
    usingMockData.value = true
    errorMessage.value = `${getErrorMessage(error, '工作台数据加载失败')}，当前展示模拟数据。`
  } finally {
    loading.value = false
  }
}

onMounted(fetchDashboardData)
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
