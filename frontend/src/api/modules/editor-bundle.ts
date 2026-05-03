import { http } from '@/utils/request'
import type {
  DraftBundleRequest,
  EditorBundleResponse,
  SaveBundleRequest,
  SaveBundleResult,
} from '@/api/types'

export function getEditorBundle(exhibitionId: number) {
  return http.get<EditorBundleResponse>(`/exhibitions/${exhibitionId}/editor-bundle`)
}

export function saveEditorBundle(exhibitionId: number, payload: SaveBundleRequest) {
  return http.put<SaveBundleResult>(`/exhibitions/${exhibitionId}/editor-bundle`, payload)
}

export function saveDraftBundle(exhibitionId: number, payload: DraftBundleRequest) {
  return http.put<void>(`/exhibitions/${exhibitionId}/draft-bundle`, payload)
}
