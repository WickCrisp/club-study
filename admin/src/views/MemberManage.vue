<template>
  <div class="app-page">
    <div class="page-head">
      <div>
        <h2>成员审批</h2>
        <p>你负责的社团收到的入社与退社申请，处理后成员状态会立即更新</p>
      </div>
      <div class="head-actions">
        <el-tag type="info" effect="plain" round>待审批 {{ rows.length }} 条</el-tag>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="rows"
        stripe
        border
        :header-cell-style="{ background: '#f7f9fc', color: '#4a5568', fontWeight: 600 }"
      >
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'primary' : 'warning'" effect="light" round>
              {{ row.status === 'PENDING' ? '入社' : '退社' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请人" width="110">
          <template #default="{ row }">{{ row.userName || '—' }}</template>
        </el-table-column>
        <el-table-column label="学号" width="130">
          <template #default="{ row }">
            <span class="mono">{{ row.studentNo || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="社团" width="150">
          <template #default="{ row }">{{ row.clubName || '—' }}</template>
        </el-table-column>
        <el-table-column label="申请理由" min-width="200">
          <template #default="{ row }">
            <span :class="{ muted: !row.applyReason }">{{ row.applyReason || '未填写' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button link type="primary" :loading="acting === row.id" @click="approveJoin(row)">通过</el-button>
              <el-button link type="danger" @click="rejectJoin(row)">拒绝</el-button>
            </template>
            <template v-else>
              <el-button link type="warning" :loading="acting === row.id" @click="approveQuit(row)">通过退出</el-button>
              <el-button link type="danger" @click="rejectQuit(row)">驳回</el-button>
            </template>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无待审批申请" :image-size="90" />
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

const rows = ref([])
const loading = ref(false)
const acting = ref(null)

const formatTime = (t) => (t ? t.substring(0, 16).replace('T', ' ') : '—')

const load = async () => {
  loading.value = true
  try {
    const clubsRes = await api.get('/club/my-managed')
    const clubs = clubsRes.code === 200 ? (clubsRes.data || []) : []

    const lists = await Promise.all(
      clubs.map(c =>
        api.get(`/club-member/pending/${c.id}`)
          .then(r => (r.code === 200 ? (r.data || []) : []).map(m => ({ ...m, clubName: c.name })))
          .catch(() => [])
      )
    )

    rows.value = lists.flat().sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
  } catch (e) {
    ElMessage.error('加载待审批失败')
  } finally {
    loading.value = false
  }
}

const doAction = async (row, url, successText) => {
  acting.value = row.id
  try {
    const result = await api.put(url)
    if (result.code === 200) {
      ElMessage.success(result.message || successText)
      await load()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    acting.value = null
  }
}

const approveJoin = (row) => doAction(row, `/club-member/approve/${row.id}`, '已通过申请')

const rejectJoin = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定拒绝「${row.userName || '该用户'}」加入「${row.clubName}」吗？`,
      '拒绝入社申请',
      { type: 'warning', confirmButtonText: '确定拒绝', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger' }
    )
  } catch (e) {
    return
  }
  doAction(row, `/club-member/reject/${row.id}`, '已拒绝申请')
}

const approveQuit = (row) => doAction(row, `/club-member/approve-quit/${row.id}`, '已通过退出申请')

const rejectQuit = async (row) => {
  try {
    await ElMessageBox.confirm(
      `驳回后「${row.userName || '该用户'}」将继续留在「${row.clubName}」，确定吗？`,
      '驳回退出申请',
      { type: 'warning', confirmButtonText: '确定驳回', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  doAction(row, `/club-member/reject-quit/${row.id}`, '已驳回退出申请')
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
