<template>
  <div class="auth-shell">
    <section class="auth-hero">
      <div class="hero-orb hero-orb--one" />
      <div class="hero-orb hero-orb--two" />

      <div class="hero-top">
        <AppLogo :size="42" />
        <div class="hero-brand">
          <strong>{{ appStore.siteName }}</strong>
          <span>Admin foundation</span>
        </div>
      </div>

      <div class="hero-copy">
        <p class="hero-eyebrow">Spring Boot 3 · Vue 3</p>
        <h1>{{ t('auth.heroTitle') }}</h1>
        <p class="hero-lead">{{ t('auth.heroSubtitle') }}</p>

        <ul class="hero-features">
          <li>
            <span class="feature-icon"
              ><el-icon><Lock /></el-icon
            ></span>
            <div>
              <strong>{{ t('auth.featureRbacTitle') }}</strong>
              <p>{{ t('auth.featureRbacText') }}</p>
            </div>
          </li>
          <li>
            <span class="feature-icon"
              ><el-icon><Document /></el-icon
            ></span>
            <div>
              <strong>{{ t('auth.featureAuditTitle') }}</strong>
              <p>{{ t('auth.featureAuditText') }}</p>
            </div>
          </li>
          <li>
            <span class="feature-icon"
              ><el-icon><Lightning /></el-icon
            ></span>
            <div>
              <strong>{{ t('auth.featureFastTitle') }}</strong>
              <p>{{ t('auth.featureFastText') }}</p>
            </div>
          </li>
        </ul>
      </div>

      <div class="hero-footer">
        <span>© {{ currentYear }} {{ appStore.siteName }}</span>
        <span>v{{ appStore.version }}</span>
      </div>
    </section>

    <section class="auth-panel">
      <div class="panel-toolbar">
        <el-dropdown @command="changeLocale">
          <el-button text class="toolbar-button">
            <el-icon><Connection /></el-icon>
            <span>{{ appStore.locale === 'zh-CN' ? '中文' : 'English' }}</span>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="zh-CN" :disabled="appStore.locale === 'zh-CN'">
                中文
              </el-dropdown-item>
              <el-dropdown-item command="en-US" :disabled="appStore.locale === 'en-US'">
                English
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-button text circle class="toolbar-button" @click="appStore.toggleTheme()">
          <el-icon><Moon v-if="appStore.theme === 'light'" /><Sunny v-else /></el-icon>
        </el-button>
      </div>

      <div class="auth-card">
        <div class="mobile-brand">
          <AppLogo :size="36" />
          <strong>{{ appStore.siteName }}</strong>
        </div>
        <header class="auth-card__header">
          <h2>{{ title }}</h2>
          <p v-if="subtitle">{{ subtitle }}</p>
        </header>
        <slot />
        <div v-if="$slots.footer" class="auth-card__footer">
          <slot name="footer" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { Document, Connection, Lightning, Lock, Moon, Sunny } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import AppLogo from '@/components/AppLogo.vue'
import { useAppStore } from '@/stores/app'
import type { AppLocale } from '@/locales'

defineProps<{
  title: string
  subtitle?: string
}>()

const { t } = useI18n()
const appStore = useAppStore()
const currentYear = new Date().getFullYear()

function changeLocale(locale: string | number | object) {
  appStore.setLocale(String(locale) as AppLocale)
}
</script>

<style scoped>
.auth-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(420px, 0.92fr);
  min-height: 100vh;
  background: var(--app-bg);
}

.auth-hero {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 100vh;
  padding: 40px 56px;
  overflow: hidden;
  color: #e2e8f0;
  background:
    linear-gradient(150deg, rgba(15, 23, 42, 0.96), rgba(30, 27, 75, 0.94)),
    url('/favicon.svg') center / 60% no-repeat;
}

.hero-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(18px);
  opacity: 0.55;
  pointer-events: none;
}

.hero-orb--one {
  top: -120px;
  right: -80px;
  width: 420px;
  height: 420px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.7), transparent 68%);
}

.hero-orb--two {
  bottom: -160px;
  left: -90px;
  width: 460px;
  height: 460px;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.5), transparent 68%);
}

.hero-top,
.hero-copy,
.hero-footer {
  position: relative;
  z-index: 1;
}

.hero-top {
  display: flex;
  align-items: center;
  gap: 14px;
}

.hero-brand {
  display: flex;
  flex-direction: column;
}

.hero-brand strong {
  color: #f8fafc;
  font-size: 17px;
  letter-spacing: -0.02em;
}

.hero-brand span {
  color: #94a3b8;
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.hero-copy {
  max-width: 560px;
  padding: 48px 0;
}

.hero-eyebrow {
  margin: 0 0 14px;
  color: #a5b4fc;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin: 0;
  font-size: clamp(34px, 4.1vw, 56px);
  font-weight: 800;
  line-height: 1.08;
  letter-spacing: -0.04em;
}

.hero-lead {
  margin: 22px 0 0;
  color: #cbd5e1;
  font-size: 15px;
  line-height: 1.75;
}

.hero-features {
  display: grid;
  gap: 18px;
  margin: 38px 0 0;
  padding: 0;
  list-style: none;
}

.hero-features li {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.feature-icon {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 11px;
  color: #a5b4fc;
  background: rgba(99, 102, 241, 0.12);
}

.hero-features strong {
  color: #f1f5f9;
  font-size: 14px;
}

.hero-features p {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 13px;
  line-height: 1.6;
}

.hero-footer {
  display: flex;
  justify-content: space-between;
  color: #64748b;
  font-size: 11px;
}

.auth-panel {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: 26px 34px 34px;
}

.panel-toolbar {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.toolbar-button {
  color: var(--app-muted);
}

.auth-card {
  width: min(440px, 100%);
  margin: auto;
  padding: 34px;
  border: 1px solid var(--app-border);
  border-radius: 22px;
  background: var(--app-surface);
  box-shadow: var(--app-shadow);
}

.mobile-brand {
  display: none;
  align-items: center;
  gap: 10px;
  margin-bottom: 22px;
}

.mobile-brand strong {
  font-size: 17px;
}

.auth-card__header h2 {
  margin: 0;
  font-size: 27px;
  font-weight: 780;
  letter-spacing: -0.03em;
}

.auth-card__header p {
  margin: 8px 0 0;
  color: var(--app-muted);
  font-size: 13px;
}

.auth-card__footer {
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid var(--app-border);
  color: var(--app-muted);
  font-size: 13px;
  text-align: center;
}

@media (max-width: 980px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .auth-hero {
    display: none;
  }

  .auth-panel {
    padding: 18px;
  }

  .mobile-brand {
    display: flex;
  }

  .auth-card {
    margin: auto;
    padding: 26px 22px;
  }
}
</style>
