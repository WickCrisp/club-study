<template>
  <div v-loading="loading">
    <div class="page-head">
      <div>
        <h2>数据统计</h2>
        <p>{{ clubName }} · 成员规模、活动与报名情况</p>
      </div>
      <div class="head-actions">
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col v-for="m in cards" :key="m.key" :xs="12" :sm="8" :lg="6">
        <el-card shadow="never" class="metric">
          <div class="metric-icon"><el-icon><component :is="m.icon" /></el-icon></div>
          <div>
            <div class="metric-value">{{ m.value }}</div>
            <div class="metric-label">{{ m.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="panel">
          <template #header><span class="panel-title">活动状态</span></template>
          <div class="bar-row">
            <div class="bar-head">
              <span>进行中</span>
              <span>{{ stats.ongoingActivities || 0 }} / {{ stats.activities || 0 }}</span>
            </div>
            <el-progress :percentage="pct(stats.ongoingActivities, stats.activities)" :show-text="false" :stroke-width="8" color="#1a1a2e" />
          </div>
          <div class="bar-row">
            <div class="bar-head">
              <span>其他状态</span>
              <span>{{ (stats.activities || 0) - (stats.ongoingActivities || 0) }}</span>
            </div>
            <el-progress :percentage="100 - pct(stats.ongoingActivities, stats.activities)" :show-text="false" :stroke-width="8" color="#c9d1e0" />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="panel">
          <template #header><span class="panel-title">成员与报名</span></template>
          <div class="bar-row">
            <div class="bar-head">
              <span>在册成员</span>
              <span>{{ stats.members || 0 }} 人</span>
            </div>
            <el-progress :percentage="pct(stats.members, (stats.members || 0) + (stats.pendingMembers || 0))" :show-text="false" :stroke-width="8" color="#1a1a2e" />
          </div>
          <div class="bar-row">
            <div class="bar-head">
              <span>待审批</span>
              <span>{{ stats.pendingMembers || 0 }} 人</span>
            </div>
            <el-progress :percentage="pct(stats.pendingMembers, (stats.members || 0) + (stats.pendingMembers || 0))" :show-text="false" :stroke-width="8" color="#e6a23c" />
          </div>
          <div class="summary">
            <span>累计报名</span>
            <b>{{ stats.registrations || 0 }}</b> 人次
            <span class="divider">·</span>
            人均 <b>{{ avgPerMember }}</b> 次
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-alert
      class="note"
      type="info"
      :closable="false"
      show-icon
      title="统计口径"
      description="成员数与待审批按当前状态实时计算；活动数为本社团全部活动，报名数只统计状态为「已报名」的记录。"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, UserFilled, Stamp, Calendar, Tickets } from '@element-plus/icons-vue'
import api from '../../api'
import { useManagedClubs } from '../../composables/useManagedClubs'

const { currentClub, currentClubId, ensure } = useManagedClubs()

const stats = ref({})
const loading = ref(false)
const clubName = computed(() => (currentClub.value ? currentClub.value.name : '—'))

const avgPerMember = computed(() => {
  const m = Number(stats.value.members) || 0
  if (!m) return '0.0'
  return ((Number(stats.value.registrations) || 0) / m).toFixed(1)
})

const pct = (part, total) => {
  const t = Number(total) || 0
  if (!t) return 0
  return Math.min(100, Math.round((Number(part) || 0) * 100 / t))
}

const cards = computed(() => [
  { key: 'members', label: '在册成员', value: stats.value.members || 0, icon: UserFilled },
  { key: 'pending', label: '待审批', value: stats.value.pendingMembers || 0, icon: Stamp },
  { key: 'activities', label: '活动总数', value: stats.value.activities || 0, icon: Calendar },
  { key: 'registrations', label: '报名人次', value: stats.value.registrations || 0, icon: Tickets }
])

const load = async () => {
  await ensure()
  if (!currentClub.value) {
    stats.value = {}
    return
  }
  loading.value = true
  try {
    const result = await api.get(`/analytics/club/${currentClub.value.id}`)
    stats.value = result.code === 200 ? (result.data || {}) : {}
  } catch (e) {
    stats.value = {}
    ElMessage.error('加载统计失败')
  } finally {
    loading.value = false
  }
}

watch(currentClubId, load)
onMounted(load)
</script>

<style scoped>
.metric {
  margin-bottom: 16px;
  border-radius: 10px;
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

.panel-title {
  font-size: 15px;
  font-weight: 500;
  color: #1f2d3d;
}

.bar-row {
  margin-bottom: 16px;
}

.bar-row:last-child {
  margin-bottom: 0;
}

.summary {
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px solid #f2f4f7;
  font-size: 12px;
  color: #8a94a6;
}

.summary b {
  color: #1f2d3d;
  font-weight: 500;
  margin: 0 2px;
}

.divider {
  margin: 0 6px;
  color: #dcdfe6;
}

.bar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 12px;
  color: #4a5568;
}

.note {
  border-radius: 8px;
}
</style>
