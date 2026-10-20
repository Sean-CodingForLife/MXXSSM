<template>
  <div class="page">
    <PageHeader :title="t('users.title')" :subtitle="t('users.subtitle')">
      <template #actions>
        <el-button
          v-if="auth.hasPermission('admin:user:create')"
          type="primary"
          :icon="Plus"
          @click="openCreate"
        >
          {{ t('users.add') }}
        </el-button>
      </template>
    </PageHeader>

    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <div class="filter-field grow">
          <span class="filter-label">{{ t('common.keyword') }}</span>
          <el-input
            v-model="query.keyword"
            :placeholder="t('users.searchPlaceholder')"
            clearable
            :prefix-icon="Search"
            @keyup.enter="search"
          />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ t('common.status') }}</span>
          <el-select v-model="query.status" clearable :placeholder="t('common.all')">
            <el-option :label="t('status.ACTIVE')" value="ACTIVE" />
            <el-option :label="t('status.DISABLED')" value="DISABLED" />
          </el-select>
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ t('users.roles') }}</span>
          <el-select v-model="query.role" clearable :placeholder="t('common.all')">
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="roleLabel(role.code, role.name)"
              :value="role.code"
            />
          </el-select>
        </div>
        <div class="toolbar">
          <el-button type="primary" :icon="Search" @click="search">{{
            t('common.search')
          }}</el-button>
          <el-button :icon="RefreshLeft" @click="reset">{{ t('common.reset') }}</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" row-key="id">
        <el-table-column prop="id" label="ID" width="76" />
        <el-table-column :label="t('users.username')" min-width="150">
          <template #default="{ row }">
            <div class="account-cell">
              <span class="mini-avatar">{{ row.username.slice(0, 1).toUpperCase() }}</span>
              <div>
                <strong>{{ row.username }}</strong>
                <span class="cell-muted">{{ row.email || '-' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="displayName" :label="t('users.displayName')" min-width="130" />
        <el-table-column :label="t('users.roles')" min-width="150">
          <template #default="{ row }"><RoleTags :roles="row.roles" /></template>
        </el-table-column>
        <el-table-column :label="t('common.status')" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column :label="t('users.lastLogin')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.lastLoginAt) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.createdAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="auth.hasPermission('admin:user:update')"
              link
              type="primary"
              @click="openEdit(row)"
            >
              {{ t('common.edit') }}
            </el-button>
            <el-button
              v-if="auth.hasPermission('admin:user:delete')"
              link
              type="danger"
              @click="remove(row)"
            >
              {{ t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-hint">{{ t('users.empty') }}</div>
        </template>
      </el-table>

      <div class="table-footer">
        <span class="summary">{{ t('common.total', { count: total }) }}</span>
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next"
          background
          @current-change="load"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      append-to-body
      v-model="dialogVisible"
      :title="mode === 'create' ? t('users.add') : t('users.edit')"
      width="640px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item :label="t('users.username')" prop="username">
            <el-input
              v-model="form.username"
              :disabled="mode === 'edit'"
              :placeholder="t('users.usernameHint')"
            />
          </el-form-item>
          <el-form-item :label="t('users.displayName')" prop="displayName">
            <el-input v-model="form.displayName" />
          </el-form-item>
        </div>

        <el-form-item v-if="mode === 'create'" :label="t('users.password')" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            autocomplete="new-password"
          />
          <div class="form-hint">{{ t('users.passwordHint') }}</div>
        </el-form-item>

        <div class="form-grid">
          <el-form-item :label="t('users.email')" prop="email">
            <el-input v-model="form.email" :placeholder="t('users.emailPlaceholder')" />
          </el-form-item>
          <el-form-item :label="t('users.phone')" prop="phone">
            <el-input v-model="form.phone" :placeholder="t('users.phonePlaceholder')" />
          </el-form-item>
        </div>

        <div class="form-grid">
          <el-form-item :label="t('users.roles')" prop="roles">
            <el-select v-model="form.roles" multiple :placeholder="t('users.selectRoles')">
              <el-option
                v-for="role in roles"
                :key="role.id"
                :label="roleLabel(role.code, role.name)"
                :value="role.code"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('common.status')" prop="status">
            <el-select v-model="form.status">
              <el-option :label="t('status.ACTIVE')" value="ACTIVE" />
              <el-option :label="t('status.DISABLED')" value="DISABLED" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="save">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import RoleTags from '@/components/RoleTags.vue'
import StatusTag from '@/components/StatusTag.vue'
import { userApi } from '@/api/users'
import { roleApi } from '@/api/roles'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'
import type { Role } from '@/types/role'
import type { User, UserPayload } from '@/types/user'

interface UserForm {
  username: string
  displayName: string
  password: string
  email: string
  phone: string
  roles: string[]
  status: string
}

const { t, te } = useI18n()
const auth = useAuthStore()

const rows = ref<User[]>([])
const roles = ref<Role[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const mode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)

const query = reactive({ keyword: '', status: '', role: '' })
const formRef = ref<FormInstance>()
const form = reactive<UserForm>({
  username: '',
  displayName: '',
  password: '',
  email: '',
  phone: '',
  roles: ['USER'],
  status: 'ACTIVE',
})

const rules = computed<FormRules<UserForm>>(() => {
  const result: FormRules<UserForm> = {
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
    email: [{ type: 'email', message: t('validation.email'), trigger: 'blur' }],
    phone: [
      {
        pattern: /^$|^[0-9+()\- ]{5,32}$/,
        message: t('validation.phone'),
        trigger: 'blur',
      },
    ],
    roles: [
      {
        required: true,
        type: 'array',
        min: 1,
        message: t('validation.roleRequired'),
        trigger: 'change',
      },
    ],
    status: [{ required: true, message: t('validation.required'), trigger: 'change' }],
  }
  if (mode.value === 'create') {
    result.password = [
      { required: true, message: t('validation.required'), trigger: 'blur' },
      {
        pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,64}$/,
        message: t('validation.password'),
        trigger: 'blur',
      },
    ]
  }
  return result
})

function roleLabel(code: string, fallback: string): string {
  const key = `role.${code}`
  return te(key) ? t(key) : fallback
}

async function loadRoles(): Promise<void> {
  try {
    roles.value = await roleApi.all()
  } catch {
    roles.value = []
  }
}

async function load(): Promise<void> {
  loading.value = true
  try {
    const result = await userApi.page({
      keyword: query.keyword.trim(),
      status: query.status,
      role: query.role,
      page: page.value,
      size: size.value,
    })
    rows.value = result.items
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function search(): void {
  page.value = 1
  void load()
}

function reset(): void {
  query.keyword = ''
  query.status = ''
  query.role = ''
  page.value = 1
  void load()
}

function onSizeChange(): void {
  page.value = 1
  void load()
}

function openCreate(): void {
  mode.value = 'create'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: User): void {
  mode.value = 'edit'
  editingId.value = row.id
  form.username = row.username
  form.displayName = row.displayName
  form.password = ''
  form.email = row.email ?? ''
  form.phone = row.phone ?? ''
  form.roles = [...row.roles]
  form.status = row.status
  dialogVisible.value = true
}

function resetForm(): void {
  form.username = ''
  form.displayName = ''
  form.password = ''
  form.email = ''
  form.phone = ''
  form.roles = ['USER']
  form.status = 'ACTIVE'
  formRef.value?.clearValidate()
}

async function save(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  saving.value = true
  try {
    const payload: UserPayload = {
      displayName: form.displayName.trim(),
      email: form.email.trim(),
      phone: form.phone.trim(),
      roles: form.roles,
      status: form.status,
    }
    if (mode.value === 'create') {
      payload.username = form.username.trim()
      payload.password = form.password
      await userApi.create(payload)
      ElMessage.success(t('users.createSuccess'))
    } else if (editingId.value !== null) {
      await userApi.update(editingId.value, payload)
      ElMessage.success(t('users.updateSuccess'))
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row: User): Promise<void> {
  try {
    await ElMessageBox.confirm(
      t('users.deleteConfirm', { name: row.username }),
      t('common.confirm'),
      {
        type: 'warning',
        confirmButtonText: t('common.delete'),
        cancelButtonText: t('common.cancel'),
      },
    )
  } catch {
    return
  }
  try {
    await userApi.remove(row.id)
    ElMessage.success(t('users.deleteSuccess'))
    await load()
  } catch {
    // The API error toast explains why the account cannot be deleted.
  }
}

onMounted(async () => {
  await loadRoles()
  await load()
})
</script>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.account-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.account-cell > div {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.account-cell strong {
  font-size: 13px;
}

.account-cell .cell-muted {
  overflow: hidden;
  max-width: 190px;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mini-avatar {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  flex: 0 0 auto;
  border-radius: 10px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  background: linear-gradient(135deg, #6366f1, #0ea5e9);
}

@media (max-width: 620px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
