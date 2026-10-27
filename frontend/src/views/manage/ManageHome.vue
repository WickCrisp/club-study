<template>
  <div v-loading="loading">
    <div class="page-head">
      <div>
        <h2>工作台</h2>
        <p>{{ currentClub ? currentClub.name : '我负责的社团' }} · 今天要处理的事都在这里</p>
      </div>
      <div class="head-actions">
        <el-button :icon="Refresh" @click="loadAll">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col v-for="m in metrics" :key="m.key" :xs="12" :sm="6">
        <el-card shadow="hover" class="metric" :class="{ clickable: m.to }" @click="m.to && $router.push(m.to)">
          <div class="metric-icon" :class="m.tone"><el-icon><component :is="m.icon" /></el-icon></div>
          <div>
            <div class="metric-value">{{ m.value }}</div>
            <div class="metric-label">{{ m.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-head">
              <span class="panel-title">待处理申请</span>
              <el-button link type="primary" @click="$router.push('/manage/approvals')">全部 →</el-button>
            </div>
          </template>

          <div v-if="pending.length" class="list">
            <div v-for="p in pending.slice(0, 5)" :key="p.id" class="item">
              <div class="item-left">
                <div class="item-title">{{ p.userName || '未知用户' }} · {{ p.status === 'PENDING' ? '申请加入' : '申请退出' }}</div>
                <div class="item-sub">{{ p.applyReason || '未填写理由' }} · {{ formatTime(p.createdAt) }}</div>
              </div>
              <el-tag :type="p.status === 'PENDING' ? 'primary' : 'warning'" size="small" effect="light" round>
                {{ p.status === 'PENDING' ? '入社' : '退社' }}
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="没有待处理的申请" :image-size="70" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-head">
              <span class="panel-title">最近活动</span>
              <el-button link type="primary" @click="$router.push('/manage/activities')">全部 →</el-button>
            </div>
          </template>

          <div v-if="activities.length" class="list">
            <div v-for="a in activities.slice(0, 5)" :key="a.id" class="item" @click="$router.push('/manage/activities')">
              <div class="item-left">
                <div class="item-title">{{ a.title }}</div>
                <div class="item-sub">{{ formatTime(a.startTime) }} · 报名 {{ a.currentCount || 0 }}/{{ a.maxParticipants || '不限' }}</div>
              </div>
              <el-tag :type="statusType(a.status)" size="small" effect="light" round>{{ statusText(a.status) }}</el-tag>
            </div>
          </div>
          <el-empty v-else description="还没有发布活动" :image-size="70" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Stamp, Calendar, VideoPlay, UserFilled } from '@element-plus/icons-vue'
import api from '../../api'
import { useManagedClubs } from '../../composables/useManagedClubs'

const { currentClub, currentClubId, ensure } = useManagedClubs()

const loading = ref(false)
const pending = ref([])
const activities = ref([])
const memberCount = ref(0)

const formatTime = (t) => (t ? t.substring(0, 16).replace('T', ' ') : '—')
const statusText = (s) => ({
  DRAFT: '草稿', PENDING: '待审核', REJECTED: '已驳回', ONGOING: '进行中', FINISHED: '已结束', CANCELLED: '已取消'
}[s] || s)
const statusType = (s) => ({
  DRAFT: 'info', PENDING: 'warning', REJECTED: 'danger', ONGOING: 'success', FINISHED: 'primary', CANCELLED: 'info'
}[s] || 'info')

const metrics = computed(() => [
  { key: 'pending', label: '待处理申请', value: pending.value.length, icon: Stamp, to: '/manage/approvals', tone: pending.value.length ? 'warn' : '' },
  { key: 'activity', label: '本社团活动', value: activities.value.length, icon: Calendar, to: '/manage/activities', tone: '' },
  { key: 'ongoing', label: '进行中活动', value: activities.value.filter(a => a.status === 'ONGOING').length, icon: VideoPlay, to: '/manage/activities', tone: '' },
  { key: 'member', label: '在册成员', value: memberCount.value, icon: UserFilled, to: '/manage/club', tone: '' }
])

const loadAll = async () => {
  await ensure()
  if (!currentClub.value) {
    activities.value = []
    pending.value = []
    memberCount.value = 0
    return
  }

  loading.value = true
  const clubId = currentClub.value.id
  try {
    const [pendRes, actRes, memRes] = await Promise.all([
      api.get(`/club-member/pending/${clubId}`).catch(() => ({ data: [] })),
      api.get(`/activity/club/${clubId}`).catch(() => ({ data: [] })),
      api.get(`/club-member/club/${clubId}`).catch(() => ({ data: [] }))
    ])
    pending.value = pendRes.code === 200 ? (pendRes.data || []) : []
    activities.value = (actRes.code === 200 ? (actRes.data || []) : [])
      .sort((a, b) => (b.startTime || '').localeCompare(a.startTime || ''))
    memberCount.value = (memRes.code === 200 ? (memRes.data || []) : [])
      .filter(m => m.status === 'APPROVED').length
  } catch (e) {
    ElMessage.error('加载工作台数据失败')
  } finally {
    loading.value = false
  }
}

watch(currentClubId, () => loadAll())

onMounted(loadAll)
</script>

<style scoped>
.metric {
  margin-bottom: 16px;
  border-radius: 10px;
}

.metric.clickable {
  cursor: pointer;
}

.metric :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
}

.metric-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: #eef1f6;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex: none;
}

.metric-icon.warn {
  background: #fdf6ec;
  color: #b88230;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
  color: #1f2d3d;
}

.metric-label {
  margin-top: 2px;
  font-size: 12px;
  color: #8a94a6;
}

.panel {
  border-radius: 10px;
  margin-bottom: 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-title {
  font-size: 15px;
  font-weight: 500;
  color: #1f2d3d;
}

.list {
  display: flex;
  flex-direction: column;
}

.item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 2px;
  border-bottom: 1px solid #f2f4f7;
}

.item:last-child {
  border-bottom: none;
}

.item-left {
  min-width: 0;
}

.item-title {
  font-size: 13px;
  color: #1f2d3d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-sub {
  margin-top: 2px;
  font-size: 12px;
  color: #a8abb2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
