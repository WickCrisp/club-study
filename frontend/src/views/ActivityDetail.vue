<template>
  <div class="app-page">
    <el-button link :icon="Back" class="back" @click="$router.back()">返回</el-button>

    <el-row v-if="activity.id" :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="panel">
          <div class="title-row">
            <h1>{{ activity.title }}</h1>
            <el-tag :type="statusType(activity.status)" effect="light" round>{{ statusText(activity.status) }}</el-tag>
          </div>
          <p class="sub">
            {{ activity.clubName || '未知社团' }} · 由 {{ activity.creatorName || '未知' }} 发布
          </p>

          <el-descriptions :column="2" border class="info">
            <el-descriptions-item label="开始时间">{{ formatTime(activity.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ formatTime(activity.endTime) }}</el-descriptions-item>
            <el-descriptions-item label="活动地点">{{ activity.location || '待定' }}</el-descriptions-item>
            <el-descriptions-item label="报名情况">
              {{ activity.currentCount || 0 }} / {{ activity.maxParticipants || '不限' }}
            </el-descriptions-item>
            <el-descriptions-item label="参与范围" :span="2">
              <el-tag :type="activity.joinScope === 'MEMBER_ONLY' ? 'warning' : 'info'" size="small" effect="plain" round>
                {{ activity.joinScope === 'MEMBER_ONLY' ? '本社团专属（仅成员可报名）' : '公开活动（所有人可报名）' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <div class="desc-title">活动介绍</div>
          <p class="desc">{{ activity.description || '主办方还没有填写活动介绍。' }}</p>

          <div class="actions">
            <el-button
              v-if="!registration"
              type="primary"
              size="large"
              :disabled="isFull || activity.status !== 'ONGOING'"
              :loading="acting"
              @click="handleRegister"
            >
              {{ isFull ? '名额已满' : (activity.status !== 'ONGOING' ? '当前不可报名' : '报名参加') }}
            </el-button>

            <el-button
              v-else-if="registration.status === 'REGISTERED'"
              type="warning"
              size="large"
              @click="handleRequestCancel"
            >申请取消报名</el-button>

            <el-button v-else-if="registration.status === 'CANCEL_PENDING'" size="large" disabled>
              取消申请审核中…
            </el-button>

            <el-button size="large" @click="$router.push('/activity/list')">看更多活动</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="side-head">
              <span class="side-title">报名名单</span>
              <span class="side-sub">{{ registrationsLocked ? '—' : registrations.length + ' 人' }}</span>
            </div>
          </template>

          <div v-if="registrationsLocked" class="locked-box">
            <el-icon><Lock /></el-icon>
            <p>报名名单仅登录可见</p>
            <el-button type="primary" size="small" @click="$router.push('/login')">去登录</el-button>
          </div>
          <div v-else-if="registrations.length" class="reg-list">
            <div v-for="r in registrations" :key="r.id" class="reg-item">
              <div class="reg-left">
                <span class="dot" :class="r.status === 'REGISTERED' ? 'on' : 'off'"></span>
                <span class="reg-name">{{ r.userName || ('用户 #' + r.userId) }}</span>
              </div>
              <span class="reg-status">{{ r.status === 'REGISTERED' ? '已报名' : (r.status === 'CANCEL_PENDING' ? '取消待审' : '已取消') }}</span>
            </div>
          </div>
          <el-empty v-else description="还没有人报名" :image-size="70" />
        </el-card>
      </el-col>
    </el-row>

    <el-skeleton v-else :rows="6" animated />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, Lock } from '@element-plus/icons-vue'
import api from '../api'
import { getToken } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const activity = ref({})
const registration = ref(null)
const registrations = ref([])
const registrationsLocked = ref(false)
const acting = ref(false)

const isFull = computed(() => {
  const max = activity.value.maxParticipants || 0
  return max > 0 && (activity.value.currentCount || 0) >= max
})

const formatTime = (t) => (t ? t.substring(5, 16).replace('T', ' ') : '—')
const statusText = (s) => ({
  DRAFT: '草稿', PENDING: '待审核', REJECTED: '已驳回',
  ONGOING: '报名中', FINISHED: '已结束', CANCELLED: '已取消'
}[s] || s)
const statusType = (s) => ({
  PENDING: 'warning', REJECTED: 'danger', ONGOING: 'success', FINISHED: 'info', CANCELLED: 'info'
}[s] || 'info')

const loadActivity = async () => {
  try {
    const result = await api.get(`/activity/${route.params.id}`)
    if (result.code === 200) activity.value = result.data || {}
  } catch (e) {
    ElMessage.error('加载活动失败')
  }
}

const loadMyRegistration = async () => {
  try {
    const result = await api.get(`/registration/check?activityId=${route.params.id}`, { skipAuthRedirect: true })
    if (result.code === 200 && result.data) registration.value = result.data
  } catch (e) {
    registration.value = null
  }
}

const loadRegistrations = async () => {
  // 报名名单含真实姓名，匿名不发请求、直接显示占位
  if (!getToken()) {
    registrationsLocked.value = true
    return
  }
  try {
    const result = await api.get(`/registration/activity/${route.params.id}`, { skipAuthRedirect: true })
    if (result.code === 200) {
      registrations.value = result.data || []
      registrationsLocked.value = false
    }
  } catch (e) {
    // 令牌过期等情形按未登录处理，避免整页跳转打断匿名浏览
    registrationsLocked.value = true
    registrations.value = []
  }
}

const handleRegister = async () => {
  acting.value = true
  try {
    const result = await api.post(`/registration/register?activityId=${route.params.id}`)
    if (result.code === 200) {
      ElMessage.success('报名成功')
      await Promise.all([loadActivity(), loadMyRegistration(), loadRegistrations()])
    } else {
      ElMessage.error(result.message || '报名失败')
    }
  } catch (e) {
    ElMessage.error('报名失败')
  } finally {
    acting.value = false
  }
}

const handleRequestCancel = async () => {
  try {
    await ElMessageBox.confirm(
      '申请取消后需要管理员审批，审批通过名额才会退回。确定提交吗？',
      '申请取消报名',
      { type: 'warning', confirmButtonText: '提交申请', cancelButtonText: '再想想' }
    )
  } catch (e) {
    return
  }

  try {
    const result = await api.post(`/registration/request-cancel/${registration.value.id}`)
    if (result.code === 200) {
      ElMessage.success('取消申请已提交，请等待审批')
      await Promise.all([loadMyRegistration(), loadRegistrations()])
    } else {
      ElMessage.error(result.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交失败')
  }
}

onMounted(() => {
  loadActivity()
  loadMyRegistration()
  loadRegistrations()
})
</script>

<style scoped>
.back {
  margin-bottom: 12px;
  color: #4a5568;
}

.panel {
  border-radius: 10px;
  margin-bottom: 16px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.title-row h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
}

.sub {
  margin: 8px 0 18px;
  font-size: 13px;
  color: #8a94a6;
}

.info {
  margin-bottom: 20px;
}

.desc-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #1f2d3d;
}

.desc {
  margin: 0 0 24px;
  font-size: 13px;
  line-height: 1.9;
  color: #4a5568;
  white-space: pre-wrap;
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.side-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.side-title {
  font-size: 15px;
  font-weight: 500;
  color: #1f2d3d;
}

.side-sub {
  font-size: 12px;
  color: #8a94a6;
}

.reg-list {
  max-height: 420px;
  overflow-y: auto;
}

/* 未登录时的名单占位：说明原因并给出登录入口 */
.locked-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 34px 0 26px;
}

.locked-box .el-icon {
  font-size: 26px;
  color: #a8abb2;
}

.locked-box p {
  margin: 0;
  font-size: 13px;
  color: #8a94a6;
}

.reg-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 2px;
  border-bottom: 1px solid #f2f4f7;
}

.reg-item:last-child {
  border-bottom: none;
}

.reg-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.dot.on {
  background: #67c23a;
}

.dot.off {
  background: #c0c4cc;
}

.reg-name {
  font-size: 13px;
  color: #1f2d3d;
}

.reg-status {
  font-size: 12px;
  color: #8a94a6;
}
</style>
