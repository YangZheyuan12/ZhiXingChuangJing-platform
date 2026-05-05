import { http } from '@/utils/request'
import { normalizeClientPassword } from '@/utils/crypto'
import type {
  CaptchaResponse,
  CurrentUser,
  LoginRequest,
  LoginResponse,
  PasswordResetConfirmRequest,
  PasswordResetRequest,
  PasswordResetRequestResponse,
  RegisterRequest,
  RegisterResponse,
  SchoolInfo,
} from '@/api/types'

export async function login(data: LoginRequest) {
  return http.post<LoginResponse>('/auth/login', {
    ...data,
    password: await normalizeClientPassword(data.password),
  })
}

export function getCaptcha() {
  return http.get<CaptchaResponse>('/auth/captcha')
}

export function getSchools() {
  return http.get<SchoolInfo[]>('/auth/schools')
}

export async function register(data: RegisterRequest) {
  return http.post<RegisterResponse>('/auth/register', {
    ...data,
    password: await normalizeClientPassword(data.password),
  })
}

export function requestPasswordReset(data: PasswordResetRequest) {
  return http.post<PasswordResetRequestResponse>('/auth/password-reset/request', data)
}

export async function confirmPasswordReset(data: PasswordResetConfirmRequest) {
  return http.post<void>('/auth/password-reset/confirm', {
    ...data,
    newPassword: await normalizeClientPassword(data.newPassword),
  })
}

export function getCurrentUser() {
  return http.get<CurrentUser>('/auth/me')
}
