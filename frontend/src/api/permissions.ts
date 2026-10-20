import { api } from './http'
import type { PageResult } from '@/types/api'
import type { Permission, PermissionPayload, PermissionQuery } from '@/types/permission'

export const permissionApi = {
  page(query: PermissionQuery) {
    return api.get<PageResult<Permission>>('/admin/permissions', { ...query })
  },
  all() {
    return api.get<Permission[]>('/admin/permissions/all')
  },
  create(payload: PermissionPayload) {
    return api.post<Permission>('/admin/permissions', payload)
  },
  update(id: number, payload: PermissionPayload) {
    return api.put<Permission>(`/admin/permissions/${id}`, payload)
  },
  remove(id: number) {
    return api.delete<null>(`/admin/permissions/${id}`)
  },
}
