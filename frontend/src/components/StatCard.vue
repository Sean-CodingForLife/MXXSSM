<template>
  <div class="stat-card" :class="`stat-card--${tone}`">
    <div class="stat-card__icon">
      <AppIcon :name="icon" />
    </div>
    <div class="stat-card__body">
      <span class="stat-card__label">{{ label }}</span>
      <strong class="stat-card__value">{{ value }}</strong>
      <span v-if="hint" class="stat-card__hint">{{ hint }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import AppIcon from './AppIcon.vue'

withDefaults(
  defineProps<{
    label: string
    value: string | number
    icon?: string
    hint?: string
    tone?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  }>(),
  {
    icon: 'Grid',
    tone: 'primary',
  },
)
</script>

<style scoped>
.stat-card {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 20px;
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  background: var(--app-surface);
  box-shadow: var(--app-shadow-soft);
}

.stat-card__icon {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  border-radius: 14px;
  font-size: 22px;
  color: #fff;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
}

.stat-card--success .stat-card__icon {
  background: linear-gradient(135deg, #34d399, #059669);
}

.stat-card--warning .stat-card__icon {
  background: linear-gradient(135deg, #fbbf24, #d97706);
}

.stat-card--danger .stat-card__icon {
  background: linear-gradient(135deg, #fb7185, #dc2626);
}

.stat-card--info .stat-card__icon {
  background: linear-gradient(135deg, #38bdf8, #0284c7);
}

.stat-card__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.stat-card__label {
  color: var(--app-muted);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.stat-card__value {
  font-size: 26px;
  font-weight: 750;
  letter-spacing: -0.03em;
}

.stat-card__hint {
  color: var(--app-muted);
  font-size: 12px;
}
</style>
