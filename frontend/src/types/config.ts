export interface SystemConfig {
  id: number
  configKey: string
  configValue: string
  description?: string | null
  status: string
  createdAt?: string | null
  updatedAt?: string | null
}

export interface ConfigPayload {
  configKey: string
  configValue: string
  description?: string
  status: string
}

export interface ConfigQuery {
  keyword?: string
  page?: number
  size?: number
}
