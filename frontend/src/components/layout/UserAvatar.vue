<template>
  <div ref="menuRef" class="relative">
    <button
      type="button"
      class="flex items-center gap-3 rounded-lg border border-neutral-200 bg-white px-3 py-2 text-left transition hover:border-neutral-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
      :aria-expanded="menuOpen"
      aria-haspopup="menu"
      @click="toggleMenu"
    >
      <span class="flex size-9 items-center justify-center overflow-hidden rounded-full border border-neutral-200 bg-neutral-100 text-sm font-semibold text-neutral-700">
        <img v-if="avatarUrl" :src="avatarUrl" :alt="`${displayName} avatar`" class="size-full object-cover" />
        <span v-else>{{ avatarFallback }}</span>
      </span>
      <span class="hidden min-w-0 sm:block">
        <span class="block truncate text-sm font-medium text-neutral-900">{{ displayName }}</span>
        <span class="block truncate text-xs text-neutral-500">{{ roleLabel }}</span>
      </span>
      <svg class="size-4 text-neutral-400" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
        <path
          fill-rule="evenodd"
          d="M5.23 7.21a.75.75 0 0 1 1.06.02L10 11.168l3.71-3.938a.75.75 0 1 1 1.08 1.04l-4.25 4.512a.75.75 0 0 1-1.08 0L5.21 8.27a.75.75 0 0 1 .02-1.06Z"
          clip-rule="evenodd"
        />
      </svg>
    </button>

    <div
      v-if="menuOpen"
      class="absolute right-0 z-30 mt-2 w-60 rounded-xl border border-neutral-200 bg-white p-2"
      role="menu"
      aria-label="用户菜单"
    >
      <div class="rounded-lg px-3 py-2">
        <p class="truncate text-sm font-medium text-neutral-900">{{ displayName }}</p>
        <p class="mt-1 truncate text-xs text-neutral-500">{{ roleLabel }}</p>
      </div>
      <button
        type="button"
        class="mt-1 flex w-full items-center justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white transition hover:bg-blue-700"
        role="menuitem"
        @click="handleLogout"
      >
        退出登录
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const menuRef = ref<HTMLElement | null>(null)
const menuOpen = ref(false)

const displayName = computed(() => authStore.displayName)
const avatarUrl = computed(() => authStore.user?.avatarUrl?.trim() || '')
const avatarFallback = computed(() => {
  const source = displayName.value.trim()
  return source ? source.slice(0, 1).toUpperCase() : '用'
})
const roleLabel = computed(() => formatRole(authStore.user?.role))

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function closeMenu() {
  menuOpen.value = false
}

async function handleLogout() {
  closeMenu()
  authStore.clearAuth()
  await router.replace('/login')
}

function handleDocumentClick(event: MouseEvent) {
  if (!menuRef.value || !(event.target instanceof Node)) {
    return
  }

  if (!menuRef.value.contains(event.target)) {
    closeMenu()
  }
}

function handleEscape(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    closeMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', handleDocumentClick)
  document.addEventListener('keydown', handleEscape)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick)
  document.removeEventListener('keydown', handleEscape)
})

function formatRole(role?: string) {
  switch (role) {
    case 'admin':
      return '管理员'
    case 'teacher':
      return '教师'
    case 'student':
      return '学生'
    case 'assistant':
      return '助教'
    default:
      return '访客'
  }
}
</script>
