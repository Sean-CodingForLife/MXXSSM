import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/styles/index.scss'

import App from './App.vue'
import { router } from './router'
import { i18n } from './locales'
import { useAppStore } from './stores/app'
import { useAuthStore } from './stores/auth'
import { ensureCsrfToken } from './api/http'

async function bootstrap(): Promise<void> {
  const app = createApp(App)
  const pinia = createPinia()
  app.use(pinia)
  app.use(i18n)
  app.use(ElementPlus)

  const appStore = useAppStore(pinia)
  const authStore = useAuthStore(pinia)

  await appStore.loadSettings()
  await ensureCsrfToken().catch(() => undefined)
  await authStore.fetchMe(true)

  window.addEventListener('auth:unauthorized', () => {
    const current = router.currentRoute.value
    authStore.reset()
    if (current.name !== 'login') {
      void router.push({ name: 'login', query: { redirect: current.fullPath } })
    }
  })

  app.use(router)
  app.config.errorHandler = (error) => {
    console.error('Unhandled application error', error)
  }

  await router.isReady()
  app.mount('#app')
}

void bootstrap()
