import { http } from '@/utils/request'
import type {
  CreateExhibitRequest,
  ExhibitDetail,
  ExhibitSummary,
  UpdateExhibitRequest,
} from '@/api/types'

export function listExhibits(exhibitionId: number, zoneId?: number) {
  const params = zoneId ? { zoneId } : {}
  return http.get<ExhibitSummary[]>(`/exhibitions/${exhibitionId}/exhibits`, { params })
}

export function getExhibit(exhibitionId: number, exhibitId: number) {
  return http.get<ExhibitDetail>(`/exhibitions/${exhibitionId}/exhibits/${exhibitId}`)
}

export function createExhibit(exhibitionId: number, payload: CreateExhibitRequest) {
  return http.post<ExhibitDetail>(`/exhibitions/${exhibitionId}/exhibits`, payload)
}

export function updateExhibit(exhibitionId: number, exhibitId: number, payload: UpdateExhibitRequest) {
  return http.put<void>(`/exhibitions/${exhibitionId}/exhibits/${exhibitId}`, payload)
}

export function deleteExhibit(exhibitionId: number, exhibitId: number) {
  return http.delete<void>(`/exhibitions/${exhibitionId}/exhibits/${exhibitId}`)
}
