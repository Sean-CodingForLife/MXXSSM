<template>
  <el-tag :type="tagType" effect="light" round size="small">{{ label }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  status?: string | null
}>()

const { t, te } = useI18n()

const label = computed(() => {
  if (!props.status) {
    return '-'
  }
  const key = `status.${props.status}`
  return te(key) ? t(key) : props.status
})

const tagType = computed(() => {
  switch (props.status) {
    case 'ACTIVE':
    case 'SUCCESS':
      return 'success'
    case 'FAIL':
      return 'danger'
    case 'DISABLED':
      return 'info'
    default:
      return 'info'
  }
})
</script>
