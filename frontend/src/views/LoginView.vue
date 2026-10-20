<template>
  <AuthLayout :title="t('auth.signInTitle')" :subtitle="t('auth.signInSubtitle')">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      size="large"
      @submit.prevent="submit"
    >
      <el-form-item :label="t('auth.account')" prop="username">
        <el-input
          v-model="form.username"
          :prefix-icon="User"
          autocomplete="username"
          :placeholder="t('auth.account')"
          @keyup.enter="submit"
        />
      </el-form-item>

      <el-form-item :label="t('auth.password')" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          :prefix-icon="Lock"
          autocomplete="current-password"
          :placeholder="t('auth.password')"
          @keyup.enter="submit"
        />
      </el-form-item>

      <el-form-item v-if="captchaEnabled" :label="t('auth.captcha')" prop="captcha">
        <div class="captcha-row">
          <el-input
            v-model="form.captcha"
            :prefix-icon="Key"
            autocomplete="off"
            :placeholder="t('auth.captchaPlaceholder')"
            @keyup.enter="submit"
          />
          <button
            class="captcha-image"
            type="button"
            :title="t('auth.refreshCaptcha')"
            @click="loadCaptcha"
          >
            <img v-if="captchaImage" :src="captchaImage" :alt="t('auth.captcha')" />
            <span v-else>{{ t('common.loading') }}</span>
          </button>
        </div>
      </el-form-item>

      <el-alert
        v-if="errorMessage"
        class="login-error"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="false"
      />

      <el-button class="submit-button" type="primary" native-type="submit" :loading="loading">
        {{ t('auth.loginButton') }}
      </el-button>

      <el-alert
        v-if="appStore.settings"
        class="demo-hint"
        :title="t('auth.demoHint')"
        type="info"
        :closable="false"
      />
    </el-form>

    <template #footer>
      <span>{{ t('auth.noAccount') }}</span>
      <router-link class="footer-link" to="/register">{{ t('auth.signup') }}</router-link>
    </template>
  </AuthLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Key, Lock, User } from '@element-plus/icons-vue'
import AuthLayout from '@/layouts/AuthLayout.vue'
import { authApi } from '@/api/auth'
import { ApiError, ensureCsrfToken } from '@/api/http'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'

interface LoginForm {
  username: string
  password: string
  captcha: string
}

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const appStore = useAppStore()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const form = reactive<LoginForm>({ username: '', password: '', captcha: '' })
const loading = ref(false)
const errorMessage = ref('')
const captchaEnabled = ref(appStore.captchaEnabled)
const captchaImage = ref<string | null>(null)

const rules = computed<FormRules<LoginForm>>(() => {
  const base: FormRules<LoginForm> = {
    username: [{ required: true, message: t('validation.required'), trigger: 'blur' }],
    password: [{ required: true, message: t('validation.required'), trigger: 'blur' }],
  }
  if (captchaEnabled.value) {
    base.captcha = [{ required: true, message: t('validation.required'), trigger: 'blur' }]
  }
  return base
})

async function loadCaptcha(): Promise<void> {
  if (!captchaEnabled.value) {
    return
  }
  try {
    const data = await authApi.captcha()
    captchaEnabled.value = data.enabled
    captchaImage.value = data.image
    form.captcha = ''
  } catch {
    captchaImage.value = null
  }
}

async function submit(): Promise<void> {
  if (loading.value) {
    return
  }
  errorMessage.value = ''
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  loading.value = true
  try {
    await authStore.login({
      username: form.username.trim(),
      password: form.password,
      captcha: captchaEnabled.value ? form.captcha : undefined,
    })
    ElMessage.success(t('auth.loginSuccess'))
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    await router.replace(redirect.startsWith('/') ? redirect : '/dashboard')
  } catch (error) {
    if (error instanceof ApiError) {
      errorMessage.value = error.message
    } else {
      errorMessage.value = t('common.requestFailed')
    }
    await loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await ensureCsrfToken().catch(() => undefined)
  await loadCaptcha()
})
</script>

<style scoped>
.login-error {
  margin-bottom: 16px;
}

.submit-button {
  width: 100%;
  height: 44px;
  margin-top: 4px;
  border: none;
  font-weight: 650;
  background: linear-gradient(135deg, #4f46e5, #0ea5e9);
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.24);
}

.demo-hint {
  margin-top: 16px;
}

.captcha-row {
  display: grid;
  grid-template-columns: 1fr 132px;
  gap: 10px;
  width: 100%;
}

.captcha-image {
  display: grid;
  place-items: center;
  height: 40px;
  padding: 0;
  overflow: hidden;
  border: 1px solid var(--app-border);
  border-radius: 10px;
  color: var(--app-muted);
  background: var(--app-surface-strong);
  cursor: pointer;
}

.captcha-image img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.footer-link {
  margin-left: 6px;
  color: var(--el-color-primary);
  font-weight: 650;
}
</style>
