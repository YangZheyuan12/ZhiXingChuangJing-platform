<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="Profile Settings"
      title="编辑资料与安全设置"
      description="当前页面集中处理昵称、头像、简介和密码修改。所有资料模块按单列展开，便于完整浏览与编辑。"
      back-to="/profile"
      back-label="返回个人中心"
    />

    <section class="panel-card p-6">
      <SectionHeader title="个人资料" description="更新昵称、头像与个人简介。" />
      <p v-if="profileError" class="mb-4 rounded-xl bg-rose-50 px-4 py-3 text-sm text-rose-600">{{ profileError }}</p>
      <form class="space-y-5" @submit.prevent="handleProfileSubmit">
        <div class="rounded-2xl border border-slate-200 bg-slate-50 p-5">
          <div class="flex flex-col gap-5 lg:flex-row lg:items-center">
            <div class="flex items-center gap-4">
              <div class="flex h-20 w-20 items-center justify-center overflow-hidden rounded-full border border-slate-200 bg-white text-2xl font-semibold text-brand-700">
                <img
                  v-if="profileForm.avatarUrl"
                  :src="profileForm.avatarUrl"
                  alt="头像预览"
                  class="h-full w-full object-cover"
                />
                <span v-else>{{ avatarFallback }}</span>
              </div>
              <div>
                <p class="text-sm font-medium text-slate-900">{{ profile.realName || '当前用户' }}</p>
                <p class="mt-1 text-sm text-slate-500">头像与公开资料会同步显示在个人主页与用户菜单中。</p>
              </div>
            </div>
            <div class="lg:ml-auto">
              <label class="block">
                <span class="form-label">上传头像</span>
                <input
                  type="file"
                  accept="image/*"
                  class="form-file"
                  :disabled="avatarUploading"
                  @change="handleAvatarFileChange"
                />
              </label>
              <p v-if="avatarUploading" class="mt-2 text-xs text-slate-500">头像上传中...</p>
            </div>
          </div>
        </div>

        <label class="block">
          <span class="form-label">昵称</span>
          <input v-model="profileForm.nickname" class="form-control" maxlength="64" />
        </label>

        <label class="block">
          <span class="form-label">个人简介</span>
          <textarea v-model="profileForm.bio" rows="5" class="form-textarea" maxlength="255" />
        </label>

        <div class="flex flex-wrap gap-3">
          <button
            type="submit"
            :disabled="savingProfile"
            class="rounded-xl bg-brand-600 px-4 py-3 text-sm font-medium text-white transition hover:bg-brand-700"
          >
            {{ savingProfile ? '保存中...' : '保存资料' }}
          </button>
          <RouterLink
            to="/profile"
            class="rounded-xl border border-slate-200 px-4 py-3 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
          >
            返回个人中心
          </RouterLink>
        </div>
      </form>
    </section>

    <section class="panel-card p-6">
      <SectionHeader title="修改密码" description="更新当前账号的登录密码。" />
      <p v-if="passwordError" class="mb-4 rounded-xl bg-rose-50 px-4 py-3 text-sm text-rose-600">{{ passwordError }}</p>
      <form class="space-y-4" @submit.prevent="handlePasswordSubmit">
        <label class="block">
          <span class="form-label">原密码</span>
          <input v-model="passwordForm.oldPassword" type="password" class="form-control" />
        </label>
        <label class="block">
          <span class="form-label">新密码</span>
          <input v-model="passwordForm.newPassword" type="password" class="form-control" />
        </label>
        <button
          type="submit"
          :disabled="savingPassword"
          class="rounded-xl border border-brand-200 px-4 py-3 text-sm font-medium text-brand-700 transition hover:bg-brand-50"
        >
          {{ savingPassword ? '提交中...' : '更新密码' }}
        </button>
      </form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { uploadAsset } from '@/api/modules/assets'
import { getErrorMessage } from '@/utils/request'
import { getMyProfile, updateMyPassword, updateMyProfile } from '@/api/modules/users'
import PageHero from '@/components/common/PageHero.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import { useAppStore } from '@/stores/app'
import type { UserProfile } from '@/api/types'

const appStore = useAppStore()
const savingProfile = ref(false)
const savingPassword = ref(false)
const avatarUploading = ref(false)
const profileError = ref('')
const passwordError = ref('')

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

const profileForm = reactive({
  nickname: '',
  avatarUrl: '',
  bio: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
})

const avatarFallback = computed(() => {
  const source = (profileForm.nickname || profile.realName || '用').trim()
  return source.slice(0, 1).toUpperCase()
})

async function fetchProfile() {
  profileError.value = ''

  try {
    Object.assign(profile, await getMyProfile())
    profileForm.nickname = profile.nickname || ''
    profileForm.avatarUrl = profile.avatarUrl || ''
    profileForm.bio = profile.bio || ''
  } catch (error) {
    profileError.value = getErrorMessage(error, '个人资料加载失败')
  }
}

async function handleProfileSubmit() {
  savingProfile.value = true
  profileError.value = ''

  try {
    const nextProfile = await updateMyProfile(profileForm)
    Object.assign(profile, nextProfile)
    profileForm.nickname = nextProfile.nickname || ''
    profileForm.avatarUrl = nextProfile.avatarUrl || ''
    profileForm.bio = nextProfile.bio || ''
    appStore.showToast('个人资料已更新', 'success')
  } catch (error) {
    profileError.value = getErrorMessage(error, '保存资料失败')
  } finally {
    savingProfile.value = false
  }
}

async function handlePasswordSubmit() {
  savingPassword.value = true
  passwordError.value = ''

  try {
    await updateMyPassword(passwordForm)
    appStore.showToast('密码更新成功', 'success')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
  } catch (error) {
    passwordError.value = getErrorMessage(error, '密码更新失败')
  } finally {
    savingPassword.value = false
  }
}

async function handleAvatarFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) {
    return
  }

  avatarUploading.value = true
  profileError.value = ''

  try {
    const result = await uploadAsset(file, {
      folder: 'users/avatars',
      bizType: 'avatar',
    })
    profileForm.avatarUrl = result.fileUrl
    appStore.showToast('头像已上传', 'success')
  } catch (error) {
    profileError.value = getErrorMessage(error, '头像上传失败')
  } finally {
    avatarUploading.value = false
    target.value = ''
  }
}

onMounted(fetchProfile)
</script>
