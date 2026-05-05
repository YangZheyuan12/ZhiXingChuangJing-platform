<template>
  <div class="flex min-h-screen items-center justify-center px-4 py-10">
    <div class="grid w-full max-w-5xl gap-6 lg:grid-cols-[1.2fr_0.8fr]">
      <section class="hidden rounded-[2rem] bg-gradient-to-br from-brand-900 via-brand-700 to-brand-500 p-10 text-white shadow-panel lg:block">
        <p class="text-sm uppercase tracking-[0.36em] text-white/60">Password Reset</p>
        <h1 class="mt-6 text-4xl font-bold leading-tight">设置新密码，确保账号安全。</h1>
        <p class="mt-6 max-w-xl text-base leading-7 text-white/78">
          请设置一个全新的登录密码，建议包含字母与数字的组合。
        </p>
      </section>

      <section class="panel-card p-8">
        <p class="text-sm uppercase tracking-[0.36em] text-brand-600">知行创境</p>
        <h2 class="mt-3 text-3xl font-semibold text-slate-900">设置新密码</h2>
        <form class="mt-8 space-y-5" @submit.prevent="handleSubmit">
          <p v-if="errorMessage" class="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-600">
            {{ errorMessage }}
          </p>
          <p v-if="successMessage" class="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-600">
            {{ successMessage }}
          </p>
          <template v-if="!successMessage">
            <label class="block">
              <span class="form-label">新密码</span>
              <input v-model="form.newPassword" type="password" class="form-control" placeholder="请输入新密码（至少6位）" />
            </label>
            <label class="block">
              <span class="form-label">确认密码</span>
              <input v-model="form.confirmPassword" type="password" class="form-control" placeholder="请再次输入新密码" />
            </label>
            <button
              type="submit"
              :disabled="submitting"
              class="w-full rounded-xl bg-brand-600 px-4 py-3 text-sm font-medium text-white transition hover:bg-brand-700 disabled:bg-neutral-300"
            >
              {{ submitting ? '重置中...' : '确认重置' }}
            </button>
          </template>
        </form>
        <div v-if="successMessage" class="mt-6 flex flex-col gap-3">
          <RouterLink
            to="/login"
            class="w-full rounded-xl bg-brand-600 px-4 py-3 text-center text-sm font-medium text-white transition hover:bg-brand-700"
          >
            返回登录
          </RouterLink>
        </div>
        <div v-else class="mt-6 border-t border-neutral-200 pt-4 text-sm text-neutral-500">
          <p>还没收到邮件？</p>
          <RouterLink to="/forgot-password" class="mt-2 inline-block text-brand-600 transition hover:text-brand-700">
            重新发送
          </RouterLink>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { confirmPasswordReset } from '@/api/modules/auth'
import { getErrorMessage } from '@/utils/request'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const form = reactive({
  newPassword: '',
  confirmPassword: '',
})

onMounted(() => {
  const token = route.query.token as string
  if (!token) {
    errorMessage.value = '无效的重置链接，请重新申请'
  }
})

async function handleSubmit() {
  errorMessage.value = ''

  const token = route.query.token as string
  if (!token) {
    errorMessage.value = '无效的重置链接，请重新申请'
    return
  }

  if (!form.newPassword) {
    errorMessage.value = '请输入新密码'
    return
  }
  if (form.newPassword.length < 6) {
    errorMessage.value = '密码长度不能少于6位'
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }

  submitting.value = true

  try {
    await confirmPasswordReset({
      token,
      newPassword: form.newPassword,
    })
    successMessage.value = '密码重置成功，请使用新密码登录。'
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '重置失败，链接可能已过期，请重新申请')
  } finally {
    submitting.value = false
  }
}
</script>