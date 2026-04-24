<template>
  <div class="page-shell">
    <div class="flex min-h-screen">
      <aside class="hidden w-72 shrink-0 border-r border-white/40 bg-brand-900 text-white lg:flex lg:flex-col">
        <div class="border-b border-white/10 px-6 py-6">
          <p class="text-sm uppercase tracking-[0.32em] text-white/60">ZXCJ</p>
          <h1 class="mt-3 text-2xl font-bold">知行创境</h1>
          <p class="mt-2 text-sm text-white/70">教育创作平台</p>
        </div>
        <nav class="flex-1 space-y-2 px-4 py-6">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="block rounded-xl px-4 py-3 text-sm text-white/80 transition hover:bg-white/10 hover:text-white"
            active-class="bg-white/15 text-white"
          >
            {{ item.label }}
          </RouterLink>
        </nav>
      </aside>

      <div class="flex min-h-screen flex-1 flex-col">
        <header class="border-b border-[var(--zx-border)] bg-white/70 backdrop-blur">
          <div class="mx-auto flex w-full max-w-7xl items-center justify-between px-4 py-4 lg:px-8">
            <div>
              <p class="text-sm text-slate-500">沉浸式项目学习工作台</p>
              <h2 class="text-xl font-semibold text-slate-900">{{ currentTitle }}</h2>
            </div>
            <div class="flex items-center gap-4 text-right">
              <div>
                <p class="text-sm font-medium text-slate-800">{{ authStore.displayName }}</p>
                <p class="text-xs text-slate-500">{{ authStore.user?.role || 'visitor' }}</p>
              </div>
              <button
                type="button"
                class="rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-600 transition hover:border-brand-300 hover:text-brand-700"
                @click="handleLogout"
              >
                退出登录
              </button>
            </div>
          </div>
        </header>

        <main class="mx-auto w-full max-w-7xl flex-1 px-4 py-6 lg:px-8">
          <RouterView />
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const navItems = [
  { label: '工作台', to: '/' },
  { label: '班级管理', to: '/classes' },
  { label: '任务中心', to: '/tasks' },
  { label: '创作中心', to: '/exhibitions' },
  { label: '素材库', to: '/asset-library' },
  { label: '文博资源', to: '/museum' },
  { label: '社区展厅', to: '/community' },
  { label: '我的作品集', to: '/portfolio' },
  { label: '个人中心', to: '/profile' },
  { label: '消息通知', to: '/notifications' },
]

const currentTitle = computed(() => String(route.meta.title || '工作台'))

async function handleLogout() {
  authStore.clearAuth()
  await router.replace('/login')
}
</script>
