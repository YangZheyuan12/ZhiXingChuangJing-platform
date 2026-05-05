<template>
  <div class="flex min-h-screen items-center justify-center px-4 py-10">
    <div class="grid w-full max-w-5xl gap-6 lg:grid-cols-[1.2fr_0.8fr]">
      <section class="hidden rounded-[2rem] bg-gradient-to-br from-brand-900 via-brand-700 to-brand-500 p-10 text-white shadow-panel lg:block">
        <p class="text-sm uppercase tracking-[0.36em] text-white/60">Password Recovery</p>
        <h1 class="mt-6 text-4xl font-bold leading-tight">通过注册邮箱验证，快速找回账号。</h1>
        <p class="mt-6 max-w-xl text-base leading-7 text-white/78">
          系统会将重置链接发送至您注册时绑定的邮箱，请在 15 分钟内完成操作。
        </p>
      </section>

      <section class="panel-card p-8">
        <p class="text-sm uppercase tracking-[0.36em] text-brand-600">知行创境</p>
        <h2 class="mt-3 text-3xl font-semibold text-slate-900">找回密码</h2>
        <form class="mt-8 space-y-5" @submit.prevent="handleSubmit">
          <p v-if="errorMessage" class="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-600">
            {{ errorMessage }}
          </p>
          <p v-if="successMessage" class="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-600">
            {{ successMessage }}
          </p>
          <RouterLink
            v-if="resetPath"
            :to="resetPath"
            class="block rounded-xl border border-brand-200 bg-brand-50 px-4 py-3 text-sm font-medium text-brand-700 transition hover:border-brand-300 hover:bg-brand-100"
          >
            本地开发模式：直接去重置密码
          </RouterLink>
          <label class="block">
            <span class="form-label">账号</span>
            <input v-model="form.account" class="form-control" placeholder="请输入账号" />
          </label>
          <label class="block">
            <span class="form-label">注册邮箱</span>
            <input v-model="form.email" type="email" class="form-control" placeholder="请输入注册时绑定的邮箱" />
          </label>
          <button
            type="submit"
            :disabled="submitting || successMessage !== ''"
            class="w-full rounded-xl bg-brand-600 px-4 py-3 text-sm font-medium text-white transition hover:bg-brand-700 disabled:bg-neutral-300"
          >
            {{ submitting ? '发送中...' : '发送重置邮件' }}
          </button>
        </form>
        <div class="mt-6 border-t border-neutral-200 pt-4 text-sm text-neutral-500">
          <p>想起密码了？</p>
          <RouterLink to="/login" class="mt-2 inline-block text-brand-600 transition hover:text-brand-700">
            回到登录
          </RouterLink>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { requestPasswordReset } from '@/api/modules/auth'
import { getErrorMessage } from '@/utils/request'

const submitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const resetPath = ref('')

const form = reactive({
  account: '',
  email: '',
})

async function handleSubmit() {
  errorMessage.value = ''
  successMessage.value = ''
  resetPath.value = ''

  if (!form.account.trim()) {
    errorMessage.value = '请输入账号'
    return
  }
  if (!form.email.trim()) {
    errorMessage.value = '请输入注册邮箱'
    return
  }

  submitting.value = true

  try {
    const response = await requestPasswordReset({
      account: form.account,
      email: form.email,
    })
    if (response.resetUrl) {
      resetPath.value = toLocalResetPath(response.resetUrl)
      successMessage.value = `已生成重置链接，请在 ${response.expiresInMinutes} 分钟内完成密码重置。`
      return
    }
    successMessage.value = `重置邮件已发送至您的邮箱，请在 ${response.expiresInMinutes} 分钟内点击邮件中的链接完成密码重置。`
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '发送失败，请检查账号与邮箱是否匹配')
  } finally {
    submitting.value = false
  }
}

function toLocalResetPath(resetUrl: string) {
  try {
    const url = new URL(resetUrl, window.location.origin)
    return `${url.pathname}${url.search}`
  } catch {
    return resetUrl
  }
}
</script>
