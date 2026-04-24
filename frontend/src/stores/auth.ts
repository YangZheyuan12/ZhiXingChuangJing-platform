import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser, login as loginApi } from '@/api/modules/auth'
import type { CurrentUser, LoginRequest, LoginUser } from '@/api/types'

const TOKEN_KEY = 'zxcyj-token'
const USER_KEY = 'zxcyj-user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<CurrentUser | null>(readUser())
  const initialized = ref(false)
  const bootstrapping = ref(false)
  let bootstrapTask: Promise<void> | null = null

  const isLoggedIn = computed(() => Boolean(token.value))
  const displayName = computed(() => user.value?.nickname || user.value?.realName || '未登录用户')

  function setSession(nextToken: string, nextUser: CurrentUser) {
    token.value = nextToken
    user.value = nextUser
    persistToken(nextToken)
    persistUser(nextUser)
  }

  function setToken(nextToken: string) {
    token.value = nextToken
    persistToken(nextToken)
  }

  function setUser(nextUser: CurrentUser) {
    user.value = nextUser
    persistUser(nextUser)
  }

  async function login(credentials: LoginRequest) {
    const response = await loginApi(credentials)
    setToken(response.token)
    setUser(normalizeLoginUser(response.user))
    initialized.value = false
    await bootstrap()
    return response
  }

  async function bootstrap(force = false) {
    if (!token.value) {
      clearAuth()
      initialized.value = true
      return
    }

    if (initialized.value && !force) {
      return
    }

    if (bootstrapTask && !force) {
      return bootstrapTask
    }

    bootstrapping.value = true
    bootstrapTask = (async () => {
      try {
        const currentUser = await getCurrentUser()
        setUser(currentUser)
      } finally {
        initialized.value = true
        bootstrapping.value = false
        bootstrapTask = null
      }
    })()

    return bootstrapTask
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    initialized.value = false
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return {
    token,
    user,
    initialized,
    bootstrapping,
    isLoggedIn,
    displayName,
    setSession,
    setToken,
    setUser,
    login,
    bootstrap,
    clearAuth,
  }
})

function persistToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

function persistUser(user: CurrentUser) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

function readUser(): CurrentUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as CurrentUser
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

function normalizeLoginUser(user: LoginUser): CurrentUser {
  return {
    id: user.id,
    role: user.role,
    nickname: user.nickname,
    realName: user.nickname,
    avatarUrl: null,
    schoolId: null,
  }
}
