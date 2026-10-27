<template>
  <div class="home">
    <section class="hero">
      <div class="hero-inner">
        <h1>发现社团，找到同好</h1>
        <p>浏览校内的社团与活动，报名参加，和一群有意思的人做点有意思的事</p>
        <div class="hero-actions">
          <el-button size="large" class="solid-btn" @click="$router.push('/club/list')">浏览社团</el-button>
          <el-button size="large" class="ghost-btn" @click="$router.push('/activity/list')">看看活动</el-button>
        </div>
      </div>
    </section>

    <div class="app-page">
      <el-row :gutter="16" class="quick-row">
        <el-col v-for="q in quickEntries" :key="q.path" :xs="24" :sm="8">
          <el-card shadow="hover" class="quick-card" @click="$router.push(q.path)">
            <div class="quick-icon"><el-icon><component :is="q.icon" /></el-icon></div>
            <div class="quick-text">
              <div class="quick-title">{{ q.label }}</div>
              <div class="quick-sub">{{ q.desc }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="section-head">
        <h2>近期活动</h2>
        <el-button link type="primary" @click="$router.push('/activity/list')">全部活动 →</el-button>
      </div>

      <el-row v-loading="loading" :gutter="16">
        <el-col v-for="a in activities" :key="a.id" :xs="24" :sm="12" :lg="6" class="col">
          <el-card
            shadow="hover"
            :body-style="{ padding: '0' }"
            class="act-card"
            @click="$router.push('/activity/detail/' + a.id)"
          >
            <div class="act-cover" :style="{ background: coverColor(a.id) }">
              <span class="act-char">{{ (a.clubName || '活').slice(0, 1) }}</span>
              <span class="act-status">{{ statusText(a.status) }}</span>
            </div>
            <div class="act-body">
              <h3>{{ a.title }}</h3>
              <div class="meta">{{ a.clubName || '未知社团' }}</div>
              <div class="meta">{{ formatTime(a.startTime) }}</div>
              <div class="meta">{{ a.location || '地点待定' }}</div>
              <div class="foot">
                <span class="seats">已报 {{ a.currentCount || 0 }} / {{ a.maxParticipants || '不限' }}</span>
                <span class="link">查看详情 →</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && activities.length === 0" description="还没有正在报名的活动，晚点再来看看" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { OfficeBuilding, UserFilled, Calendar } from '@element-plus/icons-vue'
import api from '../api'

const activities = ref([])
const loading = ref(false)

const quickEntries = [
  { path: '/club/list', label: '社团浏览', desc: '看看学校都有哪些社团', icon: OfficeBuilding },
  { path: '/club/my', label: '我参加的社团', desc: '查看已加入的社团与退出进度', icon: UserFilled },
  { path: '/activity/list', label: '活动浏览', desc: '挑一个感兴趣的活动报名', icon: Calendar }
]

const palette = ['#1f2d3d', '#2c3e50', '#33415c', '#3d3b5e', '#2f4858', '#443c4a']
const coverColor = (id) => palette[(id || 0) % palette.length]

const formatTime = (t) => (t ? t.substring(5, 16).replace('T', ' ') : '时间待定')
const statusText = (s) => ({ ONGOING: '报名中', FINISHED: '已结束', CANCELLED: '已取消' }[s] || s)

const load = async () => {
  loading.value = true
  try {
    const result = await api.get('/activity/list')
    if (result.code === 200) {
      activities.value = (result.data || [])
        .filter(a => a.status === 'ONGOING' || a.status === 'FINISHED')
        .slice(0, 8)
    }
  } catch (e) {
    ElMessage.error('加载活动失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.hero {
  background: #1a1a2e;
  padding: 56px 16px 60px;
}

.hero-inner {
  max-width: 1180px;
  margin: 0 auto;
}

.hero h1 {
  margin: 0 0 12px;
  font-size: 30px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 1px;
}

.hero p {
  margin: 0 0 26px;
  font-size: 14px;
  color: #a9b3c6;
}

.solid-btn {
  background: #ffffff;
  border-color: #ffffff;
  color: #1a1a2e;
  font-weight: 500;
}

.solid-btn:hover,
.solid-btn:focus {
  background: #e9edf5;
  border-color: #e9edf5;
  color: #1a1a2e;
}

.ghost-btn {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.35);
  color: #ffffff;
}

.ghost-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: #ffffff;
  color: #ffffff;
}

.quick-row {
  margin-top: -34px;
  margin-bottom: 8px;
}

.quick-card {
  margin-bottom: 16px;
  border-radius: 10px;
  cursor: pointer;
}

.quick-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
}

.quick-icon {
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

.quick-title {
  font-size: 15px;
  font-weight: 500;
  color: #1f2d3d;
}

.quick-sub {
  margin-top: 2px;
  font-size: 12px;
  color: #8a94a6;
}

.col {
  margin-bottom: 16px;
}

.act-card {
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.18s;
}

.act-card:hover {
  transform: translateY(-2px);
}

.act-cover {
  position: relative;
  height: 86px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.act-char {
  font-size: 30px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.85);
}

.act-status {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  color: #ffffff;
  background: rgba(0, 0, 0, 0.42);
}

.act-body {
  padding: 12px 14px 14px;
}

.act-body h3 {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 500;
  color: #1f2d3d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta {
  font-size: 12px;
  line-height: 1.9;
  color: #8a94a6;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f2f4f7;
}

.seats {
  font-size: 12px;
  color: #606266;
}

.link {
  font-size: 12px;
  color: #1a1a2e;
}
</style>
