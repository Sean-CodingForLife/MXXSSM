import { api } from './http'
import type { PageResult } from '@/types/api'
import type { User, UserPayload, UserQuery } from '@/types/user'

export const userApi = {
  page(query: UserQuery) {
    return api.get<PageResult<User>>('/admin/users', { ...query })
  },
  get(id: number) {
    return api.get<User>(`/admin/users/${id}`)
  },
  create(payload: UserPayload) {
    return api.post<User>('/admin/users', payload)
  },
  update(id: number, payload: UserPayload) {
    return api.put<User>(`/admin/users/${id}`, payload)
  },
  remove(id: number) {
    return api.delete<null>(`/admin/users/${id}`)
  },
}
