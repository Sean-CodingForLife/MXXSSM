<template>
  <div class="role-tags">
    <el-tag
      v-for="role in roles"
      :key="role"
      :type="role === 'ADMIN' ? 'danger' : 'info'"
      effect="light"
      round
      size="small"
    >
      {{ roleLabel(role) }}
    </el-tag>
    <span v-if="!roles.length" class="cell-muted">-</span>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

defineProps<{
  roles: string[]
}>()

const { t, te } = useI18n()

function roleLabel(role: string): string {
  const key = `role.${role}`
  return te(key) ? t(key) : role
}
</script>

<style scoped>
.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>
