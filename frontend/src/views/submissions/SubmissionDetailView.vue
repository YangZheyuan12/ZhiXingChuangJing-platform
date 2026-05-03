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

    <div v-if="detail" class="flex items-center gap-3 rounded-2xl bg-white p-4 shadow-sm">
      <span class="text-sm text-slate-500">当前状态：</span>
      <StatusPill :value="detail.submissionStatus" />
      <span v-if="detail.submissionStatus === 'approved'" class="text-xs text-emerald-700">该提交已通过审核，可进行发布</span>
      <span v-else-if="detail.submissionStatus === 'returned'" class="text-xs text-rose-700">该提交已退回，请提交人修改后重新提交</span>
    </div>

    <div class="grid gap-6 lg:grid-cols-4">
      <MetricTile label="提交版本" :value="detail?.versionNo ?? '--'" hint="对应展厅版本号" />
      <MetricTile label="提交状态" :value="statusText" hint="submitted / reviewed / approved / returned" />
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
              <input v-model.number="reviewForm.score" type="number" min="0" max="100" class="form-control" :disabled="isFinalized" />
            </label>
            <label class="block">
              <span class="form-label">点评内容</span>
              <textarea v-model="reviewForm.commentText" rows="4" class="form-textarea" :disabled="isFinalized" />
            </label>
            <label class="flex items-center gap-3 text-sm text-slate-600">
              <input v-model="reviewForm.isPublic" type="checkbox" class="form-checkbox" :disabled="isFinalized" />
              公开展示给学生
            </label>
            <button
              type="submit"
              :disabled="submittingReview || isFinalized"
              class="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-medium text-white transition hover:bg-brand-700 disabled:bg-slate-300"
            >
              {{ submittingReview ? '提交中...' : '保存点评' }}
            </button>
            <p v-if="isFinalized" class="text-xs text-slate-400">当前提交已进入终态，不允许追加点评。</p>
          </form>
        </div>

        <div v-if="canReview && !isFinalized" class="panel-card p-6">
          <SectionHeader title="审核决策" description="选择通过或退回提交。通过后提交人可发布展厅；退回后提交人需修改后重新提交。" />

          <div v-if="actionMode === null" class="mt-4 flex flex-wrap gap-3">
            <button
              type="button"
              class="flex-1 min-w-[8rem] rounded-2xl bg-emerald-600 px-5 py-3 text-sm font-medium text-white transition hover:bg-emerald-700"
              @click="actionMode = 'approve'"
            >
              ✓ 通过提交
            </button>
            <button
              type="button"
              class="flex-1 min-w-[8rem] rounded-2xl border border-rose-300 bg-rose-50 px-5 py-3 text-sm font-medium text-rose-700 transition hover:bg-rose-100"
              @click="actionMode = 'return'"
            >
              ✖ 退回提交
            </button>
          </div>

          <div v-else-if="actionMode === 'approve'" class="mt-4 space-y-3 rounded-2xl bg-emerald-50/50 p-4">
            <p class="text-sm font-medium text-emerald-800">确认通过该提交？</p>
            <label class="block">
              <span class="form-label">总评（选填）</span>
              <textarea v-model="approveForm.comment" rows="3" class="form-textarea" placeholder="作品主题鲜明，排版仔细..." />
            </label>
            <div class="flex gap-2">
              <button
                type="button"
                :disabled="actionSubmitting"
                class="rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:bg-slate-300"
                @click="handleApprove"
              >
                {{ actionSubmitting ? '提交中...' : '确认通过' }}
              </button>
              <button
                type="button"
                :disabled="actionSubmitting"
                class="rounded-xl px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
                @click="resetActionMode"
              >
                取消
              </button>
            </div>
          </div>

          <div v-else class="mt-4 space-y-3 rounded-2xl bg-rose-50/50 p-4">
            <p class="text-sm font-medium text-rose-800">请说明退回原因</p>
            <label class="block">
              <span class="form-label">退回理由 <span class="text-rose-500">*</span></span>
              <textarea
                v-model="returnForm.reason"
                rows="4"
                class="form-textarea"
                placeholder="例如：提交说明不完整、展区内容与任务主题不符合..."
              />
            </label>
            <div class="flex gap-2">
              <button
                type="button"
                :disabled="actionSubmitting || !returnForm.reason.trim()"
                class="rounded-xl bg-rose-600 px-4 py-2 text-sm font-medium text-white hover:bg-rose-700 disabled:bg-slate-300"
                @click="handleReturn"
              >
                {{ actionSubmitting ? '提交中...' : '确认退回' }}
              </button>
              <button
                type="button"
                :disabled="actionSubmitting"
                class="rounded-xl px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
                @click="resetActionMode"
              >
                取消
              </button>
            </div>
          </div>
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
import { approveSubmission, createSubmissionReview, getSubmissionDetail, returnSubmission } from '@/api/modules/submissions'
import type { SubmissionDetail } from '@/api/types'
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
const submissionId = Number(route.params.submissionId)

const loading = ref(false)
const submittingReview = ref(false)
const errorMessage = ref('')
const detail = ref<SubmissionDetail | null>(null)

const reviewForm = reactive({
  score: 95,
  commentText: '',
  isPublic: true,
})

const actionMode = ref<'approve' | 'return' | null>(null)
const actionSubmitting = ref(false)
const approveForm = reactive({ score: null as number | null, comment: '' })
const returnForm = reactive({ reason: '' })

const canReview = computed(() => ['teacher', 'admin'].includes(authStore.user?.role || ''))
const isFinalized = computed(
  () => detail.value?.submissionStatus === 'approved' || detail.value?.submissionStatus === 'returned',
)
const statusText = computed(() => {
  const map: Record<string, string> = {
    submitted: '待审核',
    reviewed: '已评分',
    approved: '已通过',
    returned: '已退回',
  }
  return map[detail.value?.submissionStatus ?? ''] ?? '--'
})

function resetActionMode() {
  actionMode.value = null
  approveForm.score = null
  approveForm.comment = ''
  returnForm.reason = ''
}

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

async function handleApprove() {
  actionSubmitting.value = true
  try {
    await approveSubmission(submissionId, {
      score: approveForm.score,
      comment: approveForm.comment.trim() || null,
    })
    appStore.showToast('提交已通过审核', 'success')
    resetActionMode()
    await fetchSubmissionDetail()
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '审核通过失败'), 'error')
  } finally {
    actionSubmitting.value = false
  }
}

async function handleReturn() {
  if (!returnForm.reason.trim()) return
  actionSubmitting.value = true
  try {
    await returnSubmission(submissionId, { reason: returnForm.reason.trim() })
    appStore.showToast('提交已退回', 'success')
    resetActionMode()
    await fetchSubmissionDetail()
  } catch (error) {
    appStore.showToast(getErrorMessage(error, '退回提交失败'), 'error')
  } finally {
    actionSubmitting.value = false
  }
}

onMounted(fetchSubmissionDetail)
</script>
