import type { LoginLog } from './log'

export interface DashboardSummary {
  userCount: number
  activeUserCount: number
  roleCount: number
  permissionCount: number
  menuCount: number
  todayLogins: number
  todayFailedLogins: number
  todayOperations: number
  recentLogins: LoginLog[]
  generatedAt: string
}
