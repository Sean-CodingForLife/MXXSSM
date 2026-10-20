<template>
  <div class="page">
    <PageHeader :title="t('roles.title')" :subtitle="t('roles.subtitle')">
      <template #actions>
        <el-button
          v-if="auth.hasPermission('admin:role:create')"
          type="primary"
          :icon="Plus"
          @click="openCreate"
        >
          {{ t('roles.add') }}
        </el-button>
      </template>
    </PageHeader>

    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <div class="filter-field grow">
          <span class="filter-label">{{ t('common.keyword') }}</span>
          <el-input
            v-model="keyword"
            :placeholder="t('roles.searchPlaceholder')"
            clearable
            :prefix-icon="Search"
            @keyup.enter="search"
          />
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
        <el-table-column :label="t('roles.code')" min-width="200">
          <template #default="{ row }">
            <div class="role-code">
              <el-tag effect="plain" class="code-tag">{{ row.code }}</el-tag>
              <el-tag v-if="isBuiltIn(row.code)" type="warning" size="small">
                {{ t('roles.builtIn') }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="t('roles.name')" min-width="140" />
        <el-table-column prop="description" :label="t('roles.description')" min-width="220">
          <template #default="{ row }">
            <span class="cell-muted">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('roles.userCount')" width="100">
          <template #default="{ row }">
            <el-tag round effect="light" type="info">{{ row.userCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.status')" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column :label="t('common.createdAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="230" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="auth.hasPermission('admin:role:permission:read')"
              link
              type="primary"
              @click="openPermissions(row)"
            >
              {{ t('roles.permissions') }}
            </el-button>
            <el-button
              v-if="auth.hasPermission('admin:role:update')"
              link
              type="primary"
              @click="openEdit(row)"
            >
              {{ t('common.edit') }}
            </el-button>
            <el-button
              v-if="auth.hasPermission('admin:role:delete') && !isBuiltIn(row.code)"
              link
              type="danger"
              @click="remove(row)"
            >
              {{ t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-hint">{{ t('common.noData') }}</div>
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
      :title="mode === 'create' ? t('roles.add') : t('roles.edit')"
      width="560px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item :label="t('roles.code')" prop="code">
          <el-input
            v-model="form.code"
            :disabled="mode === 'edit'"
            placeholder="CUSTOM_ROLE"
            @input="form.code = form.code.toUpperCase()"
          />
          <div class="form-hint">{{ t('roles.codeHint') }}</div>
        </el-form-item>
        <el-form-item :label="t('roles.name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="t('roles.description')" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            :placeholder="t('roles.descriptionPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('common.status')" prop="status">
          <el-select v-model="form.status" :disabled="mode === 'edit' && isBuiltIn(editingCode)">
            <el-option :label="t('status.ACTIVE')" value="ACTIVE" />
            <el-option :label="t('status.DISABLED')" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="save">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog
      append-to-body
      v-model="permissionDialogVisible"
      :title="permissionTitle"
      width="720px"
    >
      <div class="permission-toolbar">
        <div class="toolbar">
          <el-button size="small" @click="selectAllPermissions">{{
            t('roles.selectAll')
          }}</el-button>
          <el-button size="small" @click="clearPermissions">{{ t('roles.clearAll') }}</el-button>
        </div>
        <span class="cell-muted">
          {{ t('roles.permissionCount', { count: selectedPermissionIds.length }) }}
        </span>
      </div>

      <el-scrollbar max-height="56vh">
        <div class="permission-groups">
          <section v-for="group in permissionGroups" :key="group.prefix" class="permission-group">
            <header>
              <strong>{{ group.prefix.toUpperCase() }}</strong>
              <span>{{ group.items.length }}</span>
            </header>
            <el-checkbox-group v-model="selectedPermissionIds">
              <el-checkbox
                v-for="permission in group.items"
                :key="permission.id"
                :value="permission.id"
              >
                <span class="permission-code">{{ permission.code }}</span>
                <span class="cell-muted"> · {{ permission.name }}</span>
              </el-checkbox>
            </el-checkbox-group>
          </section>
          <div v-if="!permissionGroups.length" class="empty-hint">{{ t('common.noData') }}</div>
        </div>
      </el-scrollbar>

      <template #footer>
        <el-button @click="permissionDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="savingPermissions" @click="savePermissions">
          {{ t('common.save') }}
        </el-button>
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
import StatusTag from '@/components/StatusTag.vue'
import { roleApi } from '@/api/roles'
import { permissionApi } from '@/api/permissions'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'
import type { Role, RolePayload } from '@/types/role'
import type { Permission } from '@/types/permission'

interface RoleForm {
  code: string
  name: string
  description: string
  status: string
}

const { t } = useI18n()
const auth = useAuthStore()

const rows = ref<Role[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)

const dialogVisible = ref(false)
const mode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)
const editingCode = ref('')
const formRef = ref<FormInstance>()
const form = reactive<RoleForm>({ code: '', name: '', description: '', status: 'ACTIVE' })

const rules: FormRules<RoleForm> = {
  code: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    {
      pattern: /^[A-Z][A-Z0-9_]{1,63}$/,
      message: t('validation.code'),
      trigger: 'blur',
    },
  ],
  name: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    { max: 64, message: t('validation.displayName'), trigger: 'blur' },
  ],
  status: [{ required: true, message: t('validation.required'), trigger: 'change' }],
}

const permissionDialogVisible = ref(false)
const permissionTitle = ref('')
const permissions = ref<Permission[]>([])
const selectedPermissionIds = ref<number[]>([])
const activeRoleId = ref<number | null>(null)
const savingPermissions = ref(false)

const permissionGroups = computed(() => {
  const groups = new Map<string, Permission[]>()
  for (const permission of permissions.value) {
    const prefix = permission.code.split(':')[0] || 'general'
    if (!groups.has(prefix)) {
      groups.set(prefix, [])
    }
    groups.get(prefix)?.push(permission)
  }
  return [...groups.entries()].map(([prefix, items]) => ({ prefix, items }))
})

function isBuiltIn(code: string): boolean {
  return code === 'ADMIN' || code === 'USER'
}

async function load(): Promise<void> {
  loading.value = true
  try {
    const result = await roleApi.page({
      keyword: keyword.value.trim(),
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
  keyword.value = ''
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
  editingCode.value = ''
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: Role): void {
  mode.value = 'edit'
  editingId.value = row.id
  editingCode.value = row.code
  form.code = row.code
  form.name = row.name
  form.description = row.description ?? ''
  form.status = row.status
  dialogVisible.value = true
}

function resetForm(): void {
  form.code = ''
  form.name = ''
  form.description = ''
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
    const payload: RolePayload = {
      code: form.code.trim().toUpperCase(),
      name: form.name.trim(),
      description: form.description.trim(),
      status: form.status,
    }
    if (mode.value === 'create') {
      await roleApi.create(payload)
      ElMessage.success(t('roles.createSuccess'))
    } else if (editingId.value !== null) {
      await roleApi.update(editingId.value, payload)
      ElMessage.success(t('roles.updateSuccess'))
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row: Role): Promise<void> {
  try {
    await ElMessageBox.confirm(t('roles.deleteConfirm', { name: row.name }), t('common.confirm'), {
      type: 'warning',
      confirmButtonText: t('common.delete'),
      cancelButtonText: t('common.cancel'),
    })
  } catch {
    return
  }
  try {
    await roleApi.remove(row.id)
    ElMessage.success(t('roles.deleteSuccess'))
    await load()
  } catch {
    // The API error toast explains why the role cannot be deleted.
  }
}

async function openPermissions(row: Role): Promise<void> {
  activeRoleId.value = row.id
  permissionTitle.value = t('roles.permissionsTitle', { name: row.name })
  permissionDialogVisible.value = true
  try {
    if (!permissions.value.length) {
      permissions.value = await permissionApi.all()
    }
    const owned = await roleApi.permissions(row.id)
    selectedPermissionIds.value = owned.map((permission) => permission.id)
  } catch {
    selectedPermissionIds.value = []
  }
}

function selectAllPermissions(): void {
  selectedPermissionIds.value = permissions.value.map((permission) => permission.id)
}

function clearPermissions(): void {
  selectedPermissionIds.value = []
}

async function savePermissions(): Promise<void> {
  if (activeRoleId.value === null) {
    return
  }
  savingPermissions.value = true
  try {
    await roleApi.updatePermissions(activeRoleId.value, selectedPermissionIds.value)
    ElMessage.success(t('roles.permissionsSaved'))
    permissionDialogVisible.value = false
  } finally {
    savingPermissions.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.code-tag {
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 11px;
}

.role-code {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.permission-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--app-border);
}

.permission-groups {
  display: grid;
  gap: 18px;
  padding: 14px 2px 2px;
}

.permission-group header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.permission-group header strong {
  color: var(--app-muted);
  font-size: 11px;
  letter-spacing: 0.12em;
}

.permission-group header span {
  display: grid;
  place-items: center;
  min-width: 20px;
  height: 20px;
  border-radius: 999px;
  color: var(--app-muted);
  font-size: 11px;
  background: var(--app-primary-soft);
}

.permission-group :deep(.el-checkbox-group) {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2px 12px;
}

.permission-group :deep(.el-checkbox) {
  height: auto;
  min-height: 30px;
  margin-right: 0;
}

.permission-group :deep(.el-checkbox__label) {
  line-height: 1.45;
  white-space: normal;
  overflow-wrap: anywhere;
}

.permission-code {
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 11px;
}

@media (max-width: 720px) {
  .permission-group :deep(.el-checkbox-group) {
    grid-template-columns: 1fr;
  }
}
</style>
