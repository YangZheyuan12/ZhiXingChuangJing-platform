<template>
  <header class="sticky top-0 z-20 border-b border-slate-800/60 bg-slate-950/95 text-slate-100 shadow-sm backdrop-blur">
    <div class="mx-auto flex h-16 w-full max-w-7xl items-center gap-6 px-4 sm:px-6 lg:px-8">
      <RouterLink to="/admin" class="flex shrink-0 items-center gap-2 text-base font-semibold tracking-tight text-white">
        <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-500/90 text-xs font-bold">管</span>
        <span>知行创境 · 管理后台</span>
      </RouterLink>

      <nav class="min-w-0 flex-1 overflow-x-auto">
        <ul class="flex min-w-max items-center gap-1">
          <li v-for="item in adminNavItems" :key="item.to">
            <RouterLink
              :to="item.to"
              class="flex items-center gap-2 rounded-md px-3 py-2 text-sm transition"
              :class="isActive(item.match) ? 'bg-indigo-500/20 text-indigo-200 ring-1 ring-indigo-400/40' : 'text-slate-300 hover:bg-slate-800 hover:text-white'"
            >
              <svg class="h-4 w-4 shrink-0" viewBox="0 0 20 20" fill="none" aria-hidden="true">
                <path
                  :d="item.icon"
                  stroke="currentColor"
                  stroke-width="1.6"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              <span>{{ item.label }}</span>
            </RouterLink>
          </li>
        </ul>
      </nav>

      <RouterLink
        to="/"
        class="shrink-0 rounded-md border border-slate-700 px-3 py-2 text-xs text-slate-300 transition hover:border-indigo-400 hover:text-white"
      >
        前往创作端
      </RouterLink>

      <div class="shrink-0">
        <UserAvatar />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import UserAvatar from '@/components/layout/UserAvatar.vue'

const route = useRoute()

const adminNavItems = [
  {
    label: '总览',
    to: '/admin',
    match: '/admin',
    icon: 'M3.5 8.5 10 3l6.5 5.5v7.5h-4.5v-4.5h-4v4.5H3.5V8.5Z',
  },
  {
    label: '班级审核',
    to: '/admin/classes',
    match: '/admin/classes',
    icon: 'M3.5 15.5c0-2 2.6-3.5 5.5-3.5s5.5 1.5 5.5 3.5M9 10a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm6 1V6m-2.5 2.5h5',
  },
  {
    label: '任务审核',
    to: '/admin/tasks',
    match: '/admin/tasks',
    icon: 'M5 4.5h10M5 9.5h10M5 14.5h6M3.5 3.5h13v13h-13z',
  },
  {
    label: '社区审核',
    to: '/admin/community',
    match: '/admin/community',
    icon: 'M4 5.5h12v8H8l-4 3v-11Z',
  },
  {
    label: '素材审核',
    to: '/admin/assets',
    match: '/admin/assets',
    icon: 'M4 4.5h12v11H4zM7 8.5h.01M5.5 13l3.5-3 2.5 2 2-1.5 2 2.5',
  },
  {
    label: '文博资源',
    to: '/admin/museum',
    match: '/admin/museum',
    icon: 'M3.5 16.5h13M5 16.5v-7m3 7v-7m4 7v-7m3 7v-7M2.5 9.5 10 4l7.5 5.5h-15Z',
  },
  {
    label: '系统通知',
    to: '/admin/notifications',
    match: '/admin/notifications',
    icon: 'M10 3.5a3 3 0 0 0-3 3v1.2c0 .7-.2 1.3-.6 1.9L5 12.5h10l-1.4-2.9a3.8 3.8 0 0 1-.6-1.9V6.5a3 3 0 0 0-3-3Zm-1.5 12a1.5 1.5 0 0 0 3 0',
  },
]

function isActive(match: string) {
  return match === '/admin' ? route.path === '/admin' : route.path.startsWith(match)
}
</script>
