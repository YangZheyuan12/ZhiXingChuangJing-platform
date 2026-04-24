<template>
  <div class="flex min-h-screen items-center justify-center px-4 py-10">
    <div class="grid w-full max-w-5xl gap-6 lg:grid-cols-[1.2fr_0.8fr]">
      <section class="hidden rounded-[2rem] bg-gradient-to-br from-brand-900 via-brand-700 to-brand-500 p-10 text-white shadow-panel lg:block">
        <p class="text-sm uppercase tracking-[0.36em] text-white/60">Creative Learning</p>
        <h1 class="mt-6 text-4xl font-bold leading-tight">让历史叙事、数字展厅与 AI 数字人一起进入课堂。</h1>
        <p class="mt-6 max-w-xl text-base leading-7 text-white/78">
          面向教师与学生的项目式学习平台，围绕任务、创作、展示、点评形成闭环。
        </p>
      </section>

      <section class="panel-card p-8">
        <p class="text-sm uppercase tracking-[0.36em] text-brand-600">知行创境</p>
        <h2 class="mt-3 text-3xl font-semibold text-slate-900">登录平台</h2>
        <form class="mt-8 space-y-5" @submit.prevent="handleSubmit">
          <p v-if="errorMessage" class="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-600">
            {{ errorMessage }}
          </p>
          <label class="block">
            <span class="form-label">账号</span>
            <input v-model="form.account" class="form-control" />
          </label>
          <label class="block">
            <span class="form-label">密码</span>
            <input v-model="form.password" type="password" class="form-control" />
          </label>
          <button
            type="submit"
            :disabled="submitting"
            class="w-full rounded-xl bg-brand-600 px-4 py-3 text-sm font-medium text-white transition hover:bg-brand-700"
          >
            {{ submitting ? '登录中...' : '登录' }}
          </button>
        </form>
        <div class="mt-6 flex items-center justify-between border-t border-neutral-200 pt-4 text-sm text-neutral-500">
          <span>还没有账号</span>
          <RouterLink to="/register" class="text-neutral-900 transition hover:text-blue-600">去注册</RouterLink>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getErrorMessage } from '@/utils/request'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const submitting = ref(false)
const errorMessage = ref('')

const form = reactive({
  account: '',
  password: '',
})

async function handleSubmit() {
  submitting.value = true
  errorMessage.value = ''

  try {
    await authStore.login(form)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '登录失败，请检查账号密码')
  } finally {
    submitting.value = false
  }
}
</script>
