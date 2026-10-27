<template>
  <div>
    <div class="page-head">
      <div>
        <h2>入社与退社审批</h2>
        <p>{{ clubName }} · 审批通过后成员状态立即生效</p>
      </div>
      <div class="head-actions">
        <el-tag type="info" effect="plain" round>待处理 {{ rows.length }} 条</el-tag>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="rows" stripe border
                :header-cell-style="{ background: '#f7f9fc', color: '#4a5568', fontWeight: 600 }">
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
        <el-table-column label="学号" width="140">
          <template #default="{ row }"><span class="mono">{{ row.studentNo || '—' }}</span></template>
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
              <el-button link type="primary" :loading="acting === row.id" @click="act(row, 'approve')">通过</el-button>
              <el-button link type="danger" @click="reject(row, 'reject')">拒绝</el-button>
            </template>
            <template v-else>
              <el-button link type="warning" :loading="acting === row.id" @click="act(row, 'approve-quit')">通过退出</el-button>
              <el-button link type="danger" @click="reject(row, 'reject-quit')">驳回</el-button>
            </template>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无待审批申请" :image-size="80" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import api from '../../api'
import { useManagedClubs } from '../../composables/useManagedClubs'

const { currentClub, currentClubId, ensure } = useManagedClubs()

const rows = ref([])
const loading = ref(false)
const acting = ref(null)

const clubName = computed(() => (currentClub.value ? currentClub.value.name : '—'))
const formatTime = (t) => (t ? t.substring(0, 16).replace('T', ' ') : '—')

const load = async () => {
  await ensure()
  if (!currentClub.value) {
    rows.value = []
    return
  }
  loading.value = true
  try {
    const result = await api.get(`/club-member/pending/${currentClub.value.id}`)
    rows.value = result.code === 200 ? (result.data || []) : []
  } catch (e) {
    rows.value = []
    ElMessage.error('加载待审批失败')
  } finally {
    loading.value = false
  }
}

const act = async (row, action) => {
  acting.value = row.id
  try {
    const result = await api.put(`/club-member/${action}/${row.id}`)
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

const reject = async (row, action) => {
  const isQuit = action === 'reject-quit'
  try {
    await ElMessageBox.confirm(
      isQuit
        ? `驳回后「${row.userName}」将继续留在「${clubName.value}」，确定吗？`
        : `确定拒绝「${row.userName}」加入「${clubName.value}」吗？`,
      isQuit ? '驳回退出申请' : '拒绝入社申请',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger' }
    )
  } catch (e) {
    return
  }
  act(row, action)
}

watch(currentClubId, load)
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
