import { http } from '@/utils/request'
import type { NotificationItem, PageResponse, ReadNotificationsRequest } from '@/api/types'

export function getNotifications(params?: { readStatus?: 'read' | 'unread'; page?: number; pageSize?: number }) {
  return http.get<PageResponse<NotificationItem>>('/notifications', { params })
}

export function readNotifications(payload: ReadNotificationsRequest) {
  return http.post<void>('/notifications/read', payload)
}
