<template>
  <div class="page">
    <PageHeader :title="t('configs.title')" :subtitle="t('configs.subtitle')">
      <template #actions>
        <el-button
          v-if="auth.hasPermission('admin:config:create')"
          type="primary"
          :icon="Plus"
          @click="openCreate"
        >
          {{ t('configs.add') }}
        </el-button>
      </template>
    </PageHeader>

    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <div class="filter-field grow">
          <span class="filter-label">{{ t('common.keyword') }}</span>
          <el-input
            v-model="keyword"
            :placeholder="t('configs.searchPlaceholder')"
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
        <el-table-column :label="t('configs.key')" min-width="230">
          <template #default="{ row }">
            <span class="cell-mono">{{ row.configKey }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('configs.value')" min-width="200">
          <template #default="{ row }">
            <span class="config-value">{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="t('configs.description')" min-width="250">
          <template #default="{ row }"
            ><span class="cell-muted">{{ row.description || '-' }}</span></template
          >
        </el-table-column>
        <el-table-column :label="t('common.status')" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column :label="t('common.updatedAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="auth.hasPermission('admin:config:update')"
              link
              type="primary"
              @click="openEdit(row)"
            >
              {{ t('common.edit') }}
            </el-button>
            <el-button
              v-if="auth.hasPermission('admin:config:delete')"
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
      :title="mode === 'create' ? t('configs.add') : t('configs.edit')"
      width="580px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item :label="t('configs.key')" prop="configKey">
          <el-input v-model="form.configKey" :disabled="mode === 'edit'" placeholder="site.name" />
          <div class="form-hint">{{ t('configs.keyHint') }}</div>
        </el-form-item>
        <el-form-item :label="t('configs.value')" prop="configValue">
          <el-input v-model="form.configValue" type="textarea" :rows="2" />
          <div class="form-hint">{{ t('configs.booleanHint') }}</div>
        </el-form-item>
        <el-form-item :label="t('configs.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" />
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
import { configApi } from '@/api/configs'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'
import type { ConfigPayload, SystemConfig } from '@/types/config'

interface ConfigForm {
  configKey: string
  configValue: string
  description: string
  status: string
}

const { t } = useI18n()
const auth = useAuthStore()

const rows = ref<SystemConfig[]>([])
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
const form = reactive<ConfigForm>({
  configKey: '',
  configValue: '',
  description: '',
  status: 'ACTIVE',
})

const rules: FormRules<ConfigForm> = {
  configKey: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    {
      pattern: /^[A-Za-z][A-Za-z0-9._-]{1,127}$/,
      message: t('validation.configKey'),
      trigger: 'blur',
    },
  ],
  configValue: [{ required: true, message: t('validation.required'), trigger: 'blur' }],
  status: [{ required: true, message: t('validation.required'), trigger: 'change' }],
}

async function load(): Promise<void> {
  loading.value = true
  try {
    const result = await configApi.page({
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

function openEdit(row: SystemConfig): void {
  mode.value = 'edit'
  editingId.value = row.id
  form.configKey = row.configKey
  form.configValue = row.configValue
  form.description = row.description ?? ''
  form.status = row.status
  dialogVisible.value = true
}

function resetForm(): void {
  form.configKey = ''
  form.configValue = ''
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
    const payload: ConfigPayload = {
      configKey: form.configKey.trim(),
      configValue: form.configValue.trim(),
      description: form.description.trim(),
      status: form.status,
    }
    if (mode.value === 'create') {
      await configApi.create(payload)
      ElMessage.success(t('configs.createSuccess'))
    } else if (editingId.value !== null) {
      await configApi.update(editingId.value, payload)
      ElMessage.success(t('configs.updateSuccess'))
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row: SystemConfig): Promise<void> {
  try {
    await ElMessageBox.confirm(
      t('configs.deleteConfirm', { key: row.configKey }),
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
    await configApi.remove(row.id)
    ElMessage.success(t('configs.deleteSuccess'))
    await load()
  } catch {
    // The API error toast explains why the config cannot be deleted.
  }
}

onMounted(load)
</script>

<style scoped>
.config-value {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  padding: 2px 8px;
  border-radius: 6px;
  color: var(--app-text);
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
  background: var(--app-bg);
}
</style>
