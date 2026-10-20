<template>
  <div class="page">
    <PageHeader :title="t('dashboard.title')" :subtitle="t('dashboard.subtitle')">
      <template #actions>
        <el-button v-if="canViewMetrics" :icon="Refresh" :loading="loading" @click="load">
          {{ t('common.refresh') }}
        </el-button>
      </template>
    </PageHeader>

    <section class="welcome-banner">
      <div class="welcome-copy">
        <p class="welcome-eyebrow">{{ t('dashboard.systemStatus') }}</p>
        <h2>{{ t('dashboard.welcome', { name: authStore.displayName }) }}</h2>
        <p>{{ t('dashboard.welcomeHint') }}</p>
        <div class="welcome-badges">
          <el-tag round effect="dark" type="success">
            <span class="status-dot" /> {{ t('dashboard.healthy') }}
          </el-tag>
          <el-tag round effect="plain">{{ roleLabel }}</el-tag>
          <el-tag round effect="plain">v{{ appStore.version }}</el-tag>
        </div>
      </div>
      <div class="welcome-rings">
        <div class="ring ring--one" />
        <div class="ring ring--two" />
        <div class="ring ring--three" />
      </div>
    </section>

    <section v-if="canViewMetrics" class="stats-grid">
      <StatCard
        :label="t('dashboard.totalUsers')"
        :value="formatNumber(summary?.userCount ?? 0)"
        icon="User"
        tone="primary"
      />
      <StatCard
        :label="t('dashboard.activeUsers')"
        :value="formatNumber(summary?.activeUserCount ?? 0)"
        icon="CircleCheck"
        tone="success"
      />
      <StatCard
        :label="t('dashboard.roles')"
        :value="formatNumber(summary?.roleCount ?? 0)"
        icon="UserFilled"
        tone="info"
      />
      <StatCard
        :label="t('dashboard.permissions')"
        :value="formatNumber(summary?.permissionCount ?? 0)"
        icon="Key"
        tone="warning"
      />
      <StatCard
        :label="t('dashboard.todayLogins')"
        :value="formatNumber(summary?.todayLogins ?? 0)"
        icon="Clock"
        tone="primary"
      />
      <StatCard
        :label="t('dashboard.failedLogins')"
        :value="formatNumber(summary?.todayFailedLogins ?? 0)"
        icon="Warning"
        tone="danger"
      />
      <StatCard
        :label="t('dashboard.todayOperations')"
        :value="formatNumber(summary?.todayOperations ?? 0)"
        icon="Tickets"
        tone="success"
      />
      <StatCard
        :label="t('dashboard.menus')"
        :value="formatNumber(summary?.menuCount ?? 0)"
        icon="Menu"
        tone="info"
      />
    </section>

    <section v-if="canViewMetrics" class="dashboard-grid">
      <el-card class="panel-card" shadow="never">
        <template #header>
          <div class="panel-header">
            <div>
              <strong>{{ t('dashboard.recentLogins') }}</strong>
              <span>{{ t('dashboard.activity') }}</span>
            </div>
            <router-link class="panel-link" to="/system/login-logs">
              {{ t('dashboard.viewAll') }}
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </template>

        <el-table v-loading="loading" :data="summary?.recentLogins ?? []" class="recent-table">
          <el-table-column prop="username" :label="t('logs.account')" min-width="120">
            <template #default="{ row }">
              <span class="user-cell">
                <span class="mini-avatar">{{
                  (row.username || '?').slice(0, 1).toUpperCase()
                }}</span>
                {{ row.username || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column :label="t('logs.status')" width="110">
            <template #default="{ row }">
              <StatusTag :status="row.status" />
            </template>
          </el-table-column>
          <el-table-column prop="ip" :label="t('logs.ip')" min-width="130" />
          <el-table-column :label="t('common.createdAt')" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
          <template #empty>
            <div class="empty-hint">{{ t('dashboard.noRecentLogins') }}</div>
          </template>
        </el-table>
      </el-card>

      <div class="side-stack">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <strong>{{ t('dashboard.quickActions') }}</strong>
                <span>{{ t('dashboard.activityHint') }}</span>
              </div>
            </div>
          </template>
          <div class="quick-grid">
            <button
              v-for="item in quickActions"
              :key="item.path"
              class="quick-action"
              type="button"
              @click="router.push(item.path)"
            >
              <span class="quick-icon"><AppIcon :name="item.icon" /></span>
              <span>{{ menuLabel(item.code, item.name) }}</span>
              <el-icon class="quick-arrow"><ArrowRight /></el-icon>
            </button>
            <div v-if="!quickActions.length" class="empty-hint">{{ t('common.noData') }}</div>
          </div>
        </el-card>

        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <strong>{{ t('dashboard.systemStatus') }}</strong>
                <span>{{
                  t('dashboard.generatedAt', { time: formatDateTime(summary?.generatedAt) })
                }}</span>
              </div>
            </div>
          </template>
          <ul class="status-list">
            <li>
              <span>{{ t('settings.version') }}</span>
              <strong>v{{ appStore.version }}</strong>
            </li>
            <li>
              <span>{{ t('settings.session') }}</span>
              <strong>{{ t('dashboard.healthy') }}</strong>
            </li>
            <li>
              <span>{{ t('dashboard.activeUsers') }}</span>
              <strong>{{ formatNumber(summary?.activeUserCount ?? 0) }}</strong>
            </li>
            <li>
              <span>{{ t('dashboard.failedLogins') }}</span>
              <strong class="danger-text">{{
                formatNumber(summary?.todayFailedLogins ?? 0)
              }}</strong>
            </li>
          </ul>
        </el-card>
      </div>
    </section>

    <el-card v-else class="panel-card" shadow="never">
      <template #header>
        <div class="panel-header">
          <div>
            <strong>{{ t('dashboard.personalActions') }}</strong>
            <span>{{ t('dashboard.personalHint') }}</span>
          </div>
        </div>
      </template>
      <div class="quick-grid">
        <button
          v-for="item in personalActions"
          :key="item.path"
          class="quick-action"
          type="button"
          @click="router.push(item.path)"
        >
          <span class="quick-icon"><AppIcon :name="item.icon" /></span>
          <span>{{ menuLabel(item.code, item.name) }}</span>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </button>
        <div v-if="!personalActions.length" class="empty-hint">{{ t('common.noData') }}</div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowRight, Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import AppIcon from '@/components/AppIcon.vue'
import { dashboardApi } from '@/api/dashboard'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime, formatNumber } from '@/utils/format'
import type { DashboardSummary } from '@/types/dashboard'

const { t, te } = useI18n()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

const summary = ref<DashboardSummary | null>(null)
const loading = ref(false)
const canViewMetrics = computed(() => authStore.hasPermission('admin:dashboard:read'))
const personalActions = computed(() => authStore.menus)

const quickActions = computed(() =>
  authStore.menus
    .filter((menu) => menu.path !== '/profile' && menu.path !== '/settings')
    .slice(0, 5),
)

const roleLabel = computed(() => {
  const roles = [...authStore.roles]
  return roles.map((role) => (te(`role.${role}`) ? t(`role.${role}`) : role)).join(' · ')
})

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

async function load(): Promise<void> {
  loading.value = true
  try {
    summary.value = await dashboardApi.summary()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (canViewMetrics.value) {
    void load()
  }
})
</script>

<style scoped>
.welcome-banner {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 190px;
  padding: 30px 34px;
  overflow: hidden;
  border-radius: 22px;
  color: #e2e8f0;
  background:
    radial-gradient(circle at 82% 18%, rgba(14, 165, 233, 0.5), transparent 34%),
    linear-gradient(135deg, #111a2e 0%, #1e1b4b 58%, #312e81 100%);
  box-shadow: 0 20px 50px rgba(30, 27, 75, 0.28);
}

.welcome-copy {
  position: relative;
  z-index: 2;
  max-width: 620px;
}

.welcome-eyebrow {
  margin: 0 0 10px;
  color: #a5b4fc;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.welcome-copy h2 {
  margin: 0;
  color: #f8fafc;
  font-size: clamp(24px, 2.6vw, 34px);
  font-weight: 780;
  letter-spacing: -0.03em;
}

.welcome-copy p {
  margin: 10px 0 0;
  color: #cbd5e1;
  font-size: 13px;
}

.welcome-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 18px;
}

.welcome-badges :deep(.el-tag) {
  border-color: rgba(148, 163, 184, 0.3);
  color: #e2e8f0;
  background: rgba(15, 23, 42, 0.4);
}

.welcome-badges :deep(.el-tag--success) {
  color: #bbf7d0;
  background: rgba(22, 163, 74, 0.25);
  border-color: rgba(34, 197, 94, 0.4);
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 4px;
  border-radius: 999px;
  background: #22c55e;
  vertical-align: middle;
}

.welcome-rings {
  position: absolute;
  top: 50%;
  right: 6%;
  width: 280px;
  height: 280px;
  transform: translateY(-50%);
}

.ring {
  position: absolute;
  border: 1px solid rgba(165, 180, 252, 0.22);
  border-radius: 999px;
}

.ring--one {
  inset: 0;
}

.ring--two {
  inset: 40px;
  border-color: rgba(56, 189, 248, 0.24);
}

.ring--three {
  inset: 82px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.38), transparent 70%);
  border-color: transparent;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(300px, 0.85fr);
  gap: 18px;
}

.side-stack {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.panel-card {
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  background: var(--app-surface);
  box-shadow: var(--app-shadow-soft);
}

.panel-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--app-border);
}

.panel-card :deep(.el-card__body) {
  padding: 0;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-header > div {
  display: flex;
  flex-direction: column;
}

.panel-header strong {
  font-size: 14px;
}

.panel-header span {
  color: var(--app-muted);
  font-size: 11px;
}

.panel-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--el-color-primary);
  font-size: 12px;
  font-weight: 600;
}

.recent-table {
  --el-table-border-color: var(--app-border);
}

.recent-table :deep(.el-table__header th) {
  background: color-mix(in srgb, var(--app-bg) 70%, transparent);
  color: var(--app-muted);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.03em;
  text-transform: uppercase;
}

.user-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.mini-avatar {
  display: grid;
  place-items: center;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  color: #fff;
  font-size: 11px;
  background: linear-gradient(135deg, #6366f1, #0ea5e9);
}

.quick-grid {
  display: grid;
  gap: 8px;
  padding: 14px;
}

.quick-action {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 11px 12px;
  border: 1px solid transparent;
  border-radius: 12px;
  color: var(--app-text);
  font-size: 13px;
  font-weight: 600;
  text-align: left;
  background: transparent;
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    background-color 0.18s ease;
}

.quick-action:hover {
  border-color: var(--app-border);
  background: var(--app-primary-soft);
}

.quick-icon {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  color: var(--el-color-primary);
  background: var(--app-primary-soft);
}

.quick-arrow {
  margin-left: auto;
  color: var(--app-muted);
}

.status-list {
  display: grid;
  gap: 0;
  margin: 0;
  padding: 6px 20px 14px;
  list-style: none;
}

.status-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 0;
  border-bottom: 1px dashed var(--app-border);
  font-size: 13px;
}

.status-list li:last-child {
  border-bottom: none;
}

.status-list span {
  color: var(--app-muted);
}

.danger-text {
  color: var(--el-color-danger);
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .welcome-banner {
    padding: 24px;
  }

  .welcome-rings {
    display: none;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
