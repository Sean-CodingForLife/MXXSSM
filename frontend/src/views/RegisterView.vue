<template>
  <AuthLayout :title="t('auth.signUpTitle')" :subtitle="t('auth.signUpSubtitle')">
    <el-alert
      v-if="!appStore.registrationEnabled"
      :title="t('auth.registrationDisabled')"
      type="warning"
      show-icon
      :closable="false"
    />

    <el-form
      v-else
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      size="large"
      @submit.prevent="submit"
    >
      <div class="form-grid">
        <el-form-item :label="t('auth.account')" prop="username">
          <el-input
            v-model="form.username"
            :prefix-icon="User"
            autocomplete="username"
            :placeholder="t('users.usernameHint')"
          />
        </el-form-item>

        <el-form-item :label="t('auth.displayName')" prop="displayName">
          <el-input v-model="form.displayName" :prefix-icon="Avatar" />
        </el-form-item>
      </div>

      <div class="form-grid">
        <el-form-item :label="t('auth.password')" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :prefix-icon="Lock"
            autocomplete="new-password"
          />
        </el-form-item>

        <el-form-item :label="t('auth.repeatPassword')" prop="repeatPassword">
          <el-input
            v-model="form.repeatPassword"
            type="password"
            show-password
            :prefix-icon="Lock"
            autocomplete="new-password"
          />
        </el-form-item>
      </div>

      <div class="form-grid">
        <el-form-item :label="`${t('auth.email')} (${t('common.optional')})`" prop="email">
          <el-input
            v-model="form.email"
            :prefix-icon="Message"
            autocomplete="email"
            :placeholder="t('users.emailPlaceholder')"
          />
        </el-form-item>

        <el-form-item :label="`${t('auth.phone')} (${t('common.optional')})`" prop="phone">
          <el-input v-model="form.phone" :prefix-icon="Iphone" />
        </el-form-item>
      </div>

      <p class="password-rule">{{ t('auth.passwordRule') }}</p>

      <el-alert
        v-if="errorMessage"
        class="register-error"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="false"
      />

      <el-button class="submit-button" type="primary" native-type="submit" :loading="loading">
        {{ t('auth.registerButton') }}
      </el-button>
    </el-form>

    <template #footer>
      <span>{{ t('auth.hasAccount') }}</span>
      <router-link class="footer-link" to="/login">{{ t('auth.backToLogin') }}</router-link>
    </template>
  </AuthLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Avatar, Iphone, Lock, Message, User } from '@element-plus/icons-vue'
import AuthLayout from '@/layouts/AuthLayout.vue'
import { authApi } from '@/api/auth'
import { ApiError } from '@/api/http'
import { useAppStore } from '@/stores/app'

interface RegisterForm {
  username: string
  displayName: string
  password: string
  repeatPassword: string
  email: string
  phone: string
}

const router = useRouter()
const { t } = useI18n()
const appStore = useAppStore()

const formRef = ref<FormInstance>()
const form = reactive<RegisterForm>({
  username: '',
  displayName: '',
  password: '',
  repeatPassword: '',
  email: '',
  phone: '',
})
const loading = ref(false)
const errorMessage = ref('')

const rules: FormRules<RegisterForm> = {
  username: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    {
      pattern: /^[A-Za-z0-9._-]{3,32}$/,
      message: t('validation.username'),
      trigger: 'blur',
    },
  ],
  displayName: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    { min: 2, max: 64, message: t('validation.displayName'), trigger: 'blur' },
  ],
  password: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,64}$/,
      message: t('validation.password'),
      trigger: 'blur',
    },
  ],
  repeatPassword: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error(t('auth.passwordMismatch')))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  email: [{ type: 'email', message: t('validation.email'), trigger: 'blur' }],
  phone: [
    {
      pattern: /^$|^[0-9+()\- ]{5,32}$/,
      message: t('validation.phone'),
      trigger: 'blur',
    },
  ],
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
    await authApi.register({
      username: form.username.trim(),
      displayName: form.displayName.trim(),
      password: form.password,
      email: form.email.trim() || undefined,
      phone: form.phone.trim() || undefined,
    })
    ElMessage.success(t('auth.registerSuccess'))
    await router.push('/login')
  } catch (error) {
    errorMessage.value = error instanceof ApiError ? error.message : t('common.requestFailed')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 14px;
}

.password-rule {
  margin: -6px 0 14px;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.6;
}

.register-error {
  margin-bottom: 16px;
}

.submit-button {
  width: 100%;
  height: 44px;
  margin-top: 6px;
  border: none;
  font-weight: 650;
  background: linear-gradient(135deg, #4f46e5, #0ea5e9);
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.24);
}

.footer-link {
  margin-left: 6px;
  color: var(--el-color-primary);
  font-weight: 650;
}

@media (max-width: 620px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
