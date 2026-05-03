import { http } from '@/utils/request'
import type { GenerateNarrationRequest, GenerateNarrationResponse } from '@/api/types'

export function generateNarration(payload: GenerateNarrationRequest) {
  return http.post<GenerateNarrationResponse>('/ai/narration', payload)
}
