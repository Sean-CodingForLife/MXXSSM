<template>
  <div
    class="app-logo"
    :style="{ width: `${size}px`, height: `${size}px`, borderRadius: `${size / 4}px` }"
  >
    <span :style="{ fontSize: `${size * 0.46}px` }">M</span>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    size?: number
  }>(),
  { size: 40 },
)
</script>

<style scoped>
.app-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  background: linear-gradient(135deg, #6366f1 0%, #0ea5e9 100%);
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.35);
}

.app-logo span {
  color: #fff;
  font-weight: 800;
  letter-spacing: -0.04em;
  line-height: 1;
}
</style>
