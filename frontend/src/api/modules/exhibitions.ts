import { http } from '@/utils/request'
import type {
  AddExhibitionMembersRequest,
  CreateExhibitionRequest,
  DigitalHuman,
  ExhibitionDetail,
  ExhibitionMember,
  ExhibitionTemplate,
  ExhibitionVersion,
  ExhibitionViewerData,
  ExhibitionSummary,
  PageResponse,
  PublishExhibitionRequest,
  SaveExhibitionVersionRequest,
  UpdateExhibitionRequest,
  UpsertDigitalHumanRequest,
} from '@/api/types'

export function createExhibition(payload: CreateExhibitionRequest) {
  return http.post<ExhibitionDetail>('/exhibitions', payload)
}

export function updateExhibition(exhibitionId: number, payload: UpdateExhibitionRequest) {
  return http.put<void>(`/exhibitions/${exhibitionId}`, payload)
}

export function getMyExhibitions(params?: Record<string, unknown>) {
  return http.get<PageResponse<ExhibitionSummary>>('/exhibitions/my', { params })
}

export function getExhibitionDetail(exhibitionId: number) {
  return http.get<ExhibitionDetail>(`/exhibitions/${exhibitionId}`)
}

export function getExhibitionMembers(exhibitionId: number) {
  return http.get<ExhibitionMember[]>(`/exhibitions/${exhibitionId}/members`)
}

export function addExhibitionMembers(exhibitionId: number, payload: AddExhibitionMembersRequest) {
  return http.post<void>(`/exhibitions/${exhibitionId}/members`, payload)
}

export function getExhibitionVersions(exhibitionId: number) {
  return http.get<ExhibitionVersion[]>(`/exhibitions/${exhibitionId}/versions`)
}

export function saveExhibitionVersion(exhibitionId: number, payload: SaveExhibitionVersionRequest) {
  return http.post<ExhibitionVersion>(`/exhibitions/${exhibitionId}/versions`, payload)
}

export function publishExhibition(exhibitionId: number, payload: PublishExhibitionRequest) {
  return http.post<void>(`/exhibitions/${exhibitionId}/publish`, payload)
}

export function getExhibitionViewer(exhibitionId: number) {
  return http.get<ExhibitionViewerData>(`/exhibitions/${exhibitionId}/viewer`)
}

export function getExhibitionDigitalHuman(exhibitionId: number) {
  return http.get<DigitalHuman | null>(`/exhibitions/${exhibitionId}/digital-human`)
}

export function upsertExhibitionDigitalHuman(exhibitionId: number, payload: UpsertDigitalHumanRequest) {
  return http.put<DigitalHuman>(`/exhibitions/${exhibitionId}/digital-human`, payload)
}

export function getExhibitionTemplates() {
  return http.get<ExhibitionTemplate[]>('/exhibition-templates')
}
