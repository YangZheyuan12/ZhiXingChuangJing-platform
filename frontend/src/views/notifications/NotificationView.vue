<template>
  <div class="panel-card p-6">
    <SectionHeader title="消息通知" description="支持批量标记当前页未读通知为已读。">
      <button
        type="button"
        :disabled="reading || unreadIds.length === 0"
        class="rounded-2xl border border-brand-200 px-4 py-2 text-sm font-medium text-brand-700 transition hover:bg-brand-50 disabled:cursor-not-allowed disabled:border-slate-200 disabled:text-slate-400"
        @click="handleReadCurrentPage"
      >
        {{ reading ? '处理中...' : '标记当前页已读' }}
      </button>
    </SectionHeader>

    <p v-if="errorMessage" class="mb-4 rounded-xl bg-rose-50 px-4 py-3 text-sm text-rose-600">{{ errorMessage }}</p>
    <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
    <div v-else-if="notifications.length === 0" class="text-sm text-slate-500">暂时没有消息通知。</div>
    <div v-else class="space-y-3">
      <article v-for="item in notifications" :key="item.id" class="rounded-2xl border border-slate-200 p-4">
        <div class="flex items-center justify-between gap-3">
          <h3 class="font-medium text-slate-900">{{ item.title }}</h3>
          <span
            class="rounded-full px-3 py-1 text-xs"
            :class="item.readStatus === 'unread' ? 'bg-brand-50 text-brand-700' : 'bg-slate-100 text-slate-500'"
          >
            {{ item.readStatus === 'unread' ? '未读' : '已读' }}
          </span>
        </div>
        <p class="mt-2 text-sm text-slate-500">{{ item.content }}</p>
        <p class="mt-3 text-xs text-slate-400">{{ formatDateTime(item.createdAt) }}</p>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getErrorMessage } from '@/utils/request'
import { getNotifications, readNotifications } from '@/api/modules/notifications'
import type { NotificationItem } from '@/api/types'
import SectionHeader from '@/components/common/SectionHeader.vue'
import { useAppStore } from '@/stores/app'
import { formatDateTime } from '@/utils/format'

const appStore = useAppStore()
const loading = ref(false)
const reading = ref(false)
const errorMessage = ref('')
const notifications = ref<NotificationItem[]>([])

const unreadIds = computed(() => notifications.value.filter((item) => item.readStatus === 'unread').map((item) => item.id))

async function fetchNotifications() {
  loading.value = true
  errorMessage.value = ''

  try {
    const page = await getNotifications({ page: 1, pageSize: 20 })
    notifications.value = page.list
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '通知列表加载失败')
  } finally {
    loading.value = false
  }
}

async function handleReadCurrentPage() {
  reading.value = true
  errorMessage.value = ''

  try {
    await readNotifications({ notificationIds: unreadIds.value })
    notifications.value = notifications.value.map((item) =>
      unreadIds.value.includes(item.id)
        ? { ...item, readStatus: 'read', readAt: new Date().toISOString() }
        : item,
    )
    appStore.showToast('当前页未读通知已全部标记为已读', 'success')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '通知标记失败')
  } finally {
    reading.value = false
  }
}

onMounted(fetchNotifications)
</script>
