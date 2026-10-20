import axios, { type AxiosError, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { i18n } from '@/locales'
import type { ApiResponse } from '@/types/api'
import type { CsrfResponse } from '@/types/auth'

export class ApiError extends Error {
  readonly code: string
  readonly status?: number
  readonly data?: unknown

  constructor(message: string, code: string, status?: number, data?: unknown) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.status = status
    this.data = data
  }
}

const http = axios.create({
  baseURL: '/api/v1',
  timeout: 20000,
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  headers: {
    'Content-Type': 'application/json',
  },
})

let csrfPromise: Promise<void> | null = null

export function ensureCsrfToken(force = false): Promise<void> {
  if (force) {
    csrfPromise = null
  }
  if (!csrfPromise) {
    csrfPromise = http
      .get<ApiResponse<CsrfResponse>>('/auth/csrf', { silent: true })
      .then(() => undefined)
      .catch((error) => {
        csrfPromise = null
        throw error
      })
  }
  return csrfPromise
}

http.interceptors.request.use((config) => {
  config.headers.set('Accept-Language', i18n.global.locale.value)
  return config
})

http.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const config = error.config
    const status = error.response?.status
    const body = error.response?.data

    const isCsrfEndpoint = config?.url?.includes('/auth/csrf') ?? false
    if (status === 403 && config && !config.csrfRetry && !isCsrfEndpoint) {
      config.csrfRetry = true
      try {
        await ensureCsrfToken(true)
        return await http.request(config)
      } catch {
        // Fall through to the regular error handling below.
      }
    }

    const message =
      body?.message ??
      (error.code === 'ECONNABORTED'
        ? i18n.global.t('common.timeout')
        : i18n.global.t('common.requestFailed'))
    const apiError = new ApiError(message, body?.code ?? 'NETWORK_ERROR', status, body?.data)

    if (status === 401 && !config?.silent) {
      window.dispatchEvent(new CustomEvent('auth:unauthorized'))
    } else if (!config?.silent) {
      ElMessage.error(message)
    }

    return Promise.reject(apiError)
  },
)

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await http.request<ApiResponse<T>>(config)
  return response.data.data
}

export const api = {
  get<T>(url: string, params?: Record<string, unknown>, silent = false) {
    return request<T>({ url, method: 'get', params, silent })
  },
  post<T>(url: string, data?: unknown, silent = false) {
    return request<T>({ url, method: 'post', data, silent })
  },
  put<T>(url: string, data?: unknown, silent = false) {
    return request<T>({ url, method: 'put', data, silent })
  },
  delete<T>(url: string, silent = false) {
    return request<T>({ url, method: 'delete', silent })
  },
}
