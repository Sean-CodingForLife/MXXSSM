<template>
  <div class="page">
    <PageHeader :title="t('profile.title')" :subtitle="t('profile.subtitle')" />

    <div class="profile-grid">
      <el-card class="profile-card" shadow="never">
        <div class="profile-hero">
          <span class="profile-avatar">{{ auth.initials }}</span>
          <div>
            <h2>{{ auth.displayName }}</h2>
            <p>@{{ auth.user?.username }}</p>
          </div>
        </div>

        <div class="profile-roles">
          <RoleTags :roles="auth.user?.roles ?? []" />
        </div>

        <dl class="profile-meta">
          <div>
            <dt>{{ t('profile.email') }}</dt>
            <dd>{{ auth.user?.email || '-' }}</dd>
          </div>
          <div>
            <dt>{{ t('profile.phone') }}</dt>
            <dd>{{ auth.user?.phone || '-' }}</dd>
          </div>
          <div>
            <dt>{{ t('profile.lastLogin') }}</dt>
            <dd>{{ formatDateTime(auth.user?.lastLoginAt) }}</dd>
          </div>
          <div>
            <dt>{{ t('profile.permissions') }}</dt>
            <dd>
              {{ t('profile.permissionCount', { count: auth.user?.permissions.length ?? 0 }) }}
            </dd>
          </div>
        </dl>

        <div class="permission-cloud">
          <el-tag
            v-for="permission in auth.user?.permissions ?? []"
            :key="permission"
            size="small"
            effect="plain"
            class="permission-tag"
          >
            {{ permission }}
          </el-tag>
        </div>
      </el-card>

      <div class="profile-forms">
        <el-card class="form-card" shadow="never">
          <template #header>
            <div class="card-heading">
              <div>
                <strong>{{ t('profile.accountInfo') }}</strong>
                <span>{{ t('profile.subtitle') }}</span>
              </div>
            </div>
          </template>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-position="top"
          >
            <el-form-item :label="t('profile.displayName')" prop="displayName">
              <el-input v-model="profileForm.displayName" />
            </el-form-item>
            <div class="form-grid">
              <el-form-item :label="t('profile.email')" prop="email">
                <el-input v-model="profileForm.email" :placeholder="t('users.emailPlaceholder')" />
              </el-form-item>
              <el-form-item :label="t('profile.phone')" prop="phone">
                <el-input v-model="profileForm.phone" :placeholder="t('users.phonePlaceholder')" />
              </el-form-item>
            </div>
            <el-button type="primary" :loading="savingProfile" @click="saveProfile">
              {{ t('profile.saveProfile') }}
            </el-button>
          </el-form>
        </el-card>

        <el-card class="form-card" shadow="never">
          <template #header>
            <div class="card-heading">
              <div>
                <strong>{{ t('profile.security') }}</strong>
                <span>{{ t('auth.passwordRule') }}</span>
              </div>
            </div>
          </template>

          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-position="top"
          >
            <el-form-item :label="t('profile.currentPassword')" prop="currentPassword">
              <el-input
                v-model="passwordForm.currentPassword"
                type="password"
                show-password
                autocomplete="current-password"
              />
            </el-form-item>
            <div class="form-grid">
              <el-form-item :label="t('profile.newPassword')" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  show-password
                  autocomplete="new-password"
                />
              </el-form-item>
              <el-form-item :label="t('profile.confirmPassword')" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  show-password
                  autocomplete="new-password"
                />
              </el-form-item>
            </div>
            <el-button type="primary" :loading="savingPassword" @click="changePassword">
              {{ t('profile.changePasswordButton') }}
            </el-button>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import RoleTags from '@/components/RoleTags.vue'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'

interface ProfileForm {
  displayName: string
  email: string
  phone: string
}

interface PasswordForm {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

const { t } = useI18n()
const auth = useAuthStore()

const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const savingProfile = ref(false)
const savingPassword = ref(false)

const profileForm = reactive<ProfileForm>({ displayName: '', email: '', phone: '' })
const passwordForm = reactive<PasswordForm>({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const profileRules: FormRules<ProfileForm> = {
  displayName: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    { min: 2, max: 64, message: t('validation.displayName'), trigger: 'blur' },
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

const passwordRules: FormRules<PasswordForm> = {
  currentPassword: [{ required: true, message: t('validation.required'), trigger: 'blur' }],
  newPassword: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,64}$/,
      message: t('validation.password'),
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error(t('profile.passwordMismatch')))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

function fillForms(): void {
  const user = auth.user
  profileForm.displayName = user?.displayName ?? ''
  profileForm.email = user?.email ?? ''
  profileForm.phone = user?.phone ?? ''
}

async function saveProfile(): Promise<void> {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  savingProfile.value = true
  try {
    const user = await authApi.updateProfile({
      displayName: profileForm.displayName.trim(),
      email: profileForm.email.trim(),
      phone: profileForm.phone.trim(),
    })
    auth.setUser(user)
    ElMessage.success(t('profile.updated'))
  } finally {
    savingProfile.value = false
  }
}

async function changePassword(): Promise<void> {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  savingPassword.value = true
  try {
    await authApi.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success(t('profile.passwordUpdated'))
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value?.clearValidate()
  } finally {
    savingPassword.value = false
  }
}

onMounted(fillForms)
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: minmax(300px, 0.82fr) minmax(0, 1.18fr);
  gap: 18px;
  align-items: start;
}

.profile-card,
.form-card {
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  background: var(--app-surface);
  box-shadow: var(--app-shadow-soft);
}

.profile-card :deep(.el-card__body) {
  padding: 26px;
}

.profile-hero {
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-avatar {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  border-radius: 20px;
  color: #fff;
  font-size: 26px;
  font-weight: 750;
  background: linear-gradient(135deg, #6366f1, #0ea5e9);
  box-shadow: 0 12px 26px rgba(79, 70, 229, 0.3);
}

.profile-hero h2 {
  margin: 0;
  font-size: 21px;
  font-weight: 750;
  letter-spacing: -0.02em;
}

.profile-hero p {
  margin: 4px 0 0;
  color: var(--app-muted);
  font-size: 13px;
}

.profile-roles {
  margin-top: 18px;
}

.profile-meta {
  display: grid;
  gap: 0;
  margin: 20px 0 0;
}

.profile-meta > div {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  padding: 11px 0;
  border-bottom: 1px dashed var(--app-border);
}

.profile-meta > div:last-child {
  border-bottom: none;
}

.profile-meta dt {
  color: var(--app-muted);
  font-size: 12px;
}

.profile-meta dd {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  text-align: right;
  word-break: break-word;
}

.permission-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-height: 190px;
  margin-top: 16px;
  padding-top: 16px;
  overflow: auto;
  border-top: 1px dashed var(--app-border);
}

.permission-tag {
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 10px;
}

.profile-forms {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-card :deep(.el-card__header) {
  padding: 18px 22px;
  border-bottom: 1px solid var(--app-border);
}

.form-card :deep(.el-card__body) {
  padding: 22px;
}

.card-heading strong {
  font-size: 15px;
}

.card-heading span {
  display: block;
  margin-top: 3px;
  color: var(--app-muted);
  font-size: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

@media (max-width: 1000px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 620px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
