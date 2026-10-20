import { api } from './http'
import type {
  CaptchaResponse,
  CurrentUser,
  LoginPayload,
  PasswordPayload,
  ProfilePayload,
  RegisterPayload,
} from '@/types/auth'

export const authApi = {
  captcha() {
    return api.get<CaptchaResponse>('/auth/captcha', undefined, true)
  },
  login(payload: LoginPayload) {
    return api.post<CurrentUser>('/auth/login', payload, true)
  },
  logout() {
    return api.post<null>('/auth/logout', undefined, true)
  },
  register(payload: RegisterPayload) {
    return api.post<null>('/auth/register', payload, true)
  },
  me(silent = true) {
    return api.get<CurrentUser>('/auth/me', undefined, silent)
  },
  updateProfile(payload: ProfilePayload) {
    return api.put<CurrentUser>('/auth/profile', payload)
  },
  changePassword(payload: PasswordPayload) {
    return api.put<null>('/auth/password', payload)
  },
}
