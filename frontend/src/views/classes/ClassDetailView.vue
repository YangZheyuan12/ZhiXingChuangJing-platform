<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="Class Detail"
      :title="detail?.name || '班级详情'"
      description="查看班级成员、公告与当前班级的基础信息。"
      back-to="/classes"
      back-label="返回班级列表"
    >
      <template #badge>
        <StatusPill value="class" />
      </template>
      <template #meta>
        <span>{{ detail?.school?.name || '--' }}</span>
        <span>{{ detail?.grade || '--' }}</span>
        <span>{{ detail?.academicYear || '--' }}</span>
      </template>
    </PageHero>

    <div v-if="errorMessage" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-600">
      {{ errorMessage }}
    </div>
    <div class="grid gap-6 lg:grid-cols-3">
      <MetricTile label="班级人数" :value="detail?.memberCount ?? '--'" hint="包含教师与学生" />
      <MetricTile label="班主任" :value="detail?.headTeacher?.nickname || detail?.headTeacher?.name || '--'" hint="班级负责人" />
      <MetricTile label="最新公告" :value="announcements.length" hint="按发布时间倒序排列" />
    </div>

    <div class="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
      <section class="panel-card p-6">
        <SectionHeader title="班级成员" description="查看当前班级的成员名单与角色信息。" />
        <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
        <EmptyStatePanel
          v-else-if="members.length === 0"
          eyebrow="Members"
          title="暂无成员数据"
          description="当前班级还没有成员信息。"
        />
        <div v-else class="space-y-3">
          <article v-for="member in members" :key="member.userId" class="rounded-2xl border border-slate-200 p-4">
            <div class="flex items-center justify-between gap-4">
              <div>
                <h3 class="font-medium text-slate-900">{{ member.nickname || member.realName }}</h3>
                <p class="mt-1 text-sm text-slate-500">{{ member.realName }}</p>
              </div>
              <StatusPill :value="member.role" />
            </div>
            <p class="mt-3 text-xs text-slate-400">加入时间：{{ formatDateTime(member.joinedAt) }}</p>
          </article>
        </div>
      </section>

      <section class="panel-card p-6">
        <SectionHeader title="班级公告" description="支持教师发布，当前页展示历史公告。" />
        <form v-if="canPublishAnnouncement" class="mb-5 space-y-3 rounded-2xl border border-slate-200 bg-slate-50 p-4" @submit.prevent="handleCreateAnnouncement">
          <label class="block">
            <span class="form-label">公告标题</span>
            <input v-model="announcementForm.title" class="form-control" maxlength="128" />
          </label>
          <label class="block">
            <span class="form-label">公告内容</span>
            <textarea v-model="announcementForm.content" rows="3" class="form-textarea" />
          </label>
          <label class="flex items-center gap-3 text-sm text-slate-600">
            <input v-model="announcementForm.pinned" type="checkbox" class="form-checkbox" />
            发布后置顶
          </label>
          <button
            type="submit"
            :disabled="publishingAnnouncement"
            class="rounded-xl bg-brand-600 px-4 py-3 text-sm font-medium text-white transition hover:bg-brand-700 disabled:bg-slate-300"
          >
            {{ publishingAnnouncement ? '发布中...' : '发布公告' }}
          </button>
        </form>
        <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
        <EmptyStatePanel
          v-else-if="announcements.length === 0"
          eyebrow="Announcements"
          title="暂无班级公告"
          description="当前班级还没有发布公告。"
        />
        <div v-else class="space-y-3">
          <article v-for="announcement in announcements" :key="announcement.id" class="rounded-2xl border border-slate-200 p-4">
            <div class="flex items-center justify-between gap-4">
              <h3 class="font-medium text-slate-900">{{ announcement.title }}</h3>
              <span v-if="announcement.pinned" class="rounded-full bg-amber-50 px-3 py-1 text-xs text-amber-700">置顶</span>
            </div>
            <p class="mt-3 text-sm leading-6 text-slate-500">{{ announcement.content }}</p>
            <p class="mt-3 text-xs text-slate-400">
              {{ announcement.publisher.nickname || announcement.publisher.name }} · {{ formatDateTime(announcement.publishedAt) }}
            </p>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getErrorMessage } from '@/utils/request'
import { createClassAnnouncement, getClassAnnouncements, getClassDetail, getClassMembers } from '@/api/modules/classes'
import type { Announcement, ClassDetail, ClassMember } from '@/api/types'
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
const classId = Number(route.params.classId)

const loading = ref(false)
const publishingAnnouncement = ref(false)
const errorMessage = ref('')
const detail = ref<ClassDetail | null>(null)
const members = ref<ClassMember[]>([])
const announcements = ref<Announcement[]>([])
const announcementForm = reactive({
  title: '',
  content: '',
  pinned: false,
})

const canPublishAnnouncement = computed(() => ['teacher', 'admin'].includes(authStore.user?.role || ''))

async function fetchClassDetail() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [detailData, memberData, announcementData] = await Promise.all([
      getClassDetail(classId),
      getClassMembers(classId),
      getClassAnnouncements(classId),
    ])
    detail.value = detailData
    members.value = memberData
    announcements.value = announcementData
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '班级详情加载失败')
  } finally {
    loading.value = false
  }
}

async function handleCreateAnnouncement() {
  publishingAnnouncement.value = true
  errorMessage.value = ''

  try {
    await createClassAnnouncement(classId, announcementForm)
    appStore.showToast('班级公告已发布', 'success')
    announcementForm.title = ''
    announcementForm.content = ''
    announcementForm.pinned = false
    await fetchClassDetail()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '公告发布失败')
  } finally {
    publishingAnnouncement.value = false
  }
}

onMounted(fetchClassDetail)
</script>
