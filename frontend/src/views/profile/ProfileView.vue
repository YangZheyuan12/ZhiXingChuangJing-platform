<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="Profile Center"
      title="个人中心"
      description="查看当前账号的基础资料、主页入口与作品归档。资料编辑与密码修改已独立到设置页处理。"
    >
      <template #actions>
        <RouterLink
          to="/profile/edit"
          class="page-primary-button px-4 py-3"
        >
          编辑资料与安全设置
        </RouterLink>
      </template>
    </PageHero>

    <p v-if="profileError" class="rounded-xl bg-rose-50 px-4 py-3 text-sm text-rose-600">{{ profileError }}</p>

    <section class="panel-card p-6">
      <SectionHeader title="个人资料" description="当前账号公开展示的基础信息。" />
      <div v-if="loadingProfile" class="text-sm text-slate-500">加载中...</div>
      <div v-else class="space-y-6">
        <div class="flex flex-col gap-5 rounded-2xl bg-brand-50 p-5 md:flex-row md:items-center md:justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-20 w-20 items-center justify-center overflow-hidden rounded-full border border-brand-100 bg-white text-2xl font-semibold text-brand-700">
              <img
                v-if="profile.avatarUrl"
                :src="profile.avatarUrl"
                alt="头像"
                class="h-full w-full object-cover"
              />
              <span v-else>{{ avatarFallback }}</span>
            </div>
            <div>
              <p class="text-sm text-brand-700">当前登录用户</p>
              <p class="mt-2 text-2xl font-semibold text-brand-900">{{ profile.realName || '--' }}</p>
              <p class="mt-1 text-sm text-slate-600">{{ roleLabel }}</p>
            </div>
          </div>
          <div class="flex flex-wrap gap-3">
            <RouterLink
              to="/profile/edit"
              class="rounded-xl border border-brand-200 bg-white px-4 py-3 text-sm font-medium text-brand-700 transition hover:bg-brand-50"
            >
              去设置页编辑
            </RouterLink>
            <RouterLink
              v-if="profile.id"
              :to="`/users/${profile.id}/homepage`"
              class="rounded-xl bg-brand-600 px-4 py-3 text-sm font-medium text-white transition hover:bg-brand-700"
            >
              预览我的主页
            </RouterLink>
          </div>
        </div>

        <dl class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <div class="rounded-2xl border border-slate-200 bg-white p-4">
            <dt class="text-sm text-slate-500">昵称</dt>
            <dd class="mt-2 text-base font-medium text-slate-900">{{ profile.nickname || '--' }}</dd>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-white p-4">
            <dt class="text-sm text-slate-500">学校</dt>
            <dd class="mt-2 text-base font-medium text-slate-900">{{ profile.school?.name || '--' }}</dd>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-white p-4">
            <dt class="text-sm text-slate-500">班级</dt>
            <dd class="mt-2 text-base font-medium text-slate-900">{{ profile.classInfo?.name || '--' }}</dd>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-white p-4">
            <dt class="text-sm text-slate-500">邮箱</dt>
            <dd class="mt-2 text-base font-medium text-slate-900">{{ profile.email || '--' }}</dd>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-white p-4 md:col-span-2 xl:col-span-2">
            <dt class="text-sm text-slate-500">个人简介</dt>
            <dd class="mt-2 text-base leading-7 text-slate-900">{{ profile.bio || '暂未填写个人简介。' }}</dd>
          </div>
        </dl>
      </div>
    </section>

    <section class="panel-card p-6">
      <SectionHeader title="快捷入口" description="快速进入作品页与个人设置页。" />
      <div class="flex flex-wrap gap-3">
        <RouterLink
          to="/portfolio"
          class="rounded-xl bg-brand-600 px-4 py-3 text-sm font-medium text-white transition hover:bg-brand-700"
        >
          打开我的作品集
        </RouterLink>
        <RouterLink
          to="/profile/edit"
          class="rounded-xl border border-slate-200 px-4 py-3 text-sm font-medium text-slate-700 transition hover:bg-slate-50"
        >
          编辑资料
        </RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { getErrorMessage } from '@/utils/request'
import { getMyProfile } from '@/api/modules/users'
import type { UserProfile } from '@/api/types'
import PageHero from '@/components/common/PageHero.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'

const loadingProfile = ref(false)
const profileError = ref('')
const profile = reactive<UserProfile>({
  id: 0,
  role: '',
  realName: '',
  nickname: '',
  avatarUrl: '',
  email: '',
  mobile: '',
  bio: '',
  school: null,
  classInfo: null,
})

const avatarFallback = computed(() => {
  const source = (profile.nickname || profile.realName || '用').trim()
  return source.slice(0, 1).toUpperCase()
})

const roleLabel = computed(() => {
  switch (profile.role) {
    case 'admin':
      return '管理员'
    case 'teacher':
      return '教师'
    case 'student':
      return '学生'
    case 'assistant':
      return '助教'
    default:
      return profile.role || '--'
  }
})

async function fetchProfile() {
  loadingProfile.value = true
  profileError.value = ''

  try {
    Object.assign(profile, await getMyProfile())
  } catch (error) {
    profileError.value = getErrorMessage(error, '个人资料加载失败')
  } finally {
    loadingProfile.value = false
  }
}

onMounted(fetchProfile)
</script>
