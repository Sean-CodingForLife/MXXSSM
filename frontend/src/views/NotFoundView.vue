<template>
  <div class="error-page">
    <div class="error-card">
      <span class="error-code">404</span>
      <div class="error-icon">
        <el-icon><Compass /></el-icon>
      </div>
      <h1>{{ t('errors.notFoundTitle') }}</h1>
      <p>{{ t('errors.notFoundText') }}</p>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">{{ t('errors.backHome') }}</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Compass } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const { t } = useI18n()
const auth = useAuthStore()

function goHome() {
  void router.push(auth.isAuthenticated ? '/dashboard' : '/login')
}
</script>

<style scoped>
.error-page {
  display: grid;
  place-items: center;
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at 20% 20%, rgba(99, 102, 241, 0.12), transparent 30%),
    radial-gradient(circle at 80% 80%, rgba(14, 165, 233, 0.12), transparent 30%), var(--app-bg);
}

.error-card {
  position: relative;
  width: min(520px, 100%);
  padding: 48px 40px;
  overflow: hidden;
  border: 1px solid var(--app-border);
  border-radius: 24px;
  background: var(--app-surface);
  box-shadow: var(--app-shadow);
  text-align: center;
}

.error-code {
  position: absolute;
  top: -12px;
  right: 18px;
  color: color-mix(in srgb, var(--app-primary) 10%, transparent);
  font-size: 120px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.06em;
}

.error-icon {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  margin: 0 auto 18px;
  border-radius: 18px;
  color: #fff;
  font-size: 28px;
  background: linear-gradient(135deg, #6366f1, #0ea5e9);
}

.error-card h1 {
  position: relative;
  margin: 0;
  font-size: 25px;
  font-weight: 780;
  letter-spacing: -0.03em;
}

.error-card p {
  position: relative;
  margin: 12px 0 0;
  color: var(--app-muted);
  font-size: 14px;
  line-height: 1.7;
}

.error-actions {
  position: relative;
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 26px;
}
</style>
