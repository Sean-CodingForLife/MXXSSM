import { api } from './http'
import type { PageResult } from '@/types/api'
import type { ConfigPayload, ConfigQuery, SystemConfig } from '@/types/config'

export const configApi = {
  page(query: ConfigQuery) {
    return api.get<PageResult<SystemConfig>>('/admin/configs', { ...query })
  },
  all() {
    return api.get<SystemConfig[]>('/admin/configs/all')
  },
  create(payload: ConfigPayload) {
    return api.post<SystemConfig>('/admin/configs', payload)
  },
  update(id: number, payload: ConfigPayload) {
    return api.put<SystemConfig>(`/admin/configs/${id}`, payload)
  },
  remove(id: number) {
    return api.delete<null>(`/admin/configs/${id}`)
  },
}
