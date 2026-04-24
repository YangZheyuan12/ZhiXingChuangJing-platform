<template>
  <header class="sticky top-0 z-20 border-b border-neutral-200 bg-white/95 backdrop-blur">
    <div class="mx-auto flex h-16 w-full max-w-7xl items-center gap-6 px-4 sm:px-6 lg:px-8">
      <RouterLink :to="homePath" class="shrink-0 text-base font-semibold tracking-tight text-neutral-900">
        知行创境
      </RouterLink>

      <AdminBadge v-if="isAdmin" class="hidden lg:inline-flex" />

      <nav class="min-w-0 flex-1 overflow-x-auto">
        <ul class="flex min-w-max items-center gap-1">
          <li v-for="item in currentNavItems" :key="item.to">
            <RouterLink
              :to="item.to"
              class="flex items-center gap-2 rounded-md px-3 py-2 text-sm transition"
              :class="isActive(item.match) ? 'bg-neutral-900 text-white' : 'text-neutral-600 hover:bg-neutral-100 hover:text-neutral-900'"
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

      <div class="shrink-0">
        <UserAvatar />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AdminBadge from '@/components/layout/AdminBadge.vue'
import UserAvatar from '@/components/layout/UserAvatar.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const studentTeacherNavItems = [
  {
    label: '工作台',
    to: '/',
    match: '/',
    icon: 'M3.5 8.5 10 3l6.5 5.5v7.5h-4.5v-4.5h-4v4.5H3.5V8.5Z',
  },
  {
    label: '班级',
    to: '/classes',
    match: '/classes',
    icon: 'M3.5 15.5c0-2 2.6-3.5 5.5-3.5s5.5 1.5 5.5 3.5M9 10a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm6 1V6m-2.5 2.5h5',
  },
  {
    label: '任务',
    to: '/tasks',
    match: '/tasks',
    icon: 'M5 4.5h10M5 9.5h10M5 14.5h6M3.5 3.5h13v13h-13z',
  },
  {
    label: '创作',
    to: '/exhibitions',
    match: '/exhibitions',
    icon: 'M4 14.5 14.8 3.7l1.5 1.5L5.5 16H4v-1.5ZM12 5.5l2.5 2.5',
  },
  {
    label: '社区',
    to: '/community',
    match: '/community',
    icon: 'M4 5.5h12v8H8l-4 3v-11Z',
  },
  {
    label: '素材',
    to: '/asset-library',
    match: '/asset-library',
    icon: 'M4 4.5h12v11H4zM7 8.5h.01M5.5 13l3.5-3 2.5 2 2-1.5 2 2.5',
  },
  {
    label: '文博',
    to: '/museum',
    match: '/museum',
    icon: 'M3.5 16.5h13M5 16.5v-7m3 7v-7m4 7v-7m3 7v-7M2.5 9.5 10 4l7.5 5.5h-15Z',
  },
  {
    label: '通知',
    to: '/notifications',
    match: '/notifications',
    icon: 'M10 3.5a3 3 0 0 0-3 3v1.2c0 .7-.2 1.3-.6 1.9L5 12.5h10l-1.4-2.9a3.8 3.8 0 0 1-.6-1.9V6.5a3 3 0 0 0-3-3Zm-1.5 12a1.5 1.5 0 0 0 3 0',
  },
  {
    label: '个人中心',
    to: '/profile',
    match: '/profile',
    icon: 'M10 4a3 3 0 1 1 0 6 3 3 0 0 1 0-6Zm-5 11c0-2.1 2.7-3.5 5-3.5s5 1.4 5 3.5v1H5v-1Z',
  },
]

const adminNavItems = [
  {
    label: '总览',
    to: '/admin',
    match: '/admin',
    icon: 'M3.5 8.5 10 3l6.5 5.5v7.5h-4.5v-4.5h-4v4.5H3.5V8.5Z',
  },
  {
    label: '班级',
    to: '/classes',
    match: '/classes',
    icon: 'M3.5 15.5c0-2 2.6-3.5 5.5-3.5s5.5 1.5 5.5 3.5M9 10a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm6 1V6m-2.5 2.5h5',
  },
  {
    label: '任务',
    to: '/tasks',
    match: '/tasks',
    icon: 'M5 4.5h10M5 9.5h10M5 14.5h6M3.5 3.5h13v13h-13z',
  },
  {
    label: '创作',
    to: '/exhibitions',
    match: '/exhibitions',
    icon: 'M4 14.5 14.8 3.7l1.5 1.5L5.5 16H4v-1.5ZM12 5.5l2.5 2.5',
  },
  {
    label: '社区',
    to: '/community',
    match: '/community',
    icon: 'M4 5.5h12v8H8l-4 3v-11Z',
  },
  {
    label: '素材',
    to: '/asset-library',
    match: '/asset-library',
    icon: 'M4 4.5h12v11H4zM7 8.5h.01M5.5 13l3.5-3 2.5 2 2-1.5 2 2.5',
  },
  {
    label: '文博',
    to: '/museum',
    match: '/museum',
    icon: 'M3.5 16.5h13M5 16.5v-7m3 7v-7m4 7v-7m3 7v-7M2.5 9.5 10 4l7.5 5.5h-15Z',
  },
  {
    label: '通知',
    to: '/notifications',
    match: '/notifications',
    icon: 'M10 3.5a3 3 0 0 0-3 3v1.2c0 .7-.2 1.3-.6 1.9L5 12.5h10l-1.4-2.9a3.8 3.8 0 0 1-.6-1.9V6.5a3 3 0 0 0-3-3Zm-1.5 12a1.5 1.5 0 0 0 3 0',
  },
  {
    label: '个人中心',
    to: '/profile',
    match: '/profile',
    icon: 'M10 4a3 3 0 1 1 0 6 3 3 0 0 1 0-6Zm-5 11c0-2.1 2.7-3.5 5-3.5s5 1.4 5 3.5v1H5v-1Z',
  },
]

const isAdmin = computed(() => authStore.user?.role === 'admin')
const homePath = computed(() => (isAdmin.value ? '/admin' : '/'))
const currentNavItems = computed(() => (isAdmin.value ? adminNavItems : studentTeacherNavItems))

function isActive(match: string) {
  return match === '/' ? route.path === '/' : route.path.startsWith(match)
}
</script>
