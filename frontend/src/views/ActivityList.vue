<template>
  <div class="app-page">
    <div class="page-head">
      <div>
        <h2>{{ tab === 'all' ? '活动浏览' : '我报名的活动' }}</h2>
        <p>{{ tab === 'all' ? '正在报名和已经结束的活动都在这里' : '含报名中与取消待审的记录' }}</p>
      </div>
      <div class="head-actions">
        <el-radio-group v-model="tab">
          <el-radio-button value="all">全部活动</el-radio-button>
          <el-radio-button value="my">我报名的</el-radio-button>
        </el-radio-group>
        <el-input
          v-if="tab === 'all'"
          v-model="keyword"
          placeholder="搜索活动名称"
          clearable
          style="width: 220px"
          @keyup.enter="doSearch"
          @clear="loadAll"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button v-if="tab === 'all'" :icon="Search" @click="doSearch">搜索</el-button>
      </div>
    </div>

    <template v-if="tab === 'all'">
      <el-row v-loading="loading" :gutter="16">
        <el-col v-for="a in activities" :key="a.id" :xs="24" :sm="12" :lg="8" class="col">
          <el-card
            shadow="hover"
            :body-style="{ padding: '0' }"
            class="act-card"
            @click="$router.push('/activity/detail/' + a.id)"
          >
            <div class="cover" :style="{ background: coverColor(a.id) }">
              <span class="cover-char">{{ (a.clubName || '活').slice(0, 1) }}</span>
              <span class="cover-tag" :class="a.status === 'ONGOING' ? 'on' : 'off'">
                {{ a.status === 'ONGOING' ? '报名中' : '已结束' }}
              </span>
              <span v-if="a.joinScope === 'MEMBER_ONLY'" class="scope-tag">仅成员</span>
            </div>

            <div class="body">
              <h3>{{ a.title }}</h3>
              <div class="meta">{{ a.clubName || '未知社团' }}</div>
              <div class="meta">{{ formatTime(a.startTime) }} · {{ a.location || '地点待定' }}</div>
              <div class="foot">
                <span class="seats">
                  {{ a.currentCount || 0 }} / {{ a.maxParticipants || '不限' }}
                </span>
                <el-tag :type="a.currentCount >= a.maxParticipants && a.maxParticipants ? 'info' : 'success'" size="small" effect="plain" round>
                  {{ a.maxParticipants && a.currentCount >= a.maxParticipants ? '已满' : '可报名' }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && activities.length === 0" description="没有找到匹配的活动" />
    </template>

    <template v-else>
      <el-row v-loading="loading" :gutter="16">
        <el-col v-for="r in myRegistrations" :key="r.id" :xs="24" :sm="12" :lg="8" class="col">
          <el-card shadow="hover" class="reg-card" @click="$router.push('/activity/detail/' + r.activityId)">
            <div class="reg-head">
              <h3>{{ r.activityTitle }}</h3>
              <el-tag :type="r.status === 'REGISTERED' ? 'success' : 'warning'" size="small" effect="light" round>
                {{ regText(r.status) }}
              </el-tag>
            </div>
            <div class="meta">报名时间：{{ formatTime(r.registeredAt) }}</div>
            <div class="reg-foot">查看活动详情 →</div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && myRegistrations.length === 0" description="还没有报名任何活动">
        <el-button type="primary" @click="tab = 'all'">去浏览活动</el-button>
      </el-empty>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api from '../api'

const tab = ref('all')
const activities = ref([])
const myRegistrations = ref([])
const keyword = ref('')
const loading = ref(false)

const palette = ['#1f2d3d', '#2c3e50', '#33415c', '#3d3b5e', '#2f4858', '#443c4a']
const coverColor = (id) => palette[(id || 0) % palette.length]

const formatTime = (t) => (t ? t.substring(5, 16).replace('T', ' ') : '—')
const regText = (s) => ({ REGISTERED: '已报名', CANCEL_PENDING: '取消待审', CANCELLED: '已取消' }[s] || s)

const loadAll = async () => {
  loading.value = true
  try {
    const result = await api.get('/activity/list')
    if (result.code === 200) {
      activities.value = (result.data || []).filter(a => a.status === 'ONGOING' || a.status === 'FINISHED')
      keyword.value = ''
    }
  } catch (e) {
    ElMessage.error('加载活动失败')
  } finally {
    loading.value = false
  }
}

const loadMine = async () => {
  loading.value = true
  try {
    const result = await api.get('/registration/my')
    if (result.code === 200) {
      myRegistrations.value = (result.data || []).filter(
        r => r.status === 'REGISTERED' || r.status === 'CANCEL_PENDING'
      )
    }
  } catch (e) {
    myRegistrations.value = []
  } finally {
    loading.value = false
  }
}

const doSearch = async () => {
  const kw = keyword.value.trim()
  if (!kw) return loadAll()
  loading.value = true
  try {
    const result = await api.get(`/activity/search?keyword=${encodeURIComponent(kw)}`)
    if (result.code === 200) {
      activities.value = (result.data || []).filter(a => a.status === 'ONGOING' || a.status === 'FINISHED')
    } else {
      ElMessage.error(result.message || '搜索失败')
    }
  } catch (e) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAll()
  loadMine()
})
</script>

<style scoped>
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

.cover {
  position: relative;
  height: 104px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-char {
  font-size: 34px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.85);
}

.cover-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  color: #ffffff;
  background: rgba(0, 0, 0, 0.42);
}

.cover-tag.off {
  background: rgba(0, 0, 0, 0.6);
}

.scope-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  color: #1a1a2e;
  background: rgba(255, 255, 255, 0.85);
}

.body {
  padding: 14px 16px 14px;
}

.body h3 {
  margin: 0 0 8px;
  font-size: 15px;
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

.reg-card {
  cursor: pointer;
  border-radius: 10px;
}

.reg-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 6px;
}

.reg-head h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 500;
  color: #1f2d3d;
}

.reg-foot {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f2f4f7;
  font-size: 12px;
  color: #1a1a2e;
}
</style>
