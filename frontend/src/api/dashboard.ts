import { api } from './http'
import type { DashboardSummary } from '@/types/dashboard'

export const dashboardApi = {
  summary() {
    return api.get<DashboardSummary>('/dashboard')
  },
}
