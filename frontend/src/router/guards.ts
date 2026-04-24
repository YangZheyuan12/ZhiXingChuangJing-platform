import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export function setupRouterGuards(router: Router) {
  router.beforeEach(async (to) => {
    const authStore = useAuthStore()
    const isPublic = Boolean(to.meta.public)
    const isAdminRoute = to.path.startsWith('/admin')

    if (authStore.token && !authStore.initialized) {
      try {
        await authStore.bootstrap()
      } catch {
        authStore.clearAuth()
      }
    }

    if (!isPublic && !authStore.token) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }

    if ((to.path === '/login' || to.path === '/register') && authStore.isLoggedIn) {
      return { path: authStore.user?.role === 'admin' ? '/admin' : '/' }
    }

    if (!isPublic && authStore.user?.role === 'admin') {
      if (to.path === '/') {
        return { path: '/admin' }
      }
    }

    if (isAdminRoute && authStore.user?.role !== 'admin') {
      return { path: '/' }
    }

    return true
  })
}
