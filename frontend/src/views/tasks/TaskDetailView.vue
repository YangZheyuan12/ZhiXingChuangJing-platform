<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="Task Detail"
      :title="detail?.title || '任务详情'"
      :description="detail?.description || '暂无任务描述。'"
      back-to="/tasks"
      back-label="返回任务中心"
    >
      <template #badge>
        <StatusPill :value="detail?.status" />
      </template>
      <template #meta>
        <span>教师：{{ detail?.teacher.nickname || detail?.teacher.name || '--' }}</span>
        <span>开始：{{ formatDateTime(detail?.startTime) }}</span>
        <span>截止：{{ formatDateTime(detail?.dueTime) }}</span>
      </template>
    </PageHero>

    <div v-if="errorMessage" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-600">
      {{ errorMessage }}
    </div>
    <div class="grid gap-6 lg:grid-cols-3">
      <MetricTile label="优秀展厅" :value="excellentExhibitions.length" hint="来自任务优秀作品接口" />
      <MetricTile label="已提交小组" :value="progress?.submittedCount ?? '--'" hint="用于教师查看进度" />
      <MetricTile label="已评价作品" :value="progress?.reviewedCount ?? '--'" hint="教师点评完成情况" />
    </div>

    <div class="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
      <section class="panel-card p-6">
        <SectionHeader title="任务资料与投放班级" description="整合任务详情中的背景资料和目标班级。" />
        <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
        <div v-else class="space-y-5">
          <div>
            <h3 class="text-sm font-semibold uppercase tracking-[0.18em] text-slate-400">评价标准</h3>
            <p class="mt-3 text-sm leading-7 text-slate-600">{{ detail?.evaluationCriteria || '暂无评价标准说明。' }}</p>
          </div>
          <div>
            <h3 class="text-sm font-semibold uppercase tracking-[0.18em] text-slate-400">背景资料</h3>
            <EmptyStatePanel
              v-if="!detail || detail.backgroundMaterials.length === 0"
              eyebrow="Materials"
              title="暂无背景资料"
              description="教师尚未为该任务配置背景资料。"
            />
            <div v-else class="mt-3 space-y-3">
              <article v-for="material in detail.backgroundMaterials" :key="material.id" class="rounded-2xl border border-slate-200 p-4">
                <div class="flex items-center justify-between gap-3">
                  <h4 class="font-medium text-slate-900">{{ material.title }}</h4>
                  <StatusPill :value="material.materialType" />
                </div>
                <p class="mt-2 text-sm text-slate-500">{{ material.description || '暂无说明' }}</p>
                <a
                  v-if="material.url"
                  :href="material.url"
                  target="_blank"
                  rel="noreferrer"
                  class="mt-3 inline-flex text-sm text-brand-700 transition hover:text-brand-900"
                >
                  打开资料链接
                </a>
              </article>
            </div>
          </div>
          <div>
            <h3 class="text-sm font-semibold uppercase tracking-[0.18em] text-slate-400">投放班级</h3>
            <div class="mt-3 flex flex-wrap gap-2">
              <span
                v-for="targetClass in detail?.targetClasses || []"
                :key="targetClass.id"
                class="rounded-full bg-brand-50 px-3 py-1 text-sm text-brand-700"
              >
                {{ targetClass.name }}
              </span>
            </div>
          </div>
        </div>
      </section>

      <section class="space-y-6">
        <div class="panel-card p-6">
          <SectionHeader title="任务提交" description="学生可提交关联展厅，教师可查看记录。" />
          <div v-if="canSubmitTask" class="space-y-4">
            <div class="grid gap-4 md:grid-cols-2">
              <label class="block">
                <span class="form-label">选择展厅</span>
                <select v-model="submissionForm.exhibitionId" class="form-select">
                  <option value="">请选择展厅</option>
                  <option v-for="item in myExhibitions" :key="item.id" :value="String(item.id)">
                    {{ item.title }} · V{{ item.latestVersionNo }}
                  </option>
                </select>
              </label>
              <label class="block">
                <span class="form-label">提交说明</span>
                <input v-model="submissionForm.submitRemark" class="form-control" />
              </label>
            </div>
            <button
              type="button"
              :disabled="submittingTask || !submissionForm.exhibitionId"
              class="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-medium text-white transition hover:bg-brand-700 disabled:bg-slate-300"
              @click="handleSubmitTask"
            >
              {{ submittingTask ? '提交中...' : '提交任务作品' }}
            </button>
          </div>
          <p v-else class="text-sm text-slate-500">当前角色不可直接提交任务作品。</p>
        </div>

        <div class="panel-card p-6">
          <SectionHeader title="优秀作品" description="查看当前任务下被推荐的展厅作品。" />
          <EmptyStatePanel
            v-if="!loading && excellentExhibitions.length === 0"
            eyebrow="Featured"
            title="暂无优秀作品"
            description="该任务还没有被推荐的优秀展厅。"
          />
          <div v-else class="grid gap-4">
            <ExhibitionCard
              v-for="item in excellentExhibitions"
              :key="item.id"
              :exhibition="item"
              :to="`/exhibitions/${item.id}`"
            />
          </div>
        </div>

        <div class="panel-card p-6">
          <SectionHeader title="任务进度" description="适合教师查看提交与评审情况。" />
          <EmptyStatePanel
            v-if="!loading && (!progress || progress.groups.length === 0)"
            eyebrow="Progress"
            title="暂无进度数据"
            description="当前任务还没有小组展厅或提交记录。"
          />
          <div v-else class="space-y-3">
            <article v-for="group in progress?.groups || []" :key="group.exhibitionId" class="rounded-2xl border border-slate-200 p-4">
              <div class="flex items-center justify-between gap-4">
                <h4 class="font-medium text-slate-900">{{ group.groupName || `展厅 #${group.exhibitionId}` }}</h4>
                <StatusPill :value="group.submissionStatus || 'draft'" />
              </div>
              <div class="mt-3 h-2 overflow-hidden rounded-full bg-slate-100">
                <div class="h-full rounded-full bg-brand-600" :style="{ width: `${group.progressPercent}%` }" />
              </div>
              <div class="mt-3 flex flex-wrap gap-4 text-xs text-slate-400">
                <span>组长：{{ group.leaderName || '--' }}</span>
                <span>人数：{{ group.memberCount }}</span>
                <span>进度：{{ group.progressPercent }}%</span>
                <span>更新时间：{{ formatDateTime(group.updatedAt) }}</span>
              </div>
            </article>
          </div>
        </div>

        <div v-if="canViewSubmissions" class="panel-card p-6">
          <SectionHeader title="提交记录" description="支持查看每条提交并进入点评详情页。" />
          <EmptyStatePanel
            v-if="!loading && submissions.length === 0"
            eyebrow="Submissions"
            title="暂无提交记录"
            description="当前任务还没有小组提交作品。"
          />
          <div v-else class="space-y-3">
            <article v-for="submission in submissions" :key="submission.id" class="rounded-2xl border border-slate-200 p-4">
              <div class="flex items-center justify-between gap-3">
                <div>
                  <h4 class="font-medium text-slate-900">{{ submission.submitter.nickname || submission.submitter.name }}</h4>
                  <p class="mt-1 text-xs text-slate-400">提交时间：{{ formatDateTime(submission.submittedAt) }}</p>
                </div>
                <StatusPill :value="submission.submissionStatus" />
              </div>
              <p class="mt-3 text-sm text-slate-500">{{ submission.submitRemark || '暂无提交说明。' }}</p>
              <RouterLink
                :to="`/submissions/${submission.id}`"
                class="mt-4 inline-flex rounded-full bg-brand-50 px-3 py-1 text-sm text-brand-700 transition hover:bg-brand-100"
              >
                查看提交详情
              </RouterLink>
            </article>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getErrorMessage } from '@/utils/request'
import { getMyExhibitions } from '@/api/modules/exhibitions'
import { getTaskDetail, getTaskExcellentExhibitions, getTaskProgress, getTaskSubmissions, submitTaskWork } from '@/api/modules/tasks'
import type { ExhibitionSummary, SubmissionDetail, TaskDetail, TaskProgress } from '@/api/types'
import ExhibitionCard from '@/components/business/ExhibitionCard.vue'
import EmptyStatePanel from '@/components/common/EmptyStatePanel.vue'
import MetricTile from '@/components/common/MetricTile.vue'
import PageHero from '@/components/common/PageHero.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import StatusPill from '@/components/common/StatusPill.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()
const taskId = Number(route.params.taskId)

const loading = ref(false)
const submittingTask = ref(false)
const errorMessage = ref('')
const detail = ref<TaskDetail | null>(null)
const progress = ref<TaskProgress | null>(null)
const excellentExhibitions = ref<ExhibitionSummary[]>([])
const submissions = ref<SubmissionDetail[]>([])
const myExhibitions = ref<ExhibitionSummary[]>([])
const submissionForm = reactive({
  exhibitionId: '',
  submitRemark: '',
})

const canSubmitTask = computed(() => ['student', 'teacher', 'admin'].includes(authStore.user?.role || ''))
const canViewSubmissions = computed(() => ['teacher', 'admin'].includes(authStore.user?.role || ''))

async function fetchTaskDetail() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [detailData, progressData, excellentData, submissionData, exhibitionData] = await Promise.all([
      getTaskDetail(taskId),
      getTaskProgress(taskId).catch(() => null),
      getTaskExcellentExhibitions(taskId).catch(() => []),
      canViewSubmissions.value ? getTaskSubmissions(taskId).catch(() => []) : Promise.resolve([]),
      canSubmitTask.value ? getMyExhibitions({ taskId, page: 1, pageSize: 20 }).then((page) => page.list).catch(() => []) : Promise.resolve([]),
    ])
    detail.value = detailData
    progress.value = progressData
    excellentExhibitions.value = excellentData
    submissions.value = submissionData
    myExhibitions.value = exhibitionData
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '任务详情加载失败')
  } finally {
    loading.value = false
  }
}

async function handleSubmitTask() {
  submittingTask.value = true
  errorMessage.value = ''

  try {
    const result = await submitTaskWork(taskId, {
      exhibitionId: Number(submissionForm.exhibitionId),
      submitRemark: submissionForm.submitRemark || null,
    })
    appStore.showToast(`提交成功，记录编号 #${result.id}`, 'success')
    submissionForm.exhibitionId = ''
    submissionForm.submitRemark = ''
    await fetchTaskDetail()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '任务提交失败')
  } finally {
    submittingTask.value = false
  }
}

onMounted(fetchTaskDetail)
</script>
