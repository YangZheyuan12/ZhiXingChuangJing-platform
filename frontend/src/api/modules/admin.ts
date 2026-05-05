import { http } from '@/utils/request'
import type { ReviewTeacherRegistrationRequest, TeacherRegistrationListResponse } from '@/api/types'

export function getTeacherRegistrations(status = 'pending') {
  return http.get<TeacherRegistrationListResponse>('/admin/teacher-registrations', {
    params: { status },
  })
}

export function approveTeacherRegistration(userId: number, payload: ReviewTeacherRegistrationRequest) {
  return http.post<void>(`/admin/teacher-registrations/${userId}/approve`, payload)
}

export function rejectTeacherRegistration(userId: number, payload: ReviewTeacherRegistrationRequest) {
  return http.post<void>(`/admin/teacher-registrations/${userId}/reject`, payload)
}
