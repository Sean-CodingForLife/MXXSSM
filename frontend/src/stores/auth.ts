import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { ensureCsrfToken } from '@/api/http'
import type { CurrentUser, LoginPayload } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<CurrentUser | null>(null)
  const initialized = ref(false)
  const loading = ref(false)

  const isAuthenticated = computed(() => user.value !== null)
  const permissions = computed(() => new Set(user.value?.permissions ?? []))
  const roles = computed(() => new Set(user.value?.roles ?? []))
  const menus = computed(() => user.value?.menus ?? [])
  const isAdmin = computed(() => roles.value.has('ADMIN'))
  const displayName = computed(() => user.value?.displayName || user.value?.username || '')
  const initials = computed(() => {
    const name = displayName.value.trim()
    return name ? name.slice(0, 1).toUpperCase() : 'U'
  })

  function hasPermission(code?: string | null): boolean {
    if (!code) {
      return true
    }
    return permissions.value.has(code)
  }

  function hasAnyPermission(codes: string[]): boolean {
    return codes.some((code) => hasPermission(code))
  }

  async function fetchMe(silent = true): Promise<CurrentUser | null> {
    loading.value = true
    try {
      user.value = await authApi.me(silent)
      return user.value
    } catch {
      user.value = null
      return null
    } finally {
      initialized.value = true
      loading.value = false
    }
  }

  async function login(payload: LoginPayload): Promise<CurrentUser> {
    const current = await authApi.login(payload)
    user.value = current
    initialized.value = true
    await ensureCsrfToken(true).catch(() => undefined)
    return current
  }

  async function logout(): Promise<void> {
    try {
      await authApi.logout()
    } catch {
      // The local session is cleared even if the server call fails.
    } finally {
      user.value = null
      await ensureCsrfToken(true).catch(() => undefined)
    }
  }

  function setUser(current: CurrentUser): void {
    user.value = current
  }

  function reset(): void {
    user.value = null
  }

  return {
    user,
    initialized,
    loading,
    isAuthenticated,
    permissions,
    roles,
    menus,
    isAdmin,
    displayName,
    initials,
    hasPermission,
    hasAnyPermission,
    fetchMe,
    login,
    logout,
    setUser,
    reset,
  }
})
