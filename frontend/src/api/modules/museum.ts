import { http } from '@/utils/request'
import type { MuseumResource, PageResponse, SyncMuseumResourcesRequest } from '@/api/types'

export function getMuseumResources(params?: Record<string, unknown>) {
  return http.get<PageResponse<MuseumResource>>('/museum/resources', { params })
}

export function syncMuseumResources(payload: SyncMuseumResourcesRequest) {
  return http.post<void>('/museum/resources', payload)
}

export function getMuseumResourceDetail(resourceId: number) {
  return http.get<MuseumResource>(`/museum/resources/${resourceId}`)
}
