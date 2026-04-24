import { ref } from 'vue'
import { defineStore } from 'pinia'

type ToastTone = 'success' | 'error' | 'info'

interface ToastItem {
  id: number
  message: string
  tone: ToastTone
}

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const toasts = ref<ToastItem[]>([])
  let nextToastId = 1

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function showToast(message: string, tone: ToastTone = 'info', duration = 2600) {
    const id = nextToastId++
    toasts.value.push({ id, message, tone })

    window.setTimeout(() => {
      removeToast(id)
    }, duration)
  }

  function removeToast(id: number) {
    toasts.value = toasts.value.filter((item) => item.id !== id)
  }

  return {
    sidebarCollapsed,
    toggleSidebar,
    toasts,
    showToast,
    removeToast,
  }
})
