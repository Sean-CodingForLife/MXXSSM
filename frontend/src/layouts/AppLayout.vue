<template>
  <div class="app-shell">
    <aside v-if="!isMobile" class="app-sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
      <SidebarNav :collapsed="appStore.sidebarCollapsed" />
    </aside>

    <el-drawer
      append-to-body
      v-model="menuOpen"
      direction="ltr"
      size="268px"
      :with-header="false"
      class="mobile-drawer"
    >
      <SidebarNav @navigate="menuOpen = false" />
    </el-drawer>

    <div class="app-main">
      <header class="app-header">
        <div class="app-header__left">
          <el-button
            v-if="isMobile"
            text
            circle
            class="header-icon-button"
            @click="menuOpen = true"
          >
            <el-icon><Expand /></el-icon>
          </el-button>
          <el-button
            v-else
            text
            circle
            class="header-icon-button"
            @click="appStore.toggleSidebar()"
          >
            <el-icon><Fold v-if="!appStore.sidebarCollapsed" /><Expand v-else /></el-icon>
          </el-button>

          <div class="breadcrumb">
            <span class="breadcrumb__root">{{ t('common.appName') }}</span>
            <span class="breadcrumb__sep">/</span>
            <strong>{{ pageTitle }}</strong>
          </div>
        </div>

        <div class="app-header__right">
          <el-dropdown @command="changeLocale">
            <el-button
              text
              circle
              class="header-icon-button"
              :title="t('settings.language')"
              :aria-label="t('settings.language')"
            >
              <el-icon><Connection /></el-icon>
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

          <el-button
            text
            circle
            class="header-icon-button"
            :title="t('settings.theme')"
            :aria-label="t('settings.theme')"
            @click="appStore.toggleTheme()"
          >
            <el-icon><Moon v-if="appStore.theme === 'light'" /><Sunny v-else /></el-icon>
          </el-button>

          <el-dropdown trigger="click" @command="handleUserCommand">
            <button class="user-chip" type="button">
              <span class="user-avatar">{{ authStore.initials }}</span>
              <span class="user-meta">
                <strong>{{ authStore.displayName }}</strong>
                <small>{{ roleLabel }}</small>
              </span>
              <el-icon class="user-caret"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  {{ t('nav.profile') }}
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  {{ t('nav.settings') }}
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  {{ t('auth.logout') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="app-content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  Expand,
  Fold,
  Connection,
  Moon,
  Setting,
  Sunny,
  SwitchButton,
  User,
} from '@element-plus/icons-vue'
import SidebarNav from '@/components/layout/SidebarNav.vue'
import { useResponsive } from '@/composables/useResponsive'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import type { AppLocale } from '@/locales'

const route = useRoute()
const router = useRouter()
const { t, te } = useI18n()
const appStore = useAppStore()
const authStore = useAuthStore()
const { isMobile } = useResponsive(900)
const menuOpen = ref(false)

const pageTitle = computed(() => {
  const key = route.meta.titleKey
  return key && te(key) ? t(key) : t('nav.dashboard')
})

const roleLabel = computed(() => {
  const roles = [...authStore.roles]
  if (!roles.length) {
    return ''
  }
  return roles.map((role) => (te(`role.${role}`) ? t(`role.${role}`) : role)).join(' · ')
})

watch(
  () => route.fullPath,
  () => {
    menuOpen.value = false
  },
)

watch(isMobile, (mobile) => {
  if (!mobile) {
    menuOpen.value = false
  }
})

function changeLocale(locale: string | number | object) {
  appStore.setLocale(String(locale) as AppLocale)
}

async function handleUserCommand(command: string | number | object) {
  const value = String(command)
  if (value === 'profile') {
    await router.push('/profile')
    return
  }
  if (value === 'settings') {
    await router.push('/settings')
    return
  }
  if (value === 'logout') {
    await authStore.logout()
    ElMessage.success(t('auth.logoutSuccess'))
    await router.push('/login')
  }
}
</script>

<style scoped>
.app-shell {
  display: flex;
  min-height: 100vh;
  background: var(--app-bg);
}

.app-sidebar {
  position: sticky;
  top: 0;
  flex: 0 0 256px;
  width: 256px;
  height: 100vh;
  transition:
    width 0.2s ease,
    flex-basis 0.2s ease;
}

.app-sidebar.collapsed {
  flex-basis: 76px;
  width: 76px;
}

.app-main {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: 68px;
  padding: 0 26px;
  border-bottom: 1px solid var(--app-border);
  background: color-mix(in srgb, var(--app-surface) 86%, transparent);
  backdrop-filter: blur(16px);
}

.app-header__left,
.app-header__right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon-button {
  color: var(--app-muted);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  font-size: 13px;
}

.breadcrumb__root {
  color: var(--app-muted);
}

.breadcrumb__sep {
  color: var(--app-border);
}

.breadcrumb strong {
  overflow: hidden;
  color: var(--app-text);
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px 6px 6px;
  border: 1px solid transparent;
  border-radius: 12px;
  color: inherit;
  background: transparent;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease;
}

.user-chip:hover {
  border-color: var(--app-border);
  background: var(--app-surface-strong);
}

.user-avatar {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 11px;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  background: linear-gradient(135deg, #6366f1, #0ea5e9);
}

.user-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.25;
}

.user-meta strong {
  max-width: 150px;
  overflow: hidden;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-meta small {
  color: var(--app-muted);
  font-size: 11px;
}

.user-caret {
  color: var(--app-muted);
  font-size: 12px;
}

.app-content {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  padding: 26px 28px 44px;
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition:
    opacity 0.18s ease,
    transform 0.18s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

:global(.mobile-drawer .el-drawer__body) {
  padding: 0;
}

@media (max-width: 900px) {
  .app-header {
    padding: 0 16px;
  }

  .app-content {
    padding: 18px 16px 32px;
  }

  .breadcrumb__root,
  .breadcrumb__sep {
    display: none;
  }

  .user-meta {
    display: none;
  }

  .user-caret {
    display: none;
  }
}
</style>
