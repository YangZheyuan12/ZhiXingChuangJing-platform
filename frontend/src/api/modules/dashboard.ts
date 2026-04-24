import { http } from '@/utils/request'
import type { DashboardOverview } from '@/api/types'

export function getDashboardOverview(role?: string) {
  return http.get<DashboardOverview>('/dashboard/overview', {
    params: { role },
  })
}
