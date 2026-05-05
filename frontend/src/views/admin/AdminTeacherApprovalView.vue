<template>
  <div class="space-y-6">
    <section class="panel-card p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p class="text-sm uppercase tracking-[0.3em] text-indigo-500">Teacher Review</p>
          <h2 class="mt-2 text-2xl font-semibold text-slate-900">教师注册审核</h2>
          <p class="mt-2 text-sm text-slate-500">教师注册后默认进入待审核，管理员审核通过后才能登录。</p>
        </div>

        <div class="flex gap-3">
          <button
            v-for="item in tabs"
            :key="item.value"
            type="button"
            class="rounded-full border px-4 py-2 text-sm font-medium transition"
            :class="status === item.value
              ? 'border-indigo-500 bg-indigo-50 text-indigo-700'
              : 'border-slate-200 text-slate-500 hover:border-indigo-200 hover:text-indigo-700'"
            @click="switchStatus(item.value)"
          >
            {{ item.label }}
          </button>
        </div>
      </div>
    </section>

    <section class="panel-card overflow-hidden">
      <div v-if="errorMessage" class="border-b border-rose-200 bg-rose-50 px-6 py-4 text-sm text-rose-600">
        {{ errorMessage }}
      </div>

      <div v-if="loading" class="px-6 py-12 text-center text-sm text-slate-500">加载中...</div>

      <div v-else-if="list.length === 0" class="px-6 py-12 text-center text-sm text-slate-500">
        当前没有{{ currentStatusLabel }}记录。
      </div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-slate-200 text-sm">
          <thead class="bg-slate-50 text-left text-slate-500">
            <tr>
              <th class="px-6 py-4 font-medium">账号</th>
              <th class="px-6 py-4 font-medium">学校</th>
              <th class="px-6 py-4 font-medium">教工号</th>
              <th class="px-6 py-4 font-medium">状态</th>
              <th class="px-6 py-4 font-medium">申请时间</th>
              <th class="px-6 py-4 font-medium">备注</th>
              <th class="px-6 py-4 font-medium text-right">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 bg-white">
            <tr v-for="item in list" :key="item.userId">
              <td class="px-6 py-4">
                <div class="font-medium text-slate-900">{{ item.account }}</div>
                <div class="mt-1 text-xs text-slate-500">{{ item.nickname || item.realName }}</div>
              </td>
              <td class="px-6 py-4 text-slate-700">{{ item.schoolName || '--' }}</td>
              <td class="px-6 py-4 text-slate-700">{{ item.teacherNo || '--' }}</td>
              <td class="px-6 py-4">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-medium" :class="statusClass(item.status)">
                  {{ statusLabel(item.status) }}
                </span>
              </td>
              <td class="px-6 py-4 text-slate-500">{{ formatDateTime(item.createdAt) }}</td>
              <td class="px-6 py-4 text-slate-500">{{ item.reviewRemark || '--' }}</td>
              <td class="px-6 py-4">
                <div v-if="item.status === 'pending'" class="flex justify-end gap-2">
                  <button
                    type="button"
                    class="rounded-full border border-emerald-200 px-4 py-2 text-xs font-medium text-emerald-700 transition hover:bg-emerald-50 disabled:opacity-60"
                    :disabled="pendingUserId === item.userId"
                    @click="review(item.userId, 'approve')"
                  >
                    通过
                  </button>
                  <button
                    type="button"
                    class="rounded-full border border-rose-200 px-4 py-2 text-xs font-medium text-rose-700 transition hover:bg-rose-50 disabled:opacity-60"
                    :disabled="pendingUserId === item.userId"
                    @click="review(item.userId, 'reject')"
                  >
                    驳回
                  </button>
                </div>
                <div v-else class="text-right text-xs text-slate-400">已处理</div>
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
import { approveTeacherRegistration, getTeacherRegistrations, rejectTeacherRegistration } from '@/api/modules/admin'
import type { TeacherRegistrationItem } from '@/api/types'
import { useAppStore } from '@/stores/app'
import { formatDateTime } from '@/utils/format'
import { getErrorMessage } from '@/utils/request'

type ReviewAction = 'approve' | 'reject'
type TeacherStatus = 'pending' | 'active' | 'rejected'

const appStore = useAppStore()
const loading = ref(false)
const errorMessage = ref('')
const pendingUserId = ref<number | null>(null)
const status = ref<TeacherStatus>('pending')
const list = ref<TeacherRegistrationItem[]>([])

const tabs: Array<{ value: TeacherStatus; label: string }> = [
  { value: 'pending', label: '待审核' },
  { value: 'active', label: '已通过' },
  { value: 'rejected', label: '已驳回' },
]

const currentStatusLabel = computed(() => tabs.find((item) => item.value === status.value)?.label || '待审核')

async function loadList() {
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await getTeacherRegistrations(status.value)
    list.value = response.list
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '教师审核列表加载失败')
  } finally {
    loading.value = false
  }
}

async function switchStatus(nextStatus: TeacherStatus) {
  if (status.value === nextStatus) {
    return
  }
  status.value = nextStatus
  await loadList()
}

async function review(userId: number, action: ReviewAction) {
  pendingUserId.value = userId

  try {
    if (action === 'approve') {
      await approveTeacherRegistration(userId, {})
      appStore.showToast('教师账号已审核通过', 'success')
    } else {
      await rejectTeacherRegistration(userId, {})
      appStore.showToast('教师账号已驳回', 'success')
    }
    await loadList()
  } catch (error) {
    appStore.showToast(getErrorMessage(error, action === 'approve' ? '审核通过失败' : '驳回失败'), 'error')
  } finally {
    pendingUserId.value = null
  }
}

function statusLabel(value: string) {
  switch (value) {
    case 'pending':
      return '待审核'
    case 'active':
      return '已通过'
    case 'rejected':
      return '已驳回'
    default:
      return value || '未知'
  }
}

function statusClass(value: string) {
  switch (value) {
    case 'pending':
      return 'bg-amber-50 text-amber-700'
    case 'active':
      return 'bg-emerald-50 text-emerald-700'
    case 'rejected':
      return 'bg-rose-50 text-rose-700'
    default:
      return 'bg-slate-100 text-slate-600'
  }
}

onMounted(() => {
  void loadList()
})
</script>
