import { http } from '@/utils/request'
import { normalizeClientPassword } from '@/utils/crypto'
import type { PortfolioItem, UpdatePasswordRequest, UpdateProfileRequest, UserHomepage, UserProfile } from '@/api/types'

export function getMyProfile() {
  return http.get<UserProfile>('/users/me')
}

export function updateMyProfile(data: UpdateProfileRequest) {
  return http.put<UserProfile>('/users/me/profile', data)
}

export async function updateMyPassword(data: UpdatePasswordRequest) {
  return http.put<null>('/users/me/password', {
    oldPassword: await normalizeClientPassword(data.oldPassword),
    newPassword: await normalizeClientPassword(data.newPassword),
  })
}

export function getUserHomepage(userId: number) {
  return http.get<UserHomepage>(`/users/${userId}/homepage`)
}

export function getMyPortfolio() {
  return http.get<PortfolioItem[]>('/users/me/portfolio')
}
