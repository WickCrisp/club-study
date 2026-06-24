<template>
  <div class="container">
    <div class="page-header">
      <h2>🔔 通知中心</h2>
      <p class="text-muted">查看你的申请和报名状态</p>
    </div>

    <ul class="nav nav-tabs mb-4">
      <li class="nav-item">
        <a class="nav-link" :class="{ active: tab === 'club' }" href="#" @click.prevent="tab = 'club'">
          入社申请
          <span v-if="rejectedCount > 0" class="badge bg-danger ms-1">{{ rejectedCount }}</span>
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link" :class="{ active: tab === 'activity' }" href="#" @click.prevent="tab = 'activity'">
          活动报名
        </a>
      </li>
    </ul>

    <!-- 入社申请 -->
    <div v-if="tab === 'club'">
      <div v-if="clubApplications.length > 0">
        <div class="card mb-3" v-for="app in clubApplications" :key="app.id" :class="statusCardClass(app.status)">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <h5 class="card-title mb-1">{{ app.clubName }}</h5>
                <p class="text-muted small mb-1" v-if="app.applyReason">申请理由：{{ app.applyReason }}</p>
                <p class="text-muted small mb-0">申请时间：{{ app.createdAt || '-' }}</p>
              </div>
              <span class="badge fs-6" :class="statusBadgeClass(app.status)">
                {{ statusLabel(app.status) }}
              </span>
            </div>
            <div v-if="app.status === 'PENDING'" class="mt-2 text-warning small">
              等待管理员审核中...
            </div>
            <div v-else-if="app.status === 'APPROVED'" class="mt-2 text-success small">
              你的申请已通过，现在可以参与社团活动了！
            </div>
            <div v-else-if="app.status === 'REJECTED'" class="mt-2 text-danger small">
              你的申请已被拒绝，如有疑问请联系社团负责人。
            </div>
            <div v-else-if="app.status === 'QUIT'" class="mt-2 text-muted small">
              你已退出该社团
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="icon"></div>
        <p>暂无入社申请记录</p>
      </div>
    </div>

    <!-- 活动报名 -->
    <div v-if="tab === 'activity'">
      <div v-if="activityRegistrations.length > 0">
        <div class="card mb-3" v-for="reg in activityRegistrations" :key="reg.id" :class="statusCardClass(reg.status)">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <h5 class="card-title mb-1">{{ reg.activityTitle }}</h5>
                <p class="text-muted small mb-0">报名时间：{{ reg.registeredAt || reg.createdAt || '-' }}</p>
                <p class="text-muted small mb-0" v-if="reg.cancelledAt">取消时间：{{ reg.cancelledAt }}</p>
              </div>
              <span class="badge fs-6" :class="statusBadgeClass(reg.status)">
                {{ statusLabel(reg.status) }}
              </span>
            </div>
            <div v-if="reg.status === 'REGISTERED'" class="mt-2 text-success small">
              已成功报名，记得按时参加活动！
            </div>
            <div v-else-if="reg.status === 'CANCELLED'" class="mt-2 text-muted small">
              你已取消该活动的报名
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="icon"></div>
        <p>暂无活动报名记录</p>
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
import { ref, reactive, computed, onMounted } from 'vue'
import api from '../api'

const tab = ref('club')
const clubApplications = ref([])
const activityRegistrations = ref([])
const toast = reactive({ show: false, message: '', type: 'danger' })

const rejectedCount = computed(() =>
  clubApplications.value.filter(a => a.status === 'REJECTED').length
)

const showToast = (message, type) => {
  toast.show = true; toast.message = message; toast.type = type
  setTimeout(() => toast.show = false, 3000)
}

const statusLabel = (status) => {
  const map = {
    PENDING: '审核中', APPROVED: '已通过', REJECTED: '已拒绝', QUIT: '已退出', QUIT_PENDING: '退出审核中',
    REGISTERED: '已报名', CANCELLED: '已取消', CANCEL_PENDING: '取消审核中',
  }
  return map[status] || status
}

const statusBadgeClass = (status) => {
  const map = {
    PENDING: 'bg-warning text-dark',
    APPROVED: 'bg-success',
    REJECTED: 'bg-danger',
    QUIT: 'bg-secondary',
    QUIT_PENDING: 'bg-warning text-dark',
    REGISTERED: 'bg-success',
    CANCELLED: 'bg-secondary',
    CANCEL_PENDING: 'bg-warning text-dark',
  }
  return map[status] || 'bg-secondary'
}

const statusCardClass = (status) => {
  if (status === 'REJECTED') return 'border-danger'
  if (status === 'APPROVED' || status === 'REGISTERED') return 'border-success'
  if (status === 'QUIT_PENDING' || status === 'CANCEL_PENDING') return 'border-warning'
  return ''
}

const loadClubApplications = async () => {
  try {
    const result = await api.get('/club-member/my')
    if (result.code === 200) clubApplications.value = result.data || []
  } catch (e) { showToast('加载入社申请失败', 'danger') }
}

const loadActivityRegistrations = async () => {
  try {
    const result = await api.get('/registration/my')
    if (result.code === 200) activityRegistrations.value = result.data || []
  } catch (e) { showToast('加载报名记录失败', 'danger') }
}

onMounted(() => {
  loadClubApplications()
  loadActivityRegistrations()
})
</script>
