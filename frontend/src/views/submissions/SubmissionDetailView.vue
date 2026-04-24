<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="Submission Detail"
      title="提交记录详情"
      description="查看作品提交状态、历史点评，并支持教师/管理员直接在当前页面完成评分和反馈。"
      :back-to="detail ? `/tasks/${detail.taskId}` : '/tasks'"
      back-label="返回任务详情"
    />

    <p v-if="errorMessage" class="rounded-xl bg-rose-50 px-4 py-3 text-sm text-rose-600">{{ errorMessage }}</p>

    <div class="grid gap-6 lg:grid-cols-4">
      <MetricTile label="提交版本" :value="detail?.versionNo ?? '--'" hint="对应展厅版本号" />
      <MetricTile label="提交状态" :value="detail?.submissionStatus ?? '--'" hint="submitted / reviewed" />
      <MetricTile label="点评数量" :value="detail?.reviews.length ?? 0" hint="含当前教师历史点评" />
      <MetricTile label="提交人" :value="detail?.submitter.nickname || detail?.submitter.name || '--'" hint="提交作品的账号" />
    </div>

    <div class="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
      <section class="panel-card p-6">
        <SectionHeader title="提交信息" description="查看当前作品的提交记录与时间信息。" />
        <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
        <div v-else-if="detail" class="space-y-4">
          <div class="rounded-2xl bg-brand-50 p-5">
            <p class="text-sm text-brand-700">提交说明</p>
            <p class="mt-3 text-sm leading-7 text-slate-700">{{ detail.submitRemark || '暂无提交备注。' }}</p>
          </div>
          <dl class="space-y-3 text-sm">
            <div class="flex justify-between gap-4 border-b border-slate-100 pb-3">
              <dt class="text-slate-500">任务ID</dt>
              <dd class="text-slate-900">{{ detail.taskId }}</dd>
            </div>
            <div class="flex justify-between gap-4 border-b border-slate-100 pb-3">
              <dt class="text-slate-500">展厅ID</dt>
              <dd class="text-slate-900">{{ detail.exhibitionId }}</dd>
            </div>
            <div class="flex justify-between gap-4 border-b border-slate-100 pb-3">
              <dt class="text-slate-500">提交时间</dt>
              <dd class="text-slate-900">{{ formatDateTime(detail.submittedAt) }}</dd>
            </div>
            <div class="flex justify-between gap-4">
              <dt class="text-slate-500">评审时间</dt>
              <dd class="text-slate-900">{{ formatDateTime(detail.reviewedAt) }}</dd>
            </div>
          </dl>
          <div class="flex gap-3">
            <RouterLink
              :to="`/exhibitions/${detail.exhibitionId}`"
              class="rounded-2xl border border-brand-200 px-4 py-3 text-sm text-brand-700 transition hover:bg-brand-50"
            >
              查看展厅
            </RouterLink>
            <RouterLink
              v-if="detail.submitter.id"
              :to="`/users/${detail.submitter.id}/homepage`"
              class="rounded-2xl border border-slate-200 px-4 py-3 text-sm text-slate-600 transition hover:border-brand-300 hover:text-brand-700"
            >
              访问提交人主页
            </RouterLink>
          </div>
        </div>
      </section>

      <section class="space-y-6">
        <div v-if="canReview" class="panel-card p-6">
          <SectionHeader title="教师点评" description="填写评分与反馈内容。" />
          <form class="space-y-4" @submit.prevent="handleReviewSubmit">
            <label class="block">
              <span class="form-label">评分</span>
              <input v-model.number="reviewForm.score" type="number" min="0" max="100" class="form-control" />
            </label>
            <label class="block">
              <span class="form-label">点评内容</span>
              <textarea v-model="reviewForm.commentText" rows="4" class="form-textarea" />
            </label>
            <label class="flex items-center gap-3 text-sm text-slate-600">
              <input v-model="reviewForm.isPublic" type="checkbox" class="form-checkbox" />
              公开展示给学生
            </label>
            <button
              type="submit"
              :disabled="submittingReview"
              class="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-medium text-white transition hover:bg-brand-700 disabled:bg-slate-300"
            >
              {{ submittingReview ? '提交中...' : '提交点评' }}
            </button>
          </form>
        </div>

        <div class="panel-card p-6">
          <SectionHeader title="历史点评" description="显示当前提交记录的点评列表。" />
          <EmptyStatePanel
            v-if="!loading && (!detail || detail.reviews.length === 0)"
            eyebrow="Reviews"
            title="暂无点评"
            description="当前提交记录还没有教师点评。"
          />
          <div v-else class="space-y-3">
            <article v-for="review in detail?.reviews || []" :key="review.id" class="rounded-2xl border border-slate-200 p-4">
              <div class="flex items-center justify-between gap-3">
                <h3 class="font-medium text-slate-900">{{ review.reviewer.nickname || review.reviewer.name }}</h3>
                <span class="text-sm text-brand-700">{{ review.score ?? '--' }} 分</span>
              </div>
              <p class="mt-3 text-sm leading-6 text-slate-500">{{ review.commentText || '暂无点评内容。' }}</p>
              <p class="mt-3 text-xs text-slate-400">{{ formatDateTime(review.createdAt) }}</p>
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
import { createSubmissionReview, getSubmissionDetail } from '@/api/modules/submissions'
import type { SubmissionDetail } from '@/api/types'
import EmptyStatePanel from '@/components/common/EmptyStatePanel.vue'
import MetricTile from '@/components/common/MetricTile.vue'
import PageHero from '@/components/common/PageHero.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()
const submissionId = Number(route.params.submissionId)

const loading = ref(false)
const submittingReview = ref(false)
const errorMessage = ref('')
const detail = ref<SubmissionDetail | null>(null)

const reviewForm = reactive({
  score: 95,
  commentText: '主题表达完整，叙事结构清晰，建议进一步增强视觉层次与互动引导。',
  isPublic: true,
})

const canReview = computed(() => ['teacher', 'admin'].includes(authStore.user?.role || ''))

async function fetchSubmissionDetail() {
  loading.value = true
  errorMessage.value = ''

  try {
    detail.value = await getSubmissionDetail(submissionId)
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '提交详情加载失败')
  } finally {
    loading.value = false
  }
}

async function handleReviewSubmit() {
  submittingReview.value = true
  errorMessage.value = ''

  try {
    await createSubmissionReview(submissionId, reviewForm)
    appStore.showToast('点评提交成功', 'success')
    await fetchSubmissionDetail()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '点评提交失败')
  } finally {
    submittingReview.value = false
  }
}

onMounted(fetchSubmissionDetail)
</script>
