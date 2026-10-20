export interface ApiResponse<T> {
  success: boolean
  code: string
  message: string
  data: T
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface PageQuery {
  page?: number
  size?: number
}

export interface ValidationError {
  field: string
  message: string
}
