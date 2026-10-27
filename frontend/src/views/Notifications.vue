<template>
  <div class="app-page notice-page">
    <div class="page-head">
      <div>
        <h2>通知中心</h2>
        <p>查看你提交的入社申请与活动报名的处理结果</p>
      </div>
      <div class="head-actions">
        <el-radio-group v-model="tab">
          <el-radio-button value="club">入社申请</el-radio-button>
          <el-radio-button value="activity">活动报名</el-radio-button>
        </el-radio-group>
        <el-badge v-if="tab === 'club' && rejectedCount > 0" :value="rejectedCount" type="danger" class="badge">
          <span class="badge-text">被拒绝</span>
        </el-badge>
      </div>
    </div>

    <div v-loading="loading">
      <template v-if="tab === 'club'">
        <el-card v-for="app in clubApplications" :key="app.id" shadow="never" class="msg-card">
          <div class="msg-head">
            <div class="msg-left">
              <h3>{{ app.clubName || ('社团 #' + app.clubId) }}</h3>
              <p v-if="app.applyReason" class="reason">申请理由：{{ app.applyReason }}</p>
              <p class="time">申请时间：{{ formatTime(app.createdAt) }}</p>
            </div>
            <el-tag :type="statusType(app.status)" effect="light" round>{{ statusText(app.status) }}</el-tag>
          </div>
          <div class="hint" :class="hintType(app.status)">{{ clubHint(app.status) }}</div>
        </el-card>

        <el-empty v-if="!loading && clubApplications.length === 0" description="暂无入社申请记录" />
      </template>

      <template v-else>
        <el-card v-for="reg in activityRegistrations" :key="reg.id" shadow="never" class="msg-card">
          <div class="msg-head">
            <div class="msg-left">
              <h3>{{ reg.activityTitle || ('活动 #' + reg.activityId) }}</h3>
              <p class="time">报名时间：{{ formatTime(reg.registeredAt || reg.createdAt) }}</p>
              <p v-if="reg.cancelledAt" class="time">取消时间：{{ formatTime(reg.cancelledAt) }}</p>
            </div>
            <el-tag :type="statusType(reg.status)" effect="light" round>{{ statusText(reg.status) }}</el-tag>
          </div>
          <div class="hint" :class="hintType(reg.status)">{{ regHint(reg.status) }}</div>
        </el-card>

        <el-empty v-if="!loading && activityRegistrations.length === 0" description="暂无活动报名记录" />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../api'

const tab = ref('club')
const clubApplications = ref([])
const activityRegistrations = ref([])
const loading = ref(false)

const rejectedCount = computed(() => clubApplications.value.filter(a => a.status === 'REJECTED').length)

const formatTime = (t) => (t ? t.substring(0, 16).replace('T', ' ') : '—')

const statusText = (s) => ({
  PENDING: '审核中', APPROVED: '已通过', REJECTED: '已拒绝', QUIT: '已退出', QUIT_PENDING: '退出审核中',
  REGISTERED: '已报名', CANCELLED: '已取消', CANCEL_PENDING: '取消审核中'
}[s] || s)

const statusType = (s) => ({
  PENDING: 'warning', QUIT_PENDING: 'warning', CANCEL_PENDING: 'warning',
  APPROVED: 'success', REGISTERED: 'success',
  REJECTED: 'danger',
  QUIT: 'info', CANCELLED: 'info'
}[s] || 'info')

const hintType = (s) => ({
  PENDING: 'warn', QUIT_PENDING: 'warn', CANCEL_PENDING: 'warn',
  APPROVED: 'ok', REGISTERED: 'ok',
  REJECTED: 'bad'
}[s] || 'muted')

const clubHint = (s) => ({
  PENDING: '等待负责人或管理员审核，通过后你就能参加这个社团的活动了。',
  APPROVED: '申请已通过，现在可以参加社团活动了。',
  REJECTED: '申请被拒绝。如果仍有疑问，可以在社团详情页重新申请，或联系负责人。',
  QUIT: '你已退出该社团。',
  QUIT_PENDING: '退出申请已提交，等待负责人审批。'
}[s] || '')

const regHint = (s) => ({
  REGISTERED: '报名成功，记得按时参加。',
  CANCEL_PENDING: '取消申请已提交，审批通过后名额才会退回活动。',
  CANCELLED: '你已取消这次报名。'
}[s] || '')

const load = async () => {
  loading.value = true
  try {
    const [clubRes, regRes] = await Promise.all([
      api.get('/club-member/my'),
      api.get('/registration/my')
    ])
    if (clubRes.code === 200) clubApplications.value = clubRes.data || []
    if (regRes.code === 200) activityRegistrations.value = regRes.data || []
  } catch (e) {
    clubApplications.value = []
    activityRegistrations.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.notice-page {
  max-width: 900px;
}

.badge {
  margin-left: 4px;
}

.badge-text {
  font-size: 12px;
  color: #8a94a6;
}

.msg-card {
  border-radius: 10px;
  margin-bottom: 14px;
}

.msg-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.msg-left h3 {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 500;
  color: #1f2d3d;
}

.reason {
  margin: 0 0 4px;
  font-size: 13px;
  color: #4a5568;
}

.time {
  margin: 0;
  font-size: 12px;
  color: #a8abb2;
}

.hint {
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.7;
}

.hint.warn {
  background: #fdf6ec;
  color: #b88230;
}

.hint.ok {
  background: #f0f9eb;
  color: #529b2e;
}

.hint.bad {
  background: #fef0f0;
  color: #c45656;
}

.hint.muted {
  background: #f4f4f5;
  color: #8a94a6;
}
</style>
