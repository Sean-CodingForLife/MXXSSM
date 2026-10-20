export interface MenuItem {
  id: number
  code: string
  name: string
  path: string
  icon?: string | null
  groupName: string
  permissionCode: string
  sortOrder: number
  status: string
  createdAt?: string
}

export interface CurrentUser {
  id: number
  username: string
  displayName: string
  email?: string | null
  phone?: string | null
  lastLoginAt?: string | null
  roles: string[]
  permissions: string[]
  menus: MenuItem[]
}

export interface LoginPayload {
  username: string
  password: string
  captcha?: string
}

export interface RegisterPayload {
  username: string
  displayName: string
  password: string
  email?: string
  phone?: string
}

export interface ProfilePayload {
  displayName: string
  email?: string
  phone?: string
}

export interface PasswordPayload {
  currentPassword: string
  newPassword: string
}

export interface CaptchaResponse {
  image: string | null
  enabled: boolean
  ttlSeconds: number
}

export interface CsrfResponse {
  token: string | null
  headerName: string | null
  parameterName: string | null
}

export interface PublicSettings {
  siteName: string
  captchaEnabled: boolean
  registrationEnabled: boolean
  defaultLocale: string
  version: string
}
