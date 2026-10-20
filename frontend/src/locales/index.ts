import { createI18n } from 'vue-i18n'
import enUS from './en-US'
import zhCN from './zh-CN'

export const SUPPORTED_LOCALES = ['zh-CN', 'en-US'] as const
export type AppLocale = (typeof SUPPORTED_LOCALES)[number]

const STORAGE_KEY = 'mxxssm.locale'

export function isSupportedLocale(value: string | null | undefined): value is AppLocale {
  return value === 'zh-CN' || value === 'en-US'
}

function getStorage(): Storage | null {
  if (typeof window === 'undefined') {
    return null
  }
  try {
    return window.localStorage ?? null
  } catch {
    return null
  }
}

function detectInitialLocale(): AppLocale {
  const storage = getStorage()
  const stored = storage?.getItem(STORAGE_KEY)
  if (isSupportedLocale(stored)) {
    return stored
  }
  if (typeof window === 'undefined') {
    return 'zh-CN'
  }
  const browser = window.navigator?.language ?? ''
  return browser.toLowerCase().startsWith('zh') ? 'zh-CN' : 'en-US'
}

export const i18n = createI18n({
  legacy: false,
  locale: detectInitialLocale(),
  fallbackLocale: 'en-US',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
  },
  missingWarn: false,
  fallbackWarn: false,
})

export function getStoredLocale(): AppLocale | null {
  const stored = getStorage()?.getItem(STORAGE_KEY)
  return isSupportedLocale(stored) ? stored : null
}

export function persistLocale(locale: AppLocale): void {
  getStorage()?.setItem(STORAGE_KEY, locale)
}
