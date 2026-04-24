<template>
  <div class="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
    <section
      class="relative overflow-hidden rounded-lg border border-[#E5E7EB] bg-white shadow-sm"
    >
      <div class="absolute inset-0 bg-[linear-gradient(135deg,rgba(211,47,47,0.08),transparent_32%,rgba(31,41,55,0.04))]" />
      <div class="absolute inset-x-0 top-0 h-1 bg-[#D32F2F]" />
      <div class="absolute inset-0 opacity-40 [background-image:linear-gradient(rgba(211,47,47,0.06)_1px,transparent_1px),linear-gradient(90deg,rgba(211,47,47,0.06)_1px,transparent_1px)] [background-position:center] [background-size:28px_28px]" />

      <div class="relative flex flex-col gap-6 p-6 lg:flex-row lg:items-start lg:justify-between">
        <div class="flex flex-1 flex-col gap-5 sm:flex-row">
          <div class="relative h-[120px] w-[120px] shrink-0">
            <button
              type="button"
              class="group relative h-full w-full overflow-hidden rounded-full border-4 border-white bg-[#E5E7EB] shadow-sm ring-1 ring-black/5"
              :class="canEdit ? 'cursor-pointer' : 'cursor-default'"
              :disabled="!canEdit || avatarUploading"
              @click="handleAvatarUpload"
            >
              <img
                :src="homepage?.user.avatarUrl || fallbackAvatarUrl"
                :alt="`${displayName}头像`"
                class="h-full w-full object-cover"
              />
              <div
                class="absolute inset-0 flex flex-col items-center justify-center gap-1 bg-black/0 text-white opacity-0 transition duration-200"
                :class="canEdit ? 'group-hover:bg-black/55 group-hover:opacity-100' : ''"
              >
                <svg viewBox="0 0 24 24" class="h-6 w-6" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M4 8h3l1.6-2h7L17 8h3a1 1 0 0 1 1 1v8a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V9a1 1 0 0 1 1-1Z" />
                  <circle cx="12" cy="13" r="3.5" />
                </svg>
                <span class="text-xs font-medium">更换头像</span>
              </div>
            </button>
            <input
              ref="avatarInputRef"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleAvatarFileChange"
            />
          </div>

          <div class="flex min-w-0 flex-1 flex-col justify-center gap-4">
            <div class="flex flex-wrap items-center gap-3">
              <div v-if="isEditing" class="w-full max-w-sm">
                <label class="mb-1 block text-xs font-medium tracking-[0.2em] text-[#6B7280]">昵称</label>
                <input
                  v-model.trim="profileForm.nickname"
                  type="text"
                  maxlength="24"
                  class="w-full rounded-lg border border-[#D1D5DB] bg-white px-3 py-2 text-xl font-semibold text-[#1F2937] outline-none transition focus:border-[#D32F2F] focus:ring-2 focus:ring-[#FCA5A5]/40"
                  placeholder="请输入昵称"
                />
              </div>
              <template v-else>
                <h1 class="truncate text-3xl font-semibold tracking-[0.02em] text-[#1F2937]">
                  {{ displayName }}
                </h1>
              </template>
              <span
                class="inline-flex items-center rounded-full bg-[#F3F4F6] px-3 py-1 text-xs font-medium text-[#4B5563]"
              >
                {{ roleBadge }}
              </span>
            </div>

            <div class="flex flex-wrap gap-3 text-sm text-[#6B7280]">
              <span class="inline-flex items-center gap-2 rounded-lg bg-white/80 px-3 py-2 ring-1 ring-inset ring-[#E5E7EB]">
                <svg viewBox="0 0 24 24" class="h-4 w-4 text-[#D32F2F]" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M3 19h18M7 19V8m10 11V5M12 19v-7" />
                </svg>
                {{ homepage?.user.school?.name || '知行创境示范校' }}
              </span>
              <span class="inline-flex items-center gap-2 rounded-lg bg-white/80 px-3 py-2 ring-1 ring-inset ring-[#E5E7EB]">
                <svg viewBox="0 0 24 24" class="h-4 w-4 text-[#D32F2F]" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M4 7.5 12 4l8 3.5L12 11 4 7.5Zm2 3.2V15l6 3 6-3v-4.3" />
                </svg>
                {{ classLabel }}
              </span>
            </div>

            <div>
              <label
                v-if="isEditing"
                class="mb-1 block text-xs font-medium tracking-[0.2em] text-[#6B7280]"
              >
                个人简介
              </label>
              <textarea
                v-if="isEditing"
                v-model.trim="profileForm.bio"
                rows="2"
                maxlength="120"
                class="w-full resize-none rounded-lg border border-[#D1D5DB] bg-white px-3 py-2 text-sm leading-6 text-[#1F2937] outline-none transition focus:border-[#D32F2F] focus:ring-2 focus:ring-[#FCA5A5]/40"
                placeholder="介绍你的研究主题、创作方向或近期项目"
              />
              <p
                v-else
                class="max-w-3xl truncate text-sm leading-7 text-[#4B5563]"
                :title="homepage?.user.bio || defaultBio"
              >
                {{ homepage?.user.bio || defaultBio }}
              </p>
            </div>
          </div>
        </div>

        <div class="flex shrink-0 flex-wrap items-center justify-end gap-3">
          <template v-if="canEdit">
            <template v-if="isEditing">
              <button
                type="button"
                class="rounded-lg border border-[#D1D5DB] bg-white px-4 py-2 text-sm font-medium text-[#4B5563] transition hover:border-[#9CA3AF] hover:bg-[#F9FAFB]"
                :disabled="savingProfile"
                @click="handleCancelEdit"
              >
                取消
              </button>
              <button
                type="button"
                class="rounded-lg bg-[#D32F2F] px-4 py-2 text-sm font-medium text-white transition hover:bg-[#F87171] disabled:cursor-not-allowed disabled:bg-[#FCA5A5]"
                :disabled="savingProfile"
                @click="handleSaveProfile"
              >
                {{ savingProfile ? '保存中...' : '保存' }}
              </button>
            </template>
            <button
              v-else
              type="button"
              class="rounded-lg border border-[#FCA5A5] bg-white px-4 py-2 text-sm font-medium text-[#D32F2F] transition hover:bg-[#FEF2F2]"
              @click="handleStartEdit"
            >
              编辑资料
            </button>
          </template>
          <div class="inline-flex items-center gap-3 rounded-lg border border-[#E5E7EB] bg-white px-4 py-2 text-sm shadow-sm">
            <span class="flex h-7 w-7 items-center justify-center rounded-full bg-[#FEF2F2] text-[#D32F2F]">
              <svg viewBox="0 0 24 24" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="1.8">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 4 4 7.5 12 11l8-3.5L12 4Zm-6 6.8V15l6 3 6-3v-4.2" />
              </svg>
            </span>
            <span class="text-[#9CA3AF]">身份</span>
            <span class="font-semibold text-[#1F2937]">{{ normalizeRole(homepage?.user.role) }}</span>
          </div>
        </div>
      </div>
    </section>

    <p
      v-if="noticeMessage"
      class="rounded-lg border px-4 py-3 text-sm shadow-sm"
      :class="usingMockData ? 'border-amber-200 bg-amber-50 text-amber-700' : 'border-rose-200 bg-rose-50 text-rose-600'"
    >
      {{ noticeMessage }}
    </p>

    <section class="grid gap-6 md:grid-cols-3">
      <article
        v-for="stat in statCards"
        :key="stat.label"
        class="rounded-lg border border-[#E5E7EB] bg-white p-6 shadow-sm"
      >
        <p class="text-sm font-medium tracking-[0.08em] text-[#6B7280]">{{ stat.label }}</p>
        <p class="mt-4 text-3xl font-semibold text-[#D32F2F]">{{ stat.value }}</p>
        <p class="mt-2 text-sm text-[#9CA3AF]">{{ stat.hint }}</p>
      </article>
    </section>

    <section class="rounded-lg border border-[#E5E7EB] bg-white p-6 shadow-sm">
      <div class="flex flex-col gap-2 border-b border-[#F3F4F6] pb-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p class="text-xs uppercase tracking-[0.24em] text-[#9CA3AF]">Portfolio</p>
          <h2 class="mt-2 text-2xl font-semibold text-[#1F2937]">我的作品集</h2>
          <p class="mt-2 text-sm text-[#6B7280]">聚焦“红色记忆”数字展厅与数字人创作成果，展示公开发布作品。</p>
        </div>
        <p class="text-sm text-[#9CA3AF]">
          共 {{ portfolioList.length }} 项
        </p>
      </div>

      <div v-if="loading" class="py-12 text-center text-sm text-[#6B7280]">页面数据加载中...</div>

      <div
        v-else-if="portfolioList.length"
        class="mt-6 grid gap-5 sm:grid-cols-2 xl:grid-cols-4"
      >
        <article
          v-for="item in portfolioList"
          :key="item.exhibitionId"
          class="group cursor-pointer overflow-hidden rounded-lg border border-[#E5E7EB] bg-white shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-md"
          @click="handleExhibitionClick(item.exhibitionId)"
        >
          <div class="relative aspect-[16/9] overflow-hidden bg-[#E5E7EB]">
            <img
              :src="item.coverUrl || defaultCoverUrl"
              :alt="item.title"
              class="h-full w-full object-cover transition duration-300 group-hover:scale-[1.03]"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-black/45 via-black/10 to-transparent" />
            <div class="absolute left-3 top-3 rounded-full bg-white/90 px-2.5 py-1 text-[11px] font-medium text-[#D32F2F] shadow-sm">
              公开展厅
            </div>
            <div class="absolute bottom-3 left-3 right-3 flex items-center justify-between text-xs text-white/90">
              <span>{{ formatPublishedAt(item.publishedAt) }}</span>
              <span class="rounded-full border border-white/25 bg-black/20 px-2 py-1">观众模式</span>
            </div>
          </div>
          <div class="space-y-3 p-4">
            <h3 class="truncate text-base font-semibold text-[#1F2937]">{{ item.title }}</h3>
            <div class="flex items-center gap-4 text-sm text-[#6B7280]">
              <span class="inline-flex items-center gap-1.5">
                <svg viewBox="0 0 24 24" class="h-4 w-4 text-[#9CA3AF]" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6Z" />
                  <circle cx="12" cy="12" r="2.5" />
                </svg>
                {{ item.viewCount ?? 0 }}
              </span>
              <span class="inline-flex items-center gap-1.5">
                <svg viewBox="0 0 24 24" class="h-4 w-4 text-[#D32F2F]" fill="currentColor">
                  <path d="M12 21.35 10.55 20.03C5.4 15.36 2 12.28 2 8.5 2 5.41 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09A6.03 6.03 0 0 1 16.5 3C19.58 3 22 5.41 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35Z" />
                </svg>
                {{ item.likeCount }}
              </span>
            </div>
          </div>
        </article>
      </div>

      <div
        v-else
        class="mt-6 rounded-lg border border-dashed border-[#D1D5DB] bg-[#F9FAFB] px-6 py-14 text-center"
      >
        <p class="text-lg font-semibold text-[#1F2937]">暂未发布作品</p>
        <p class="mt-2 text-sm text-[#6B7280]">发布展厅后，这里会展示封面、浏览量和点赞数据。</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserHomepage, updateMyProfile } from '@/api/modules/users'
import { uploadAsset } from '@/api/modules/assets'
import type { PortfolioItem, UpdateProfileRequest, UserHomepage, UserProfile } from '@/api/types'
import { getErrorMessage } from '@/utils/request'
import { formatDateTime } from '@/utils/format'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'

interface HomepagePortfolioItem extends PortfolioItem {
  viewCount?: number
}

interface HomepageViewModel extends Omit<UserHomepage, 'portfolio'> {
  portfolio: HomepagePortfolioItem[]
}

const defaultBio = '聚焦红色记忆数字展陈与数字叙事表达，持续沉淀兼具学术性与创造力的作品档案。'
const fallbackAvatarUrl =
  'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80'
const defaultCoverUrl =
  'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=1200&q=80'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

const loading = ref(false)
const savingProfile = ref(false)
const avatarUploading = ref(false)
const usingMockData = ref(false)
const noticeMessage = ref('')
const homepage = ref<HomepageViewModel | null>(null)
const avatarInputRef = ref<HTMLInputElement | null>(null)
const isEditing = ref(false)

const profileForm = reactive<Required<UpdateProfileRequest>>({
  nickname: '',
  avatarUrl: '',
  bio: '',
})

const resolvedUserId = computed(() => {
  const paramValue = Number(route.params.userId)
  if (Number.isFinite(paramValue) && paramValue > 0) {
    return paramValue
  }
  return authStore.user?.id || 1
})

const canEdit = computed(() => authStore.user?.id === resolvedUserId.value)

const displayName = computed(() => homepage.value?.user.nickname || homepage.value?.user.realName || '知行创境创作者')

const classLabel = computed(() => {
  const className = homepage.value?.user.classInfo?.name
  const gradeName = homepage.value?.user.classInfo?.grade
  return [gradeName, className].filter(Boolean).join(' / ') || '中小学创作班级'
})

const roleBadge = computed(() => {
  const roleText = normalizeRole(homepage.value?.user.role)
  return `${roleText} / ${classLabel.value}`
})

const statCards = computed(() => [
  {
    label: '创作展厅数',
    value: homepage.value?.stats.exhibitionCount ?? '--',
    hint: '已发布的展厅作品总数',
  },
  {
    label: '获得点赞数',
    value: homepage.value?.stats.likeCount ?? '--',
    hint: '公开展示作品累计点赞',
  },
  {
    label: '教师好评数',
    value: homepage.value?.stats.teacherPraiseCount ?? '--',
    hint: '教师评语与公开好评累计',
  },
])

const portfolioList = computed(() => homepage.value?.portfolio || [])

function createMockHomepage(userId: number): HomepageViewModel {
  const baseUser: UserProfile = {
    id: userId,
    role: 'student',
    realName: '林知行',
    nickname: '知行创作者',
    avatarUrl: fallbackAvatarUrl,
    email: null,
    mobile: null,
    bio: '关注红色文化叙事与数字展陈设计，尝试用数字人讲述更具沉浸感的校园历史故事。',
    school: {
      id: 1,
      name: '知行创境实验学校',
    },
    classInfo: {
      id: 11,
      name: '七年级1班',
      grade: '七年级',
      academicYear: '2025-2026',
    },
  }

  return {
    user: baseUser,
    stats: {
      exhibitionCount: 12,
      likeCount: 386,
      teacherPraiseCount: 28,
    },
    portfolio: [
      {
        exhibitionId: 1001,
        title: '烽火记忆中的少年邮递员',
        coverUrl:
          'https://images.unsplash.com/photo-1517048676732-d65bc937f952?auto=format&fit=crop&w=1200&q=80',
        likeCount: 96,
        viewCount: 1280,
        publishedAt: '2026-03-28T09:00:00',
      },
      {
        exhibitionId: 1002,
        title: '红船启航数字展厅',
        coverUrl:
          'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80',
        likeCount: 88,
        viewCount: 1136,
        publishedAt: '2026-03-14T10:30:00',
      },
      {
        exhibitionId: 1003,
        title: '数字人讲述长征路线图',
        coverUrl:
          'https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=1200&q=80',
        likeCount: 104,
        viewCount: 1592,
        publishedAt: '2026-02-26T14:15:00',
      },
      {
        exhibitionId: 1004,
        title: '家乡英烈纪念馆交互导览',
        coverUrl:
          'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=1200&q=80',
        likeCount: 98,
        viewCount: 1450,
        publishedAt: '2026-02-08T16:20:00',
      },
    ],
  }
}

function normalizeHomepage(data: UserHomepage): HomepageViewModel {
  return {
    ...data,
    user: {
      ...data.user,
      avatarUrl: data.user.avatarUrl || fallbackAvatarUrl,
      bio: data.user.bio || defaultBio,
    },
    portfolio: data.portfolio.map((item, index) => ({
      ...item,
      coverUrl: item.coverUrl || mockPortfolioCovers[index % mockPortfolioCovers.length],
      viewCount: 680 + index * 137,
    })),
  }
}

const mockPortfolioCovers = [
  'https://images.unsplash.com/photo-1460518451285-97b6aa326961?auto=format&fit=crop&w=1200&q=80',
  'https://images.unsplash.com/photo-1513258496099-48168024aec0?auto=format&fit=crop&w=1200&q=80',
  'https://images.unsplash.com/photo-1516534775068-ba3e7458af70?auto=format&fit=crop&w=1200&q=80',
  'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?auto=format&fit=crop&w=1200&q=80',
]

function syncProfileForm() {
  profileForm.nickname = homepage.value?.user.nickname || ''
  profileForm.avatarUrl = homepage.value?.user.avatarUrl || ''
  profileForm.bio = homepage.value?.user.bio || ''
}

function syncCurrentUser() {
  if (!canEdit.value || !homepage.value || !authStore.user) {
    return
  }

  authStore.setUser({
    ...authStore.user,
    nickname: homepage.value.user.nickname || authStore.user.nickname,
    avatarUrl: homepage.value.user.avatarUrl || null,
  })
}

async function fetchHomepage() {
  loading.value = true
  noticeMessage.value = ''

  try {
    const data = await getUserHomepage(resolvedUserId.value)
    homepage.value = normalizeHomepage(data)
    usingMockData.value = false
  } catch (error) {
    homepage.value = createMockHomepage(resolvedUserId.value)
    usingMockData.value = true
    noticeMessage.value = `${getErrorMessage(error, '个人主页加载失败')}，当前展示模拟数据。`
  } finally {
    syncProfileForm()
    syncCurrentUser()
    loading.value = false
  }
}

function handleStartEdit() {
  syncProfileForm()
  isEditing.value = true
}

function handleCancelEdit() {
  syncProfileForm()
  isEditing.value = false
}

async function handleSaveProfile() {
  if (!homepage.value || !canEdit.value) {
    return
  }

  savingProfile.value = true
  noticeMessage.value = ''

  try {
    await updateMyProfile({
      nickname: profileForm.nickname,
      bio: profileForm.bio,
    })
    appStore.showToast('个人资料已更新', 'success')
    await fetchHomepage()
    isEditing.value = false
  } catch (error) {
    if (usingMockData.value && homepage.value) {
      homepage.value.user.nickname = profileForm.nickname || homepage.value.user.realName
      homepage.value.user.bio = profileForm.bio || defaultBio
      syncCurrentUser()
      isEditing.value = false
      appStore.showToast('接口未联通，已使用本地模拟数据更新', 'info')
      return
    }

    noticeMessage.value = getErrorMessage(error, '资料保存失败')
  } finally {
    savingProfile.value = false
  }
}

function handleAvatarUpload() {
  if (!canEdit.value || avatarUploading.value) {
    return
  }

  avatarInputRef.value?.click()
}

async function handleAvatarFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  if (!file || !homepage.value || !canEdit.value) {
    input.value = ''
    return
  }

  avatarUploading.value = true
  noticeMessage.value = ''

  try {
    const uploadResult = await uploadAsset(file, {
      folder: 'avatar',
      bizType: 'profile',
    })
    await updateMyProfile({ avatarUrl: uploadResult.fileUrl })
    appStore.showToast('头像已更新', 'success')
    await fetchHomepage()
  } catch (error) {
    if (usingMockData.value) {
      const localPreviewUrl = URL.createObjectURL(file)
      homepage.value.user.avatarUrl = localPreviewUrl
      profileForm.avatarUrl = localPreviewUrl
      syncCurrentUser()
      appStore.showToast('接口未联通，已使用本地头像预览', 'info')
    } else {
      noticeMessage.value = getErrorMessage(error, '头像上传失败')
    }
  } finally {
    avatarUploading.value = false
    input.value = ''
  }
}

function handleExhibitionClick(id: number) {
  router.push(`/community/${id}`)
}

function normalizeRole(role?: string | null) {
  switch ((role || '').toLowerCase()) {
    case 'student':
      return '学生'
    case 'teacher':
      return '教师'
    case 'admin':
      return '管理员'
    default:
      return role || '创作者'
  }
}

function formatPublishedAt(dateTime?: string | null) {
  if (!dateTime) {
    return '近期发布'
  }

  return formatDateTime(dateTime).slice(0, 10)
}

watch(
  () => resolvedUserId.value,
  () => {
    isEditing.value = false
    fetchHomepage()
  },
)

onMounted(fetchHomepage)
</script>
