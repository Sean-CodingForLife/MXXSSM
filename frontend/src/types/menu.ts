export interface Menu {
  id: number
  code: string
  name: string
  path: string
  icon?: string | null
  groupName: string
  permissionCode: string
  sortOrder: number
  status: string
  createdAt?: string | null
}

export interface MenuPayload {
  code: string
  name: string
  path: string
  icon?: string
  groupName: string
  permissionCode: string
  sortOrder: number
  status: string
}

export interface MenuQuery {
  keyword?: string
  page?: number
  size?: number
}
