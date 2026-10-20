import { api } from './http'
import type { PageResult } from '@/types/api'
import type { Menu, MenuPayload, MenuQuery } from '@/types/menu'

export const menuApi = {
  page(query: MenuQuery) {
    return api.get<PageResult<Menu>>('/admin/menus', { ...query })
  },
  all() {
    return api.get<Menu[]>('/admin/menus/all')
  },
  create(payload: MenuPayload) {
    return api.post<Menu>('/admin/menus', payload)
  },
  update(id: number, payload: MenuPayload) {
    return api.put<Menu>(`/admin/menus/${id}`, payload)
  },
  remove(id: number) {
    return api.delete<null>(`/admin/menus/${id}`)
  },
}
