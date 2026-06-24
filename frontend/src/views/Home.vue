<template>
  <div>
    <div class="hero-section">
      <div class="container">
        <h1>社团活动管理系统</h1>
      </div>
    </div>

    <div class="container mt-5">
      <div class="row justify-content-center">
        <div class="col-md-3">
          <div class="card feature-card" style="cursor: pointer" @click="$router.push('/club/list')">
            <div class="icon"></div>
            <h5>社团浏览</h5>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card feature-card" style="cursor: pointer" @click="$router.push('/activity/list')">
            <div class="icon"></div>
            <h5>我的社团</h5>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card feature-card" style="cursor: pointer" @click="$router.push('/activity/list')">
            <div class="icon"></div>
            <h5>活动浏览</h5>
          </div>
        </div>
      </div>
    </div>

    <div class="container mt-5">
      <h4 class="mb-4">近期活动</h4>
      <div v-if="activities.length > 0" class="row">
        <div
          class="col-md-3 mb-3"
          v-for="a in activities.slice(0, 8)"
          :key="a.id"
          style="cursor: pointer"
          @click="$router.push(`/activity/detail/${a.id}`)"
        >
          <div class="card h-100">
            <div class="card-body">
              <h6 class="card-title">{{ a.title }}</h6>
              <p class="text-muted small mb-1">社团：{{ a.clubName || '未知' }}</p>
              <p class="text-muted small mb-1">地点：{{ a.location }}</p>
              <p class="text-muted small mb-1">时间：{{ formatTime(a.startTime) }}</p>
              <span class="badge" :class="statusClass(a.status)">{{ statusText(a.status) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">暂无活动</div>
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
import { ref, onMounted } from 'vue'
import api from '../api'

const activities = ref([])
const toast = ref({ show: false, message: '', type: 'danger' })

const loadActivities = async () => {
  try {
    const result = await api.get('/activity/list')
    if (result.code === 200) {
      // 前台只显示进行中和已结束的活动
      activities.value = (result.data || []).filter(a =>
        a.status === 'ONGOING' || a.status === 'FINISHED'
      )
    }
  } catch (e) {
    toast.value = { show: true, message: '加载失败', type: 'danger' }
  }
}

const formatTime = (time) => time ? time.substring(0, 16) : ''
const statusClass = (status) => {
  const map = { DRAFT: 'bg-secondary', ONGOING: 'bg-primary', FINISHED: 'bg-info', CANCELLED: 'bg-danger' }
  return map[status] || 'bg-secondary'
}
const statusText = (status) => {
  const map = { DRAFT: '草稿', ONGOING: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }
  return map[status] || status
}

onMounted(loadActivities)
</script>