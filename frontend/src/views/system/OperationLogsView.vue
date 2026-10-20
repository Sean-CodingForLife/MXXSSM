<template>
  <div class="page">
    <PageHeader :title="t('logs.operationTitle')" :subtitle="t('logs.operationSubtitle')">
      <template #actions>
        <el-button :icon="Refresh" :loading="loading" @click="load">{{
          t('common.refresh')
        }}</el-button>
      </template>
    </PageHeader>

    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <div class="filter-field grow">
          <span class="filter-label">{{ t('common.keyword') }}</span>
          <el-input
            v-model="query.keyword"
            :placeholder="t('logs.searchPlaceholder')"
            clearable
            :prefix-icon="Search"
            @keyup.enter="search"
          />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ t('logs.status') }}</span>
          <el-select v-model="query.status" clearable :placeholder="t('logs.allStatus')">
            <el-option :label="t('logs.success')" value="SUCCESS" />
            <el-option :label="t('logs.fail')" value="FAIL" />
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
        <el-table-column prop="username" :label="t('logs.account')" min-width="120">
          <template #default="{ row }">{{ row.username || '-' }}</template>
        </el-table-column>
        <el-table-column prop="action" :label="t('logs.action')" min-width="180">
          <template #default="{ row }"
            ><span class="cell-mono">{{ row.action }}</span></template
          >
        </el-table-column>
        <el-table-column :label="t('logs.method')" width="90">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" :label="t('logs.path')" min-width="210">
          <template #default="{ row }"
            ><span class="cell-mono">{{ row.path }}</span></template
          >
        </el-table-column>
        <el-table-column :label="t('logs.status')" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column :label="t('logs.duration')" width="110">
          <template #default="{ row }">{{ formatDuration(row.durationMs) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.createdAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">{{
              t('common.details')
            }}</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-hint">{{ t('logs.empty') }}</div>
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

    <el-dialog append-to-body v-model="detailVisible" :title="t('logs.detail')" width="640px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item :label="t('logs.account')">{{
          detail.username || '-'
        }}</el-descriptions-item>
        <el-descriptions-item :label="t('logs.action')">
          <span class="cell-mono">{{ detail.action }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="t('logs.method')">{{ detail.method }}</el-descriptions-item>
        <el-descriptions-item :label="t('logs.path')">
          <span class="cell-mono">{{ detail.path }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="t('logs.status')">
          <StatusTag :status="detail.status" />
        </el-descriptions-item>
        <el-descriptions-item :label="t('logs.duration')">
          {{ formatDuration(detail.durationMs) }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('logs.ip')">{{ detail.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('logs.message')">{{
          detail.message || '-'
        }}</el-descriptions-item>
        <el-descriptions-item :label="t('common.createdAt')">
          {{ formatDateTime(detail.createdAt) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Refresh, RefreshLeft, Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { logApi } from '@/api/logs'
import { formatDateTime, formatDuration } from '@/utils/format'
import type { OperationLog } from '@/types/log'

const { t } = useI18n()

const rows = ref<OperationLog[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref<OperationLog | null>(null)
const query = reactive({ keyword: '', status: '' })

async function load(): Promise<void> {
  loading.value = true
  try {
    const result = await logApi.operationLogs({
      keyword: query.keyword.trim(),
      status: query.status,
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
  page.value = 1
  void load()
}

function onSizeChange(): void {
  page.value = 1
  void load()
}

function openDetail(row: OperationLog): void {
  detail.value = row
  detailVisible.value = true
}

onMounted(load)
</script>
