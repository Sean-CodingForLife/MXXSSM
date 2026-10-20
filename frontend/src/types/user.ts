export interface User {
  id: number
  username: string
  displayName: string
  email?: string | null
  phone?: string | null
  status: string
  roles: string[]
  lastLoginAt?: string | null
  createdAt?: string | null
}

export interface UserPayload {
  username?: string
  displayName: string
  password?: string
  email?: string
  phone?: string
  roles: string[]
  status: string
}

export interface UserQuery {
  keyword?: string
  status?: string
  role?: string
  page?: number
  size?: number
}
