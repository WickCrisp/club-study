<template>
  <div class="container">
    <div class="page-header">
      <h2>管理仪表盘</h2>
      <p class="text-muted">欢迎回来，{{ currentUser?.name || '管理员' }}</p>
    </div>

    <!-- 快捷入口 -->
    <div class="row mb-4">
      <div class="col-md-3 mb-3">
        <div class="card stat-card" @click="$router.push('/club/manage')" style="cursor:pointer">
          <div class="stat-number">{{ stats.clubCount }}</div>
          <div class="stat-label">社团总数</div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card stat-card" @click="$router.push('/activity/manage')" style="cursor:pointer">
          <div class="stat-number">{{ stats.activityCount }}</div>
          <div class="stat-label">活动总数</div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card stat-card" style="border-left-color: #4fc3f7">
          <div class="stat-number">{{ stats.userCount }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card stat-card" style="border-left-color: #ff9800">
          <div class="stat-number">{{ stats.pendingCount }}</div>
          <div class="stat-label">待审批申请</div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="row">
      <div class="col-md-6 mb-3">
        <div class="card h-100">
          <div class="card-header">快捷操作</div>
          <div class="card-body">
            <div class="d-grid gap-2">
              <button class="btn btn-outline-primary" @click="$router.push('/activity/create')">
                发布新活动
              </button>
              <button class="btn btn-outline-primary" @click="$router.push('/club/manage')">
                管理社团
              </button>
              <button class="btn btn-outline-warning" @click="$router.push('/member/manage')">
                审核入社申请
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-6 mb-3">
        <div class="card h-100">
          <div class="card-header">我管理的社团</div>
          <div class="card-body">
            <div v-if="myClubs.length > 0">
              <div class="d-flex justify-content-between align-items-center mb-2" v-for="club in myClubs" :key="club.id">
                <span>{{ club.name }}</span>
                <span class="badge" :class="club.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary'">
                  {{ club.status === 'ACTIVE' ? '活跃' : '已解散' }}
                </span>
              </div>
            </div>
            <div v-else class="text-muted">暂未管理任何社团</div>
          </div>
        </div>
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

const currentUser = ref(null)
const myClubs = ref([])
const stats = reactive({ clubCount: 0, activityCount: 0, userCount: 0, pendingCount: 0 })
const toast = reactive({ show: false, message: '', type: 'danger' })

const showToast = (message, type) => {
  toast.show = true; toast.message = message; toast.type = type
  setTimeout(() => toast.show = false, 3000)
}

onMounted(async () => {
  try {
    const [userRes, clubsRes, actsRes, usersRes] = await Promise.all([
      api.get('/user/current'),
      api.get('/club/list'),
      api.get('/activity/list'),
      api.get('/user/list'),
    ])
    if (userRes.code === 200) currentUser.value = userRes.data
    if (clubsRes.code === 200) stats.clubCount = clubsRes.data?.length || 0
    if (actsRes.code === 200) stats.activityCount = actsRes.data?.length || 0
    if (usersRes.code === 200) stats.userCount = usersRes.data?.length || 0

    // 加载我管理的社团
    const myClubsRes = await api.get('/club/my-managed')
    if (myClubsRes.code === 200) {
      myClubs.value = myClubsRes.data || []
      // 统计待审批数
      let pending = 0
      for (const club of myClubs.value) {
        const res = await api.get(`/club-member/pending/${club.id}`)
        if (res.code === 200) pending += (res.data || []).length
      }
      stats.pendingCount = pending
    }
  } catch (e) { showToast('加载仪表盘数据失败', 'danger') }
})
</script>
