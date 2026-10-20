<template>
  <component :is="iconComponent" v-if="iconComponent" />
  <QuestionFilled v-else />
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue'
import {
  Avatar,
  CircleCheck,
  Clock,
  Document,
  Grid,
  Key,
  Menu as MenuIcon,
  Monitor,
  Odometer,
  QuestionFilled,
  Setting,
  Tickets,
  Tools,
  User,
  UserFilled,
  Warning,
} from '@element-plus/icons-vue'

const props = defineProps<{
  name?: string | null
}>()

const icons: Record<string, Component> = {
  Avatar,
  CircleCheck,
  Clock,
  Document,
  Grid,
  Key,
  Menu: MenuIcon,
  Monitor,
  Odometer,
  Setting,
  Tickets,
  Tools,
  User,
  UserFilled,
  Warning,
}

const iconComponent = computed(() => (props.name ? icons[props.name] : undefined))
</script>
