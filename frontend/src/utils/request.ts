import axios, { AxiosError, type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'
import type { ApiEnvelope } from '@/api/types'

const TOKEN_STORAGE_KEY = 'zxcyj-token'
const REQUEST_TIMEOUT = 15000

export class ApiError extends Error {
  code?: number
  status?: number
  requestId?: string

  constructor(message: string, options?: { code?: number; status?: number; requestId?: string }) {
    super(message)
    this.name = 'ApiError'
    this.code = options?.code
    this.status = options?.status
    this.requestId = options?.requestId
  }
}

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: REQUEST_TIMEOUT,
})

let unauthorizedRedirectTask: Promise<void> | null = null

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY)

  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

instance.interceptors.response.use(
  async (response) => {
    const envelope = response.data

    if (isApiEnvelope(envelope) && envelope.code !== 0) {
      if (envelope.code === 401) {
        await handleUnauthorized()
      }

      throw new ApiError(envelope.message || '请求失败', {
        code: envelope.code,
        status: response.status,
        requestId: envelope.requestId,
      })
    }

    return response
  },
  async (error: AxiosError<ApiEnvelope<unknown>>) => {
    const status = error.response?.status
    const envelope = error.response?.data

    if (status === 401 || envelope?.code === 401) {
      await handleUnauthorized()
    }

    throw new ApiError(
      envelope?.message || error.message || '网络请求失败',
      {
        code: envelope?.code,
        status,
        requestId: envelope?.requestId,
      },
    )
  },
)

async function handleUnauthorized() {
  const authStore = useAuthStore()
  authStore.clearAuth()

  const currentPath = router.currentRoute.value.fullPath
  if (currentPath === '/login') {
    return
  }

  if (!unauthorizedRedirectTask) {
    unauthorizedRedirectTask = router
      .replace({ path: '/login', query: { redirect: currentPath } })
      .then(() => undefined)
      .finally(() => {
        unauthorizedRedirectTask = null
      })
  }

  await unauthorizedRedirectTask
}

function unwrap<T>(promise: Promise<AxiosResponse<ApiEnvelope<T>>>) {
  return promise.then((response) => {
    if (!isApiEnvelope(response.data)) {
      throw new ApiError('响应格式错误', { status: response.status })
    }

    return response.data.data
  })
}

function isApiEnvelope<T>(value: unknown): value is ApiEnvelope<T> {
  if (!value || typeof value !== 'object') {
    return false
  }

  return 'code' in value && 'message' in value && 'data' in value
}

export const http = {
  get<T>(url: string, config?: AxiosRequestConfig) {
    return unwrap<T>(instance.get<ApiEnvelope<T>>(url, config))
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(instance.post<ApiEnvelope<T>>(url, data, config))
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(instance.put<ApiEnvelope<T>>(url, data, config))
  },
  delete<T>(url: string, config?: AxiosRequestConfig) {
    return unwrap<T>(instance.delete<ApiEnvelope<T>>(url, config))
  },
  raw: instance as AxiosInstance,
}

export function getErrorMessage(error: unknown, fallback = '操作失败，请稍后重试') {
  if (error instanceof ApiError) {
    return error.message
  }

  if (error instanceof Error) {
    return error.message
  }

  return fallback
}
