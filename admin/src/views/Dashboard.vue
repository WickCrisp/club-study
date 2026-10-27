<template>
  <div class="dashboard" v-loading="loading">
    <div class="page-head">
      <div>
        <h2>仪表盘</h2>
        <p>欢迎回来，{{ currentUser ? currentUser.name : '管理员' }}，这里是系统当前的运行概况</p>
      </div>
      <el-button :icon="Refresh" @click="loadAll">刷新</el-button>
    </div>

    <el-row :gutter="16" class="stat-row">
      <el-col v-for="item in statCards" :key="item.key" :xs="12" :sm="8" :lg="6">
        <el-card
          shadow="hover"
          class="metric-card"
          :class="{ clickable: item.to }"
          @click="item.to && $router.push(item.to)"
        >
          <div class="stat-icon" :class="item.tone">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
          <el-tag v-if="item.key === 'pending' && item.value > 0" type="warning" size="small" effect="light" class="stat-tip">
            待处理
          </el-tag>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-head">
              <span class="panel-title">快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" :icon="EditPen" @click="$router.push('/activity/create')">发布新活动</el-button>
            <el-button :icon="OfficeBuilding" @click="$router.push('/club/manage')">管理社团</el-button>
            <el-button :icon="Stamp" @click="$router.push('/approval')">处理待办</el-button>
            <el-button :icon="UserFilled" @click="$router.push('/member/manage')">审核入社</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-head">
              <span class="panel-title">我管理的社团</span>
              <span class="panel-sub">{{ myClubs.length }} 个</span>
            </div>
          </template>

          <div v-if="myClubs.length" class="club-list">
            <div v-for="club in myClubs" :key="club.id" class="club-item" @click="$router.push('/club/manage')">
              <div class="club-left">
                <span class="club-dot" :class="club.status === 'ACTIVE' ? 'on' : 'off'"></span>
                <span class="club-name">{{ club.name }}</span>
              </div>
              <el-tag :type="club.status === 'ACTIVE' ? 'success' : 'info'" size="small" effect="plain" round>
                {{ club.status === 'ACTIVE' ? '活跃' : '已解散' }}
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="暂未管理任何社团" :image-size="70" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh, OfficeBuilding, Calendar, User, Stamp, Document, Picture, EditPen, UserFilled
} from '@element-plus/icons-vue'
import api from '../api'

const currentUser = ref(null)
const myClubs = ref([])
const loading = ref(false)

const stats = ref({
  clubCount: 0,
  activityCount: 0,
  userCount: 0,
  pendingCount: 0,
  announcementCount: 0,
  showcaseCount: 0
})

const statCards = computed(() => [
  { key: 'club', label: '社团总数', value: stats.value.clubCount, icon: OfficeBuilding, to: '/club/manage', tone: 'tone-dark' },
  { key: 'activity', label: '活动总数', value: stats.value.activityCount, icon: Calendar, to: '/activity/manage', tone: 'tone-dark' },
  { key: 'user', label: '用户总数', value: stats.value.userCount, icon: User, to: '/user/manage', tone: 'tone-dark' },
  { key: 'pending', label: '待审批申请', value: stats.value.pendingCount, icon: Stamp, to: '/approval', tone: 'tone-warn' },
  { key: 'announcement', label: '已发布公告', value: stats.value.announcementCount, icon: Document, to: '/announcement/manage', tone: 'tone-dark' },
  { key: 'showcase', label: '社团风采', value: stats.value.showcaseCount, icon: Picture, to: '', tone: 'tone-dark' }
])

const loadAll = async () => {
  loading.value = true
  try {
    const [userRes, globalRes, countRes, myClubsRes] = await Promise.all([
      api.get('/user/current', { skipAuthRedirect: true }),
      api.get('/analytics/global'),
      api.get('/approval/count'),
      api.get('/club/my-managed')
    ])

    if (userRes.code === 200) currentUser.value = userRes.data

    if (globalRes.code === 200) {
      const g = globalRes.data || {}
      stats.value = {
        ...stats.value,
        clubCount: g.clubs || 0,
        activityCount: g.activities || 0,
        userCount: g.users || 0,
        announcementCount: g.announcements || 0,
        showcaseCount: g.showcases || 0
      }
    }

    if (countRes.code === 200) stats.value.pendingCount = countRes.data || 0
    if (myClubsRes.code === 200) myClubs.value = myClubsRes.data || []
  } catch (e) {
    ElMessage.error('加载仪表盘数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.dashboard {
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

.stat-row {
  margin-bottom: 4px;
}

.metric-card {
  position: relative;
  margin-bottom: 16px;
  border-radius: 10px;
}

.metric-card.clickable {
  cursor: pointer;
}

.metric-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
}

.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex: none;
}

.tone-dark {
  background: #eef1f6;
  color: #1a1a2e;
}

.tone-warn {
  background: #fdf6ec;
  color: #b88230;
}

.stat-body {
  min-width: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
  color: #1f2d3d;
}

.stat-label {
  margin-top: 2px;
  font-size: 12px;
  color: #8a94a6;
}

.stat-tip {
  position: absolute;
  top: 12px;
  right: 12px;
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

.panel-sub {
  font-size: 12px;
  color: #8a94a6;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.quick-actions .el-button {
  margin-left: 0;
  width: 100%;
}

.club-list {
  display: flex;
  flex-direction: column;
}

.club-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 4px;
  border-bottom: 1px solid #f2f4f7;
  cursor: pointer;
}

.club-item:last-child {
  border-bottom: none;
}

.club-item:hover {
  background: #fafbfc;
}

.club-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.club-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex: none;
}

.club-dot.on {
  background: #67c23a;
}

.club-dot.off {
  background: #c0c4cc;
}

.club-name {
  font-size: 14px;
  color: #1f2d3d;
}
</style>
