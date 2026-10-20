import { api } from './http'
import type { PageResult } from '@/types/api'
import type { LoginLog, LogQuery, OperationLog } from '@/types/log'

export const logApi = {
  loginLogs(query: LogQuery) {
    return api.get<PageResult<LoginLog>>('/admin/logs/login', { ...query })
  },
  operationLogs(query: LogQuery) {
    return api.get<PageResult<OperationLog>>('/admin/logs/operation', { ...query })
  },
}
