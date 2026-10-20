<template>
  <div class="sidebar-nav" :class="{ collapsed }">
    <div class="sidebar-brand">
      <AppLogo :size="collapsed ? 34 : 38" />
      <div v-if="!collapsed" class="brand-copy">
        <strong>{{ appStore.siteName }}</strong>
        <span>Admin console</span>
      </div>
    </div>

    <el-scrollbar class="sidebar-scroll">
      <el-menu
        :default-active="route.path"
        :collapse="collapsed"
        :collapse-transition="false"
        router
        @select="emit('navigate')"
      >
        <div v-if="!collapsed" class="menu-group-label">{{ t('groups.general') }}</div>
        <el-menu-item index="/dashboard">
          <el-icon><AppIcon name="Odometer" /></el-icon>
          <template #title>{{ t('nav.dashboard') }}</template>
        </el-menu-item>
        <div class="menu-group-divider" />

        <template v-for="group in groups" :key="group.key">
          <div v-if="!collapsed" class="menu-group-label">{{ groupLabel(group.key) }}</div>
          <div v-else class="menu-group-divider" />
          <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
            <el-icon><AppIcon :name="item.icon" /></el-icon>
            <template #title>{{ menuLabel(item.code, item.name) }}</template>
          </el-menu-item>
        </template>
      </el-menu>

      <div v-if="!authStore.menus.length" class="sidebar-empty">
        {{ t('common.noData') }}
      </div>
    </el-scrollbar>

    <div v-if="!collapsed" class="sidebar-footer">
      <span class="dot" />
      <span>v{{ appStore.version }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import AppIcon from '@/components/AppIcon.vue'
import AppLogo from '@/components/AppLogo.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { groupMenus } from '@/utils/navigation'

defineProps<{
  collapsed?: boolean
}>()

const emit = defineEmits<{
  navigate: []
}>()

const route = useRoute()
const appStore = useAppStore()
const authStore = useAuthStore()
const { t, te } = useI18n()

const groups = computed(() => groupMenus(authStore.menus))

const navKeys: Record<string, string> = {
  users: 'nav.users',
  roles: 'nav.roles',
  menus: 'nav.menus',
  permissions: 'nav.permissions',
  'login-logs': 'nav.loginLogs',
  'operation-logs': 'nav.operationLogs',
  configs: 'nav.configs',
  profile: 'nav.profile',
  settings: 'nav.settings',
}

function menuLabel(code: string, fallback: string): string {
  const key = navKeys[code]
  return key && te(key) ? t(key) : fallback
}

function groupLabel(group: string): string {
  const key = `groups.${group.toLowerCase()}`
  return te(key) ? t(key) : group
}
</script>

<style scoped>
.sidebar-nav {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background:
    radial-gradient(circle at 20% 0%, rgba(99, 102, 241, 0.22), transparent 34%), var(--app-sidebar);
  color: #cbd5e1;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 72px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.brand-copy strong {
  color: #f8fafc;
  font-size: 15px;
  letter-spacing: -0.01em;
}

.brand-copy span {
  color: #64748b;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.sidebar-scroll {
  flex: 1;
  min-height: 0;
  padding: 10px 10px 18px;
}

.sidebar-scroll :deep(.el-menu) {
  border-right: none;
  background: transparent;
}

.sidebar-scroll :deep(.el-menu-item) {
  height: 44px;
  margin: 3px 0;
  border-radius: 10px;
  color: #94a3b8;
  font-weight: 500;
}

.sidebar-scroll :deep(.el-menu-item:hover) {
  color: #e2e8f0;
  background: rgba(148, 163, 184, 0.1);
}

.sidebar-scroll :deep(.el-menu-item.is-active) {
  color: #fff;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.95), rgba(14, 165, 233, 0.9));
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.35);
}

.sidebar-scroll :deep(.el-menu--collapse .el-menu-item) {
  padding: 0 14px;
}

.menu-group-label {
  padding: 16px 12px 6px;
  color: #64748b;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.menu-group-divider {
  height: 1px;
  margin: 12px 10px;
  background: rgba(148, 163, 184, 0.16);
}

.sidebar-empty {
  padding: 16px;
  color: #64748b;
  font-size: 12px;
  text-align: center;
}

.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 22px 18px;
  border-top: 1px solid rgba(148, 163, 184, 0.12);
  color: #64748b;
  font-size: 11px;
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 999px;
  background: #22c55e;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.16);
}
</style>
