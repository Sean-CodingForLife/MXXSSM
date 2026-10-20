export interface Role {
  id: number
  code: string
  name: string
  description?: string | null
  status: string
  userCount: number
  createdAt?: string | null
}

export interface RolePayload {
  code?: string
  name: string
  description?: string
  status: string
}

export interface RoleQuery {
  keyword?: string
  page?: number
  size?: number
}
