import { http } from '@/utils/request'
import { normalizeClientPassword } from '@/utils/crypto'
import type { CaptchaResponse, CurrentUser, LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '@/api/types'

export async function login(data: LoginRequest) {
  return http.post<LoginResponse>('/auth/login', {
    ...data,
    password: await normalizeClientPassword(data.password),
  })
}

export function getCaptcha() {
  return http.get<CaptchaResponse>('/auth/captcha')
}

export async function register(data: RegisterRequest) {
  return http.post<RegisterResponse>('/auth/register', {
    ...data,
    password: await normalizeClientPassword(data.password),
  })
}

export function getCurrentUser() {
  return http.get<CurrentUser>('/auth/me')
}
