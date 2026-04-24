import { http } from '@/utils/request'
import type { Announcement, ClassDetail, ClassInfo, ClassMember, CreateAnnouncementRequest } from '@/api/types'

export function getMyClasses() {
  return http.get<ClassInfo[]>('/classes')
}

export function getClassDetail(classId: number) {
  return http.get<ClassDetail>(`/classes/${classId}`)
}

export function getClassMembers(classId: number) {
  return http.get<ClassMember[]>(`/classes/${classId}/members`)
}

export function getClassAnnouncements(classId: number) {
  return http.get<Announcement[]>(`/classes/${classId}/announcements`)
}

export function createClassAnnouncement(classId: number, payload: CreateAnnouncementRequest) {
  return http.post<void>(`/classes/${classId}/announcements`, payload)
}
