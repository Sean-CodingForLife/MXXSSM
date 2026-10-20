<template>
  <div class="page">
    <PageHeader :title="t('settings.title')" :subtitle="t('settings.subtitle')" />

    <div class="settings-grid">
      <el-card class="settings-card" shadow="never">
        <template #header>
          <div class="card-heading">
            <strong>{{ t('settings.appearance') }}</strong>
            <span>{{ t('settings.themeHint') }}</span>
          </div>
        </template>

        <div class="setting-row">
          <div class="setting-copy">
            <strong>{{ t('settings.language') }}</strong>
            <span>{{ t('settings.languageHint') }}</span>
          </div>
          <el-radio-group :model-value="appStore.locale" @update:model-value="changeLocale">
            <el-radio-button value="zh-CN">中文</el-radio-button>
            <el-radio-button value="en-US">English</el-radio-button>
          </el-radio-group>
        </div>

        <div class="setting-row">
          <div class="setting-copy">
            <strong>{{ t('settings.theme') }}</strong>
            <span>{{ t('settings.themeHint') }}</span>
          </div>
          <el-radio-group :model-value="appStore.theme" @update:model-value="changeTheme">
            <el-radio-button value="light">
              <el-icon><Sunny /></el-icon>
              {{ t('settings.light') }}
            </el-radio-button>
            <el-radio-button value="dark">
              <el-icon><Moon /></el-icon>
              {{ t('settings.dark') }}
            </el-radio-button>
          </el-radio-group>
        </div>

        <div class="setting-row">
          <div class="setting-copy">
            <strong>{{ t('settings.sidebar') }}</strong>
            <span>{{ t('settings.sidebarHint') }}</span>
          </div>
          <el-switch
            :model-value="!appStore.sidebarCollapsed"
            inline-prompt
            :active-text="t('settings.expand')"
            :inactive-text="t('settings.collapse')"
            @update:model-value="appStore.setSidebarCollapsed(!$event)"
          />
        </div>
      </el-card>

      <el-card class="settings-card" shadow="never">
        <template #header>
          <div class="card-heading">
            <strong>{{ t('settings.session') }}</strong>
            <span>{{ t('settings.sessionHint') }}</span>
          </div>
        </template>

        <dl class="settings-list">
          <div>
            <dt>{{ t('auth.account') }}</dt>
            <dd>{{ auth.user?.username }}</dd>
          </div>
          <div>
            <dt>{{ t('profile.roles') }}</dt>
            <dd><RoleTags :roles="auth.user?.roles ?? []" /></dd>
          </div>
          <div>
            <dt>{{ t('profile.lastLogin') }}</dt>
            <dd>{{ formatDateTime(auth.user?.lastLoginAt) }}</dd>
          </div>
          <div>
            <dt>{{ t('settings.sessionTimeout') }}</dt>
            <dd>{{ t('settings.sessionTimeoutValue') }}</dd>
          </div>
        </dl>
      </el-card>

      <el-card class="settings-card" shadow="never">
        <template #header>
          <div class="card-heading">
            <strong>{{ t('settings.about') }}</strong>
            <span>MXXSSM · Spring Boot 3 + Vue 3</span>
          </div>
        </template>

        <dl class="settings-list">
          <div>
            <dt>{{ t('settings.version') }}</dt>
            <dd>v{{ appStore.version }}</dd>
          </div>
          <div>
            <dt>{{ t('settings.documentation') }}</dt>
            <dd>
              <a
                class="link"
                href="https://github.com/Sean-CodingForLife/MXXSSM"
                target="_blank"
                rel="noreferrer"
              >
                README
              </a>
            </dd>
          </div>
          <div>
            <dt>{{ t('settings.apiDocs') }}</dt>
            <dd>
              <a class="link" href="/swagger-ui.html" target="_blank" rel="noreferrer"
                >Swagger UI</a
              >
            </dd>
          </div>
          <div>
            <dt>{{ t('settings.sourceCode') }}</dt>
            <dd>
              <a
                class="link"
                href="https://github.com/Sean-CodingForLife/MXXSSM"
                target="_blank"
                rel="noreferrer"
              >
                GitHub
              </a>
            </dd>
          </div>
        </dl>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { Moon, Sunny } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import RoleTags from '@/components/RoleTags.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'
import type { AppLocale } from '@/locales'

const { t } = useI18n()
const appStore = useAppStore()
const auth = useAuthStore()

function changeLocale(value: string | number | boolean | undefined) {
  if (value === 'zh-CN' || value === 'en-US') {
    appStore.setLocale(value as AppLocale)
  }
}

function changeTheme(value: string | number | boolean | undefined) {
  if (value === 'light' || value === 'dark') {
    appStore.setTheme(value)
  }
}
</script>

<style scoped>
.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  align-items: start;
}

.settings-card {
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  background: var(--app-surface);
  box-shadow: var(--app-shadow-soft);
}

.settings-card:first-child {
  grid-column: 1 / -1;
}

.settings-card :deep(.el-card__header) {
  padding: 18px 22px;
  border-bottom: 1px solid var(--app-border);
}

.settings-card :deep(.el-card__body) {
  padding: 8px 22px 18px;
}

.card-heading strong {
  font-size: 15px;
}

.card-heading span {
  display: block;
  margin-top: 3px;
  color: var(--app-muted);
  font-size: 12px;
}

.setting-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px dashed var(--app-border);
}

.setting-row:last-child {
  border-bottom: none;
}

.setting-copy {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.setting-copy strong {
  font-size: 13px;
}

.setting-copy span {
  color: var(--app-muted);
  font-size: 12px;
}

.settings-list {
  display: grid;
  gap: 0;
  margin: 0;
  padding: 6px 0;
}

.settings-list > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px dashed var(--app-border);
}

.settings-list > div:last-child {
  border-bottom: none;
}

.settings-list dt {
  color: var(--app-muted);
  font-size: 12px;
}

.settings-list dd {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  text-align: right;
}

.link {
  color: var(--el-color-primary);
  font-weight: 600;
}

@media (max-width: 900px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }

  .settings-card:first-child {
    grid-column: auto;
  }
}
</style>
