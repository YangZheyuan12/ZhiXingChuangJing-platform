import { http } from '@/utils/request'
import type {
  Comment,
  CommunityExhibition,
  CommunityExhibitionQuery,
  CommunityComments,
  CreateCommentRequest,
  ExhibitionViewerData,
  FeatureExhibitionRequest,
  PageResponse,
  ShareExhibitionRequest,
} from '@/api/types'

export function getCommunityExhibitions(params?: CommunityExhibitionQuery) {
  return http.get<PageResponse<CommunityExhibition>>('/community/exhibitions', { params })
}

export function getCommunityExhibitionDetail(exhibitionId: number) {
  return http.get<ExhibitionViewerData>(`/community/exhibitions/${exhibitionId}`)
}

export function getCommunityComments(exhibitionId: number) {
  return http.get<CommunityComments>(`/community/exhibitions/${exhibitionId}/comments`)
}

export function createCommunityComment(exhibitionId: number, payload: CreateCommentRequest) {
  return http.post<Comment>(`/community/exhibitions/${exhibitionId}/comments`, payload)
}

export function likeCommunityExhibition(exhibitionId: number) {
  return http.post<void>(`/community/exhibitions/${exhibitionId}/like`)
}

export function unlikeCommunityExhibition(exhibitionId: number) {
  return http.delete<void>(`/community/exhibitions/${exhibitionId}/like`)
}

export function favoriteCommunityExhibition(exhibitionId: number) {
  return http.post<void>(`/community/exhibitions/${exhibitionId}/favorite`)
}

export function unfavoriteCommunityExhibition(exhibitionId: number) {
  return http.delete<void>(`/community/exhibitions/${exhibitionId}/favorite`)
}

export function shareCommunityExhibition(exhibitionId: number, payload: ShareExhibitionRequest) {
  return http.post<void>(`/community/exhibitions/${exhibitionId}/share`, payload)
}

export function featureCommunityExhibition(exhibitionId: number, payload: FeatureExhibitionRequest) {
  return http.post<void>(`/community/exhibitions/${exhibitionId}/feature`, payload)
}
