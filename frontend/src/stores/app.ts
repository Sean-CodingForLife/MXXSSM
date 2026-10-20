import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { i18n, isSupportedLocale, persistLocale, getStoredLocale, type AppLocale } from '@/locales'
import { publicApi } from '@/api/public'
import type { PublicSettings } from '@/types/auth'

type Theme = 'light' | 'dark'

const THEME_KEY = 'mxxssm.theme'
const SIDEBAR_KEY = 'mxxssm.sidebar.collapsed'

function detectTheme(): Theme {
  if (typeof window === 'undefined') {
    return 'light'
  }
  const stored = window.localStorage.getItem(THEME_KEY)
  if (stored === 'light' || stored === 'dark') {
    return stored
  }
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

function applyTheme(theme: Theme): void {
  if (typeof document === 'undefined') {
    return
  }
  document.documentElement.classList.toggle('dark', theme === 'dark')
  document.documentElement.dataset.theme = theme
}

export const useAppStore = defineStore('app', () => {
  const locale = ref<AppLocale>(i18n.global.locale.value as AppLocale)
  const theme = ref<Theme>(detectTheme())
  const sidebarCollapsed = ref(
    typeof window !== 'undefined' && window.localStorage.getItem(SIDEBAR_KEY) === '1',
  )
  const settings = ref<PublicSettings | null>(null)
  const settingsLoaded = ref(false)

  applyTheme(theme.value)

  const siteName = computed(() => settings.value?.siteName || 'MXXSSM')
  const version = computed(() => settings.value?.version || '2.0.0')
  const captchaEnabled = computed(() => settings.value?.captchaEnabled ?? true)
  const registrationEnabled = computed(() => settings.value?.registrationEnabled ?? true)

  function setLocale(value: AppLocale): void {
    locale.value = value
    i18n.global.locale.value = value
    persistLocale(value)
    if (typeof document !== 'undefined') {
      document.documentElement.lang = value
    }
  }

  function setTheme(value: Theme): void {
    theme.value = value
    applyTheme(value)
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(THEME_KEY, value)
    }
  }

  function toggleTheme(): void {
    setTheme(theme.value === 'dark' ? 'light' : 'dark')
  }

  function setSidebarCollapsed(value: boolean): void {
    sidebarCollapsed.value = value
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(SIDEBAR_KEY, value ? '1' : '0')
    }
  }

  function toggleSidebar(): void {
    setSidebarCollapsed(!sidebarCollapsed.value)
  }

  async function loadSettings(): Promise<void> {
    try {
      settings.value = await publicApi.settings()
      const stored = getStoredLocale()
      if (!stored && isSupportedLocale(settings.value.defaultLocale)) {
        setLocale(settings.value.defaultLocale)
      }
    } catch {
      settings.value = null
    } finally {
      settingsLoaded.value = true
    }
  }

  return {
    locale,
    theme,
    sidebarCollapsed,
    settings,
    settingsLoaded,
    siteName,
    version,
    captchaEnabled,
    registrationEnabled,
    setLocale,
    setTheme,
    toggleTheme,
    setSidebarCollapsed,
    toggleSidebar,
    loadSettings,
  }
})
