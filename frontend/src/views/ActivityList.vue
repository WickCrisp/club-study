<template>
  <div class="container">
    <div class="page-header">
      <h2>活动浏览</h2>
    </div>

    <ul class="nav nav-tabs mb-4">
      <li class="nav-item">
        <a class="nav-link" :class="{ active: tab === 'all' }" href="#" @click.prevent="tab = 'all'">
          本校活动
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link" :class="{ active: tab === 'my' }" href="#" @click.prevent="tab = 'my'">
          我报名的活动
        </a>
      </li>
    </ul>

    <!-- 我报名的活动 -->
    <div v-if="tab === 'my'">
      <div v-if="myActivities.length > 0" class="row">
        <div class="col-md-3 mb-3" v-for="r in myActivities" :key="r.id">
          <div class="card h-100" style="cursor: pointer" @click="$router.push(`/activity/detail/${r.activityId}`)">
            <div class="card-body">
              <h6 class="card-title">{{ r.activityTitle }}</h6>
              <p class="text-muted small mb-1">报名时间：{{ r.registeredAt || '-' }}</p>
              <span class="badge" :class="regStatusBadge(r.status)">{{ regStatusText(r.status) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="icon"></div>
        <p>还未报名任何活动</p>
        <button class="btn btn-primary mt-2" @click="tab = 'all'">去浏览活动</button>
      </div>
    </div>

    <!-- 本校活动 -->
    <div v-if="tab === 'all'">
      <div class="row mb-3">
        <div class="col-md-4 search-box">
          <div class="input-group">
            <input type="text" class="form-control" v-model="keyword" @keyup.enter="doSearch" placeholder="搜索活动..." />
            <button class="btn btn-primary" @click="doSearch">搜索</button>
          </div>
        </div>
      </div>

      <div v-if="allActivities.length > 0" class="row">
        <div class="col-md-3 mb-3" v-for="a in allActivities" :key="a.id">
          <div class="card h-100" style="cursor: pointer" @click="$router.push(`/activity/detail/${a.id}`)">
            <div class="card-body">
              <h6 class="card-title">{{ a.title }}</h6>
              <p class="text-muted small mb-1">社团：{{ a.clubName || '未知' }}</p>
              <p class="text-muted small mb-1">地点：{{ a.location }}</p>
              <p class="text-muted small mb-1">时间：{{ formatTime(a.startTime) }}</p>
              <p class="text-muted small mb-1">
                人数：{{ a.currentCount || 0 }}/{{ a.maxParticipants || '不限' }}
              </p>
              <span class="badge" :class="statusClass(a.status)">{{ statusText(a.status) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="icon"></div>
        <p>暂无活动</p>
      </div>
    </div>

    <div class="toast-container">
      <div v-if="toast.show" class="alert alert-dismissible fade show" :class="'alert-' + toast.type">
        {{ toast.message }}
        <button type="button" class="btn-close" @click="toast.show = false"></button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'

const tab = ref('my')
const myActivities = ref([])
const allActivities = ref([])
const keyword = ref('')
const toast = reactive({ show: false, message: '', type: 'danger' })

const showToast = (message, type) => {
  toast.show = true; toast.message = message; toast.type = type
  setTimeout(() => toast.show = false, 3000)
}

const loadMyActivities = async () => {
  try {
    const result = await api.get('/registration/my')
    if (result.code === 200) {
      myActivities.value = (result.data || []).filter(r =>
        r.status === 'REGISTERED' || r.status === 'CANCEL_PENDING'
      )
    }
  } catch (e) { showToast('加载我的活动失败', 'danger') }
}

const loadAllActivities = async () => {
  try {
    const result = await api.get('/activity/list')
    if (result.code === 200) {
      allActivities.value = (result.data || []).filter(a =>
        a.status === 'ONGOING' || a.status === 'FINISHED'
      )
    }
  } catch (e) { showToast('加载活动失败', 'danger') }
}

const doSearch = async () => {
  const url = keyword.value ? `/activity/search?keyword=${encodeURIComponent(keyword.value)}` : '/activity/list'
  try {
    const result = await api.get(url)
    if (result.code === 200) {
      allActivities.value = (result.data || []).filter(a =>
        a.status === 'ONGOING' || a.status === 'FINISHED'
      )
    }
  } catch (e) { showToast('搜索失败', 'danger') }
}

const formatTime = (time) => time ? time.substring(0, 16) : ''
const statusClass = (s) => {
  const map = { ONGOING: 'bg-primary', FINISHED: 'bg-info', CANCELLED: 'bg-danger' }
  return map[s] || 'bg-secondary'
}
const statusText = (s) => {
  const map = { ONGOING: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }
  return map[s] || s
}
const regStatusBadge = (s) => {
  const map = { REGISTERED: 'bg-success', CANCEL_PENDING: 'bg-warning text-dark', CANCELLED: 'bg-secondary' }
  return map[s] || 'bg-secondary'
}
const regStatusText = (s) => {
  const map = { REGISTERED: '已报名', CANCEL_PENDING: '取消待审', CANCELLED: '已取消' }
  return map[s] || s
}

onMounted(() => { loadMyActivities(); loadAllActivities() })
</script>
