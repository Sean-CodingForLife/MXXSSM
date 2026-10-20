<template>
  <div class="page">
    <PageHeader :title="t('permissions.title')" :subtitle="t('permissions.subtitle')">
      <template #actions>
        <el-button
          v-if="auth.hasPermission('admin:permission:create')"
          type="primary"
          :icon="Plus"
          @click="openCreate"
        >
          {{ t('permissions.add') }}
        </el-button>
      </template>
    </PageHeader>

    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <div class="filter-field grow">
          <span class="filter-label">{{ t('common.keyword') }}</span>
          <el-input
            v-model="keyword"
            :placeholder="t('permissions.searchPlaceholder')"
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
        <el-table-column :label="t('permissions.code')" min-width="220">
          <template #default="{ row }">
            <el-tag effect="plain" class="code-tag">{{ row.code }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="t('permissions.name')" min-width="180" />
        <el-table-column prop="description" :label="t('permissions.description')" min-width="260">
          <template #default="{ row }"
            ><span class="cell-muted">{{ row.description || '-' }}</span></template
          >
        </el-table-column>
        <el-table-column :label="t('common.status')" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column :label="t('common.createdAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="auth.hasPermission('admin:permission:update')"
              link
              type="primary"
              @click="openEdit(row)"
            >
              {{ t('common.edit') }}
            </el-button>
            <el-button
              v-if="auth.hasPermission('admin:permission:delete')"
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
      :title="mode === 'create' ? t('permissions.add') : t('permissions.edit')"
      width="560px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item :label="t('permissions.code')" prop="code">
          <el-input v-model="form.code" :disabled="mode === 'edit'" placeholder="admin:user:read" />
          <div class="form-hint">
            {{ mode === 'edit' ? t('permissions.codeImmutable') : t('permissions.codeHint') }}
          </div>
        </el-form-item>
        <el-form-item :label="t('permissions.name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="t('permissions.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { permissionApi } from '@/api/permissions'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'
import type { Permission, PermissionPayload } from '@/types/permission'

interface PermissionForm {
  code: string
  name: string
  description: string
  status: string
}

const { t } = useI18n()
const auth = useAuthStore()

const rows = ref<Permission[]>([])
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
const form = reactive<PermissionForm>({ code: '', name: '', description: '', status: 'ACTIVE' })

const rules: FormRules<PermissionForm> = {
  code: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9:_-]{2,63}$/, message: t('validation.code'), trigger: 'blur' },
  ],
  name: [{ required: true, message: t('validation.required'), trigger: 'blur' }],
  status: [{ required: true, message: t('validation.required'), trigger: 'change' }],
}

async function load(): Promise<void> {
  loading.value = true
  try {
    const result = await permissionApi.page({
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
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: Permission): void {
  mode.value = 'edit'
  editingId.value = row.id
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
    const payload: PermissionPayload = {
      code: form.code.trim(),
      name: form.name.trim(),
      description: form.description.trim(),
      status: form.status,
    }
    if (mode.value === 'create') {
      await permissionApi.create(payload)
      ElMessage.success(t('permissions.createSuccess'))
    } else if (editingId.value !== null) {
      await permissionApi.update(editingId.value, payload)
      ElMessage.success(t('permissions.updateSuccess'))
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row: Permission): Promise<void> {
  try {
    await ElMessageBox.confirm(
      t('permissions.deleteConfirm', { name: row.name }),
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
    await permissionApi.remove(row.id)
    ElMessage.success(t('permissions.deleteSuccess'))
    await load()
  } catch {
    // The API error toast explains why the permission cannot be deleted.
  }
}

onMounted(load)
</script>

<style scoped>
.code-tag {
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 11px;
}
</style>
