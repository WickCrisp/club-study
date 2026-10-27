<template>
  <div class="app-page">
    <div class="page-head">
      <div>
        <h2>报名管理</h2>
        <p>全部活动的报名记录，取消报名需要在这里放行，名额才会退回</p>
      </div>
      <div class="head-actions">
        <el-select v-model="statusFilter" style="width: 160px" @change="load">
          <el-option label="全部状态" value="" />
          <el-option label="已报名" value="REGISTERED" />
          <el-option label="取消待审" value="CANCEL_PENDING" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="registrations"
        stripe
        border
        :header-cell-style="{ background: '#f7f9fc', color: '#4a5568', fontWeight: 600 }"
      >
        <el-table-column label="活动" min-width="200">
          <template #default="{ row }">{{ row.activityTitle || ('活动 #' + row.activityId) }}</template>
        </el-table-column>
        <el-table-column label="社团" width="150">
          <template #default="{ row }">
            <span :class="{ muted: !row.clubName }">{{ row.clubName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="学生" width="110">
          <template #default="{ row }">{{ row.userName || '—' }}</template>
        </el-table-column>
        <el-table-column label="学号" width="130">
          <template #default="{ row }">
            <span class="mono">{{ row.studentNo || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light" round>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" width="160">
          <template #default="{ row }">{{ formatTime(row.registeredAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 'CANCEL_PENDING'">
              <el-button link type="primary" :loading="acting === row.id" @click="decide(row, true)">通过取消</el-button>
              <el-button link type="danger" @click="decide(row, false)">驳回</el-button>
            </template>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无报名记录" :image-size="90" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import api from '../api'

const registrations = ref([])
const loading = ref(false)
const statusFilter = ref('')
const acting = ref(null)

const formatTime = (t) => (t ? t.substring(0, 16).replace('T', ' ') : '—')
const statusText = (s) => ({ REGISTERED: '已报名', CANCEL_PENDING: '取消待审', CANCELLED: '已取消' }[s] || s)
const statusType = (s) => ({ REGISTERED: 'success', CANCEL_PENDING: 'warning', CANCELLED: 'info' }[s] || 'info')

const load = async () => {
  loading.value = true
  try {
    const query = statusFilter.value ? `?status=${statusFilter.value}` : ''
    const result = await api.get(`/registration/admin/list${query}`)
    if (result.code === 200) {
      registrations.value = result.data || []
    } else {
      ElMessage.error(result.message || '加载报名失败')
    }
  } catch (e) {
    ElMessage.error('加载报名失败')
  } finally {
    loading.value = false
  }
}

const decide = async (row, approved) => {
  const title = approved ? '通过取消申请' : '驳回取消申请'
  const text = approved
    ? `通过后「${row.userName || '该学生'}」的报名会被取消，名额退回活动。确定吗？`
    : `驳回后该学生仍保留在「${row.activityTitle || '该活动'}」的报名名单里。确定吗？`

  try {
    await ElMessageBox.confirm(text, title, {
      type: 'warning',
      confirmButtonText: approved ? '确定通过' : '确定驳回',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }

  acting.value = row.id
  try {
    const url = `/registration/${approved ? 'approve' : 'reject'}-cancel/${row.id}`
    const result = await api.put(url)
    if (result.code === 200) {
      ElMessage.success(result.message || '操作成功')
      load()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    acting.value = null
  }
}

onMounted(load)
</script>

<style scoped>
.mono {
  font-family: Consolas, Monaco, monospace;
  color: #3a4252;
}

.muted {
  color: #b6bfcc;
}
</style>
