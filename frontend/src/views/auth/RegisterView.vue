<template>
  <div class="flex min-h-screen items-center justify-center bg-neutral-50 px-4 py-10">
    <div class="w-full max-w-xl">
      <section class="panel-card p-8">
        <p class="text-sm uppercase tracking-[0.36em] text-neutral-500">{{ roleMeta.eyebrow }}</p>
        <h2 class="mt-3 text-3xl font-semibold text-slate-900">{{ roleMeta.title }}</h2>
        <p class="mt-3 text-sm leading-6 text-slate-500">{{ roleMeta.description }}</p>

        <form class="mt-8 space-y-5" @submit.prevent="handleSubmit">
          <p v-if="errorMessage" class="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-600">
            {{ errorMessage }}
          </p>

          <label class="block">
            <span class="form-label">账号</span>
            <input v-model.trim="form.account" autocomplete="username" class="form-control" />
          </label>

          <label class="block">
            <span class="form-label">密码</span>
            <input v-model="form.password" type="password" autocomplete="new-password" class="form-control" />
          </label>

          <label class="block">
            <span class="form-label">邮箱</span>
            <input v-model.trim="form.email" type="email" autocomplete="email" class="form-control" />
          </label>

          <template v-if="selectedRole === 'teacher'">
            <label class="block">
              <span class="form-label">学校</span>
              <select v-model.number="form.schoolId" class="form-control">
                <option :value="0">请选择学校</option>
                <option v-for="school in schools" :key="school.id" :value="school.id">
                  {{ school.name }}
                </option>
              </select>
            </label>

            <label class="block">
              <span class="form-label">教工号</span>
              <input v-model.trim="form.teacherNo" class="form-control" placeholder="请输入教工号" />
            </label>
          </template>

          <div class="grid gap-4 md:grid-cols-[1fr_auto]">
            <label class="block">
              <span class="form-label">验证码</span>
              <input v-model.trim="form.captchaCode" class="form-control" />
            </label>

            <div class="flex flex-col justify-end">
              <button
                type="button"
                class="mb-2 rounded-xl border border-neutral-200 px-3 py-2 text-sm text-neutral-700 transition hover:border-neutral-300 hover:text-neutral-900"
                :disabled="captchaLoading"
                @click="refreshCaptcha"
              >
                {{ captchaLoading ? '刷新中...' : '刷新验证码' }}
              </button>
              <div class="flex h-11 min-w-36 items-center justify-center overflow-hidden rounded-xl border border-neutral-200 bg-neutral-50">
                <img v-if="captcha.imageData" :src="captcha.imageData" alt="图形验证码" class="h-full w-full object-cover" />
              </div>
            </div>
          </div>

          <button
            type="submit"
            :disabled="submitting || captchaLoading || schoolLoading"
            class="w-full rounded-xl px-4 py-3 text-sm font-medium text-white transition disabled:bg-neutral-300"
            :class="selectedRole === 'teacher' ? 'bg-brand-600 hover:bg-brand-700' : 'bg-emerald-600 hover:bg-emerald-700'"
          >
            {{ submitting ? '注册中...' : roleMeta.submitText }}
          </button>
        </form>

        <div class="mt-6 flex items-center justify-between border-t border-neutral-200 pt-4 text-sm text-neutral-500">
          <span>已经有账号</span>
          <RouterLink to="/login" class="text-neutral-900 transition hover:text-blue-600">去登录</RouterLink>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCaptcha, getSchools, register } from '@/api/modules/auth'
import type { RegisterRequest, SchoolInfo } from '@/api/types'
import { getErrorMessage } from '@/utils/request'
import { useAppStore } from '@/stores/app'

type RegisterRole = RegisterRequest['role']

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const submitting = ref(false)
const captchaLoading = ref(false)
const schoolLoading = ref(false)
const errorMessage = ref('')
const selectedRole = ref<RegisterRole>('student')
const schools = ref<SchoolInfo[]>([])
const captcha = reactive({
  captchaId: '',
  imageData: '',
})
const form = reactive({
  account: '',
  password: '',
  email: '',
  schoolId: 0,
  teacherNo: '',
  captchaCode: '',
})

const roleMeta = computed(() => {
  if (selectedRole.value === 'teacher') {
    return {
      eyebrow: 'Teacher Register',
      title: '教师账号注册',
      description: '教师账号提交后会进入待审核状态，管理员审核通过后才能登录。',
      submitText: '提交教师注册',
    }
  }

  return {
    eyebrow: 'Student Register',
    title: '学生账号注册',
    description: '学生账号注册成功后可直接登录平台。',
    submitText: '注册学生账号',
  }
})

function normalizeRole(value: unknown): RegisterRole {
  return value === 'teacher' ? 'teacher' : 'student'
}

function syncRoleFromRoute() {
  selectedRole.value = normalizeRole(route.query.role)
  if (selectedRole.value === 'student') {
    form.schoolId = 0
    form.teacherNo = ''
  }
}

async function refreshCaptcha() {
  captchaLoading.value = true

  try {
    const response = await getCaptcha()
    captcha.captchaId = response.captchaId
    captcha.imageData = response.imageData
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '验证码加载失败')
  } finally {
    captchaLoading.value = false
  }
}

async function loadSchools() {
  schoolLoading.value = true

  try {
    schools.value = await getSchools()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '学校列表加载失败')
  } finally {
    schoolLoading.value = false
  }
}

function validateForm() {
  if (selectedRole.value !== 'teacher') {
    return true
  }

  if (!form.schoolId) {
    errorMessage.value = '请选择学校'
    return false
  }

  if (!form.teacherNo.trim()) {
    errorMessage.value = '请输入教工号'
    return false
  }

  return true
}

async function handleSubmit() {
  errorMessage.value = ''
  if (!validateForm()) {
    return
  }

  submitting.value = true

  try {
    await register({
      account: form.account,
      password: form.password,
      email: form.email,
      role: selectedRole.value,
      schoolId: selectedRole.value === 'teacher' ? form.schoolId : undefined,
      teacherNo: selectedRole.value === 'teacher' ? form.teacherNo.trim() : undefined,
      captchaId: captcha.captchaId,
      captchaCode: form.captchaCode,
    })

    const successMessage = selectedRole.value === 'teacher'
      ? '教师注册已提交，等待管理员审核'
      : '学生注册成功，请登录'
    appStore.showToast(successMessage, 'success')
    await router.replace('/login')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '注册失败')
    form.captchaCode = ''
    await refreshCaptcha()
  } finally {
    submitting.value = false
  }
}

watch(
  () => route.query.role,
  () => {
    syncRoleFromRoute()
    if (selectedRole.value === 'teacher' && schools.value.length === 0) {
      void loadSchools()
    }
  },
)

onMounted(() => {
  syncRoleFromRoute()
  void refreshCaptcha()
  if (selectedRole.value === 'teacher') {
    void loadSchools()
  }
})
</script>
