<template>
  <div class="approval-page">
    <div class="page-head">
      <div>
        <h2>待办中心</h2>
        <p>入社、退社、取消报名、社团创建与活动审核都在这里统一处理</p>
      </div>
      <div class="head-right">
        <el-tag type="info" effect="plain" round>待处理 {{ tasks.length }} 条</el-tag>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tasks" stripe border :header-cell-style="{ background: '#f7f9fc', color: '#4a5568', fontWeight: 600 }">
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" effect="light" round>{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="事项" min-width="280">
          <template #default="{ row }">
            <div class="task-title">{{ row.title }}</div>
            <div v-if="row.summary" class="task-summary">{{ row.summary }}</div>
          </template>
        </el-table-column>
        <el-table-column label="申请人" width="120">
          <template #default="{ row }">{{ row.applicantName || '—' }}</template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Check" @click="openDecide(row, true)">通过</el-button>
            <el-button type="danger" link :icon="Close" @click="openDecide(row, false)">驳回</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无待办，所有申请都已处理完毕" :image-size="90" />
        </template>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="approved ? '通过审批' : '驳回申请'"
      width="480px"
      align-center
      modal-class="app-modal-overlay"
      :close-on-click-modal="false"
    >
      <div class="dialog-task">
        <div class="task-row">
          <span class="label">事项</span>
          <span class="value">{{ currentTask ? currentTask.title : '' }}</span>
        </div>
        <div class="task-row" v-if="currentTask && currentTask.applicantName">
          <span class="label">申请人</span>
          <span class="value">{{ currentTask.applicantName }}</span>
        </div>
      </div>

      <el-input
        v-model="remark"
        type="textarea"
        :rows="4"
        maxlength="200"
        show-word-limit
        :placeholder="approved ? '可以补充说明，审批结果会同步给申请人（选填）' : '请说明驳回原因，申请人会看到这段说明'"
      />

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :type="approved ? 'primary' : 'danger'" :loading="submitting" @click="submit">
          {{ approved ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Check, Close } from '@element-plus/icons-vue'
import api from '../api'

const tasks = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const submitting = ref(false)
const approved = ref(true)
const remark = ref('')
const currentTask = ref(null)

const typeText = (t) => ({
  CLUB_CREATE: '社团创建',
  JOIN: '入社申请',
  QUIT: '退社申请',
  CANCEL_REGISTRATION: '取消报名',
  ACTIVITY_REVIEW: '活动审核'
}[t] || t)

const typeTag = (t) => ({
  CLUB_CREATE: 'primary',
  JOIN: 'info',
  QUIT: 'warning',
  CANCEL_REGISTRATION: 'info',
  ACTIVITY_REVIEW: 'success'
}[t] || 'info')

const formatTime = (t) => (t ? t.substring(0, 16).replace('T', ' ') : '—')

const load = async () => {
  loading.value = true
  try {
    const result = await api.get('/approval/pending')
    if (result.code === 200) {
      tasks.value = result.data || []
    } else {
      ElMessage.error(result.message || '加载待办失败')
    }
  } catch (e) {
    ElMessage.error('加载待办失败')
  } finally {
    loading.value = false
  }
}

const openDecide = (task, isApproved) => {
  currentTask.value = task
  approved.value = isApproved
  remark.value = ''
  dialogVisible.value = true
}

const submit = async () => {
  if (!approved.value && !remark.value.trim()) {
    ElMessage.warning('驳回需要填写原因')
    return
  }

  submitting.value = true
  try {
    const action = approved.value ? 'approve' : 'reject'
    const url = `/approval/${currentTask.value.id}/${action}?remark=${encodeURIComponent(remark.value.trim())}`
    const result = await api.put(url)
    if (result.code === 200) {
      ElMessage.success(result.message || '操作成功')
      dialogVisible.value = false
      await load()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.approval-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px 16px 40px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.page-head h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
}

.page-head p {
  margin: 0;
  font-size: 13px;
  color: #8a94a6;
}

.head-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.task-title {
  font-size: 14px;
  color: #1f2d3d;
}

.task-summary {
  margin-top: 2px;
  font-size: 12px;
  color: #8a94a6;
}

.dialog-task {
  margin-bottom: 16px;
  padding: 12px 14px;
  background: #f7f9fc;
  border-radius: 8px;
}

.task-row {
  display: flex;
  gap: 10px;
  font-size: 13px;
  line-height: 1.9;
}

.task-row .label {
  width: 52px;
  color: #8a94a6;
  flex: none;
}

.task-row .value {
  color: #1f2d3d;
}
</style>
