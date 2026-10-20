<template>
  <div class="page">
    <PageHeader :title="t('menus.title')" :subtitle="t('menus.subtitle')">
      <template #actions>
        <el-button
          v-if="auth.hasPermission('admin:menu:create')"
          type="primary"
          :icon="Plus"
          @click="openCreate"
        >
          {{ t('menus.add') }}
        </el-button>
      </template>
    </PageHeader>

    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <div class="filter-field grow">
          <span class="filter-label">{{ t('common.keyword') }}</span>
          <el-input
            v-model="keyword"
            :placeholder="t('menus.searchPlaceholder')"
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
        <el-table-column :label="t('menus.name')" min-width="150">
          <template #default="{ row }">
            <div class="menu-cell">
              <span class="menu-icon"><AppIcon :name="row.icon" /></span>
              <div>
                <strong>{{ row.name }}</strong>
                <span class="cell-muted">{{ row.code }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="path" :label="t('menus.path')" min-width="170">
          <template #default="{ row }"
            ><span class="cell-mono">{{ row.path }}</span></template
          >
        </el-table-column>
        <el-table-column prop="groupName" :label="t('menus.group')" width="110" />
        <el-table-column prop="permissionCode" :label="t('menus.permission')" min-width="210">
          <template #default="{ row }">
            <span class="cell-mono">{{ row.permissionCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" :label="t('menus.sortOrder')" width="90" align="center" />
        <el-table-column :label="t('common.status')" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="auth.hasPermission('admin:menu:update')"
              link
              type="primary"
              @click="openEdit(row)"
            >
              {{ t('common.edit') }}
            </el-button>
            <el-button
              v-if="auth.hasPermission('admin:menu:delete')"
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
      :title="mode === 'create' ? t('menus.add') : t('menus.edit')"
      width="640px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item :label="t('menus.code')" prop="code">
            <el-input v-model="form.code" placeholder="users" />
          </el-form-item>
          <el-form-item :label="t('menus.name')" prop="name">
            <el-input v-model="form.name" />
          </el-form-item>
        </div>

        <div class="form-grid">
          <el-form-item :label="t('menus.path')" prop="path">
            <el-input v-model="form.path" :placeholder="t('menus.pathHint')" />
          </el-form-item>
          <el-form-item :label="t('menus.icon')" prop="icon">
            <el-input v-model="form.icon" :placeholder="t('menus.iconHint')" />
          </el-form-item>
        </div>

        <div class="form-grid">
          <el-form-item :label="t('menus.group')" prop="groupName">
            <el-input v-model="form.groupName" :placeholder="t('menus.groupHint')" />
          </el-form-item>
          <el-form-item :label="t('menus.sortOrder')" prop="sortOrder">
            <el-input-number
              v-model="form.sortOrder"
              :min="0"
              :max="9999"
              controls-position="right"
            />
          </el-form-item>
        </div>

        <el-form-item :label="t('menus.permission')" prop="permissionCode">
          <el-select
            v-model="form.permissionCode"
            filterable
            :placeholder="t('menus.permissionHint')"
          >
            <el-option
              v-for="permission in permissions"
              :key="permission.id"
              :label="`${permission.code} · ${permission.name}`"
              :value="permission.code"
            />
          </el-select>
        </el-form-item>

        <el-form-item :label="t('common.status')" prop="status">
          <el-select v-model="form.status">
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import AppIcon from '@/components/AppIcon.vue'
import { menuApi } from '@/api/menus'
import { permissionApi } from '@/api/permissions'
import { useAuthStore } from '@/stores/auth'
import type { Menu, MenuPayload } from '@/types/menu'
import type { Permission } from '@/types/permission'

interface MenuForm {
  code: string
  name: string
  path: string
  icon: string
  groupName: string
  permissionCode: string
  sortOrder: number
  status: string
}

const { t } = useI18n()
const auth = useAuthStore()

const rows = ref<Menu[]>([])
const permissions = ref<Permission[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const mode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive<MenuForm>({
  code: '',
  name: '',
  path: '',
  icon: 'Grid',
  groupName: 'System',
  permissionCode: '',
  sortOrder: 10,
  status: 'ACTIVE',
})

const rules: FormRules<MenuForm> = {
  code: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9-]{1,63}$/, message: t('validation.code'), trigger: 'blur' },
  ],
  name: [{ required: true, message: t('validation.required'), trigger: 'blur' }],
  path: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    { pattern: /^\/[A-Za-z0-9/_-]*$/, message: t('validation.path'), trigger: 'blur' },
  ],
  groupName: [{ required: true, message: t('validation.required'), trigger: 'blur' }],
  permissionCode: [{ required: true, message: t('validation.required'), trigger: 'change' }],
  sortOrder: [{ required: true, message: t('validation.required'), trigger: 'change' }],
  status: [{ required: true, message: t('validation.required'), trigger: 'change' }],
}

async function load(): Promise<void> {
  loading.value = true
  try {
    const result = await menuApi.page({
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

async function loadPermissions(): Promise<void> {
  try {
    permissions.value = await permissionApi.all()
  } catch {
    permissions.value = []
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
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: Menu): void {
  mode.value = 'edit'
  editingId.value = row.id
  form.code = row.code
  form.name = row.name
  form.path = row.path
  form.icon = row.icon ?? 'Grid'
  form.groupName = row.groupName
  form.permissionCode = row.permissionCode
  form.sortOrder = row.sortOrder
  form.status = row.status
  dialogVisible.value = true
}

function resetForm(): void {
  form.code = ''
  form.name = ''
  form.path = ''
  form.icon = 'Grid'
  form.groupName = 'System'
  form.permissionCode = ''
  form.sortOrder = 10
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
    const payload: MenuPayload = {
      ...form,
      code: form.code.trim(),
      name: form.name.trim(),
      path: form.path.trim(),
    }
    if (mode.value === 'create') {
      await menuApi.create(payload)
      ElMessage.success(t('menus.createSuccess'))
    } else if (editingId.value !== null) {
      await menuApi.update(editingId.value, payload)
      ElMessage.success(t('menus.updateSuccess'))
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row: Menu): Promise<void> {
  try {
    await ElMessageBox.confirm(t('menus.deleteConfirm', { name: row.name }), t('common.confirm'), {
      type: 'warning',
      confirmButtonText: t('common.delete'),
      cancelButtonText: t('common.cancel'),
    })
  } catch {
    return
  }
  try {
    await menuApi.remove(row.id)
    ElMessage.success(t('menus.deleteSuccess'))
    await load()
  } catch {
    // The API error toast explains why the menu cannot be deleted.
  }
}

onMounted(async () => {
  await loadPermissions()
  await load()
})
</script>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.menu-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.menu-cell > div {
  display: flex;
  flex-direction: column;
}

.menu-icon {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  flex: 0 0 auto;
  border-radius: 9px;
  color: var(--el-color-primary);
  background: var(--app-primary-soft);
}

.menu-cell .cell-muted {
  font-size: 11px;
}

@media (max-width: 620px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
