import { createRouter, createWebHistory } from 'vue-router'
import { routes } from '@/router/routes'
import { setupRouterGuards } from '@/router/guards'

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

setupRouterGuards(router)

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${String(to.meta.title)} - 知行创境教育创作平台`
  }
})

export default router
