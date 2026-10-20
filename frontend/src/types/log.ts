export interface LoginLog {
  id: number
  username?: string | null
  status: string
  ip?: string | null
  userAgent?: string | null
  message?: string | null
  createdAt?: string | null
}

export interface OperationLog {
  id: number
  username?: string | null
  action: string
  method: string
  path: string
  status: string
  message?: string | null
  durationMs: number
  ip?: string | null
  createdAt?: string | null
}

export interface LogQuery {
  keyword?: string
  status?: string
  page?: number
  size?: number
}
