import { api } from './http'
import type { PublicSettings } from '@/types/auth'

export const publicApi = {
  settings() {
    return api.get<PublicSettings>('/public/settings', undefined, true)
  },
}
