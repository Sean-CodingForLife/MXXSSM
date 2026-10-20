import { api } from './http'
import type { PageResult } from '@/types/api'
import type { Role, RolePayload, RoleQuery } from '@/types/role'
import type { Permission } from '@/types/permission'

export const roleApi = {
  page(query: RoleQuery) {
    return api.get<PageResult<Role>>('/admin/roles', { ...query })
  },
  all() {
    return api.get<Role[]>('/admin/roles/all')
  },
  create(payload: RolePayload) {
    return api.post<Role>('/admin/roles', payload)
  },
  update(id: number, payload: RolePayload) {
    return api.put<Role>(`/admin/roles/${id}`, payload)
  },
  remove(id: number) {
    return api.delete<null>(`/admin/roles/${id}`)
  },
  permissions(id: number) {
    return api.get<Permission[]>(`/admin/roles/${id}/permissions`)
  },
  updatePermissions(id: number, permissionIds: number[]) {
    return api.put<null>(`/admin/roles/${id}/permissions`, { permissionIds })
  },
}
