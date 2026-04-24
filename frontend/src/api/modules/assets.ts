import { http } from '@/utils/request'
import type { Asset, AssetUploadData, PageResponse } from '@/api/types'

export function uploadAsset(file: File, options?: { folder?: string; bizType?: string }) {
  const formData = new FormData()
  formData.append('file', file)
  if (options?.folder) {
    formData.append('folder', options.folder)
  }
  if (options?.bizType) {
    formData.append('bizType', options.bizType)
  }
  return http.post<AssetUploadData>('/assets/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function getMyAssets(params?: { assetType?: string; sourceType?: string; page?: number; pageSize?: number }) {
  return http.get<PageResponse<Asset>>('/assets', { params })
}
