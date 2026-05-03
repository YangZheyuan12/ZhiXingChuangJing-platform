import { http } from '@/utils/request'
import type { CreateZoneRequest, UpdateZoneRequest, ZoneDetail, ZoneSummary } from '@/api/types'

export function listZones(exhibitionId: number) {
  return http.get<ZoneSummary[]>(`/exhibitions/${exhibitionId}/zones`)
}

export function getZone(exhibitionId: number, zoneId: number) {
  return http.get<ZoneDetail>(`/exhibitions/${exhibitionId}/zones/${zoneId}`)
}

export function createZone(exhibitionId: number, payload: CreateZoneRequest) {
  return http.post<ZoneDetail>(`/exhibitions/${exhibitionId}/zones`, payload)
}

export function updateZone(exhibitionId: number, zoneId: number, payload: UpdateZoneRequest) {
  return http.put<void>(`/exhibitions/${exhibitionId}/zones/${zoneId}`, payload)
}

export function deleteZone(exhibitionId: number, zoneId: number) {
  return http.delete<void>(`/exhibitions/${exhibitionId}/zones/${zoneId}`)
}

export function reorderZones(exhibitionId: number, zoneIds: number[]) {
  return http.put<void>(`/exhibitions/${exhibitionId}/zones/reorder`, { zoneIds })
}

export function lockZone(exhibitionId: number, zoneId: number) {
  return http.put<void>(`/exhibitions/${exhibitionId}/zones/${zoneId}/lock`)
}

export function unlockZone(exhibitionId: number, zoneId: number) {
  return http.put<void>(`/exhibitions/${exhibitionId}/zones/${zoneId}/unlock`)
}

export function heartbeatZone(exhibitionId: number, zoneId: number) {
  return http.put<void>(`/exhibitions/${exhibitionId}/zones/${zoneId}/heartbeat`)
}
