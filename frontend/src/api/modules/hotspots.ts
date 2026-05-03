import { http } from '@/utils/request'
import type {
  CreateHotspotRequest,
  HotspotDetail,
  UpdateHotspotRequest,
} from '@/api/types'

/**
 * 列出指定展区下的所有热点。
 */
export function listHotspots(exhibitionId: number, zoneId: number) {
  return http.get<HotspotDetail[]>(
    `/exhibitions/${exhibitionId}/zones/${zoneId}/hotspots`,
  )
}

/**
 * 创建热点（在指定展区内）。
 */
export function createHotspot(
  exhibitionId: number,
  zoneId: number,
  payload: CreateHotspotRequest,
) {
  return http.post<HotspotDetail>(
    `/exhibitions/${exhibitionId}/zones/${zoneId}/hotspots`,
    payload,
  )
}

/**
 * 更新热点；字段为空表示保留原值。
 */
export function updateHotspot(
  exhibitionId: number,
  hotspotId: number,
  payload: UpdateHotspotRequest,
) {
  return http.put<HotspotDetail>(
    `/exhibitions/${exhibitionId}/hotspots/${hotspotId}`,
    payload,
  )
}

/**
 * 删除热点。
 */
export function deleteHotspot(exhibitionId: number, hotspotId: number) {
  return http.delete<void>(`/exhibitions/${exhibitionId}/hotspots/${hotspotId}`)
}
