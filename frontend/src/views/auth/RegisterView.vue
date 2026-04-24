<template>
  <div class="flex min-h-screen items-center justify-center bg-neutral-50 px-4 py-10">
    <div class="w-full max-w-md rounded-2xl border border-neutral-200 bg-white p-8">
      <p class="text-sm uppercase tracking-[0.3em] text-neutral-500">知行创境</p>
      <h1 class="mt-3 text-3xl font-semibold text-neutral-900">注册账号</h1>

      <form class="mt-8 space-y-5" @submit.prevent="handleSubmit">
        <p v-if="errorMessage" class="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
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

        <div class="grid gap-4 md:grid-cols-[1fr_auto]">
          <label class="block">
            <span class="form-label">验证码</span>
            <input v-model.trim="form.captchaCode" class="form-control" />
          </label>

          <div class="flex flex-col justify-end">
            <button
              type="button"
              class="mb-2 rounded-md border border-neutral-200 px-3 py-2 text-sm text-neutral-700 transition hover:border-neutral-300 hover:text-neutral-900"
              :disabled="captchaLoading"
              @click="refreshCaptcha"
            >
              {{ captchaLoading ? '刷新中...' : '刷新验证码' }}
            </button>
            <div class="flex h-11 min-w-36 items-center justify-center overflow-hidden rounded-md border border-neutral-200 bg-neutral-50">
              <img v-if="captcha.imageData" :src="captcha.imageData" alt="图形验证码" class="h-full w-full object-cover" />
            </div>
          </div>
        </div>

        <button
          type="submit"
          :disabled="submitting || captchaLoading"
          class="w-full rounded-md bg-blue-600 px-4 py-3 text-sm font-medium text-white transition hover:bg-blue-700 disabled:bg-neutral-300"
        >
          {{ submitting ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="mt-6 flex items-center justify-between border-t border-neutral-200 pt-4 text-sm text-neutral-500">
        <span>已经有账号</span>
        <RouterLink to="/login" class="text-neutral-900 transition hover:text-blue-600">去登录</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCaptcha, register } from '@/api/modules/auth'
import { getErrorMessage } from '@/utils/request'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const appStore = useAppStore()
const submitting = ref(false)
const captchaLoading = ref(false)
const errorMessage = ref('')
const captcha = reactive({
  captchaId: '',
  imageData: '',
})
const form = reactive({
  account: '',
  password: '',
  captchaCode: '',
})

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

async function handleSubmit() {
  submitting.value = true
  errorMessage.value = ''

  try {
    await register({
      account: form.account,
      password: form.password,
      captchaId: captcha.captchaId,
      captchaCode: form.captchaCode,
    })
    appStore.showToast('注册成功，请登录', 'success')
    await router.replace('/login')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '注册失败')
    form.captchaCode = ''
    await refreshCaptcha()
  } finally {
    submitting.value = false
  }
}

onMounted(refreshCaptcha)
</script>
