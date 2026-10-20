export interface Permission {
  id: number
  code: string
  name: string
  description?: string | null
  status: string
  createdAt?: string | null
}

export interface PermissionPayload {
  code?: string
  name: string
  description?: string
  status: string
}

export interface PermissionQuery {
  keyword?: string
  page?: number
  size?: number
}
