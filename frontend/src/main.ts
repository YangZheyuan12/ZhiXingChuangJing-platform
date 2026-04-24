import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/styles/index.css'
import { useAuthStore } from '@/stores/auth'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

async function bootstrapApp() {
  const authStore = useAuthStore(pinia)
  try {
    await authStore.bootstrap()
  } catch {
    authStore.clearAuth()
  }

  app.mount('#app')
}

void bootstrapApp()
