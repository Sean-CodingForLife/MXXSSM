import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { i18n } from '@/locales'
import { useAuthStore } from '@/stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    permission?: string
    titleKey?: string
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, titleKey: 'auth.login' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { public: true, titleKey: 'auth.signup' },
  },
  {
    path: '/',
    component: () => import('@/layouts/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { titleKey: 'nav.dashboard' },
      },
      {
        path: 'system/users',
        name: 'users',
        component: () => import('@/views/system/UsersView.vue'),
        meta: { titleKey: 'nav.users', permission: 'admin:user:read' },
      },
      {
        path: 'system/roles',
        name: 'roles',
        component: () => import('@/views/system/RolesView.vue'),
        meta: { titleKey: 'nav.roles', permission: 'admin:role:read' },
      },
      {
        path: 'system/menus',
        name: 'menus',
        component: () => import('@/views/system/MenusView.vue'),
        meta: { titleKey: 'nav.menus', permission: 'admin:menu:read' },
      },
      {
        path: 'system/permissions',
        name: 'permissions',
        component: () => import('@/views/system/PermissionsView.vue'),
        meta: { titleKey: 'nav.permissions', permission: 'admin:permission:read' },
      },
      {
        path: 'system/login-logs',
        name: 'login-logs',
        component: () => import('@/views/system/LoginLogsView.vue'),
        meta: { titleKey: 'nav.loginLogs', permission: 'admin:log:login:read' },
      },
      {
        path: 'system/operation-logs',
        name: 'operation-logs',
        component: () => import('@/views/system/OperationLogsView.vue'),
        meta: { titleKey: 'nav.operationLogs', permission: 'admin:log:operation:read' },
      },
      {
        path: 'system/configs',
        name: 'configs',
        component: () => import('@/views/system/ConfigsView.vue'),
        meta: { titleKey: 'nav.configs', permission: 'admin:config:read' },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { titleKey: 'nav.profile', permission: 'user:profile:read' },
      },
      {
        path: 'settings',
        name: 'settings',
        component: () => import('@/views/settings/SettingsView.vue'),
        meta: { titleKey: 'nav.settings', permission: 'user:settings:read' },
      },
    ],
  },
  {
    path: '/403',
    name: 'forbidden',
    component: () => import('@/views/ForbiddenView.vue'),
    meta: { public: true, titleKey: 'errors.forbiddenTitle' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { titleKey: 'errors.notFoundTitle' },
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.initialized) {
    await auth.fetchMe(true)
  }

  if (to.meta.public) {
    if (auth.isAuthenticated && (to.name === 'login' || to.name === 'register')) {
      return { name: 'dashboard' }
    }
    return true
  }

  if (!auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.permission && !auth.hasPermission(to.meta.permission)) {
    return { name: 'forbidden' }
  }

  return true
})

router.afterEach((to) => {
  const title = to.meta.titleKey ? i18n.global.t(to.meta.titleKey) : i18n.global.t('common.appName')
  document.title = `${title} · MXXSSM`
})
