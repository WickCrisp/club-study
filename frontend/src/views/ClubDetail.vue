<template>
  <div class="container">
    <div class="page-header">
      <h2>{{ club.name || '社团详情' }}</h2>
    </div>

    <div v-if="club.id" class="row">
      <div class="col-md-8">
        <div class="card mb-4">
          <div class="card-body">
            <h5>基本信息</h5>
            <p><strong>简介：</strong>{{ club.description || '暂无简介' }}</p>
            <p><strong>负责人：</strong>{{ club.leaderName || '未指定' }}</p>
            <p><strong>状态：</strong>
              <span class="badge" :class="club.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary'">
                {{ club.status === 'ACTIVE' ? '活跃' : '已解散' }}
              </span>
            </p>
            <button v-if="!myMember" class="btn btn-primary me-2" @click="handleApply">申请加入</button>
            <button v-if="myMember && myMember.status === 'APPROVED'" class="btn btn-warning me-2" @click="handleRequestQuit">申请退出社团</button>
            <span v-if="myMember && myMember.status === 'PENDING'" class="text-warning me-2">入社申请审核中…</span>
            <span v-if="myMember && myMember.status === 'REJECTED'" class="text-danger me-2">入社申请已被拒绝，可重新申请</span>
            <span v-if="myMember && myMember.status === 'QUIT_PENDING'" class="text-warning me-2">退出申请审核中…</span>
            <button class="btn btn-outline-primary" @click="goBack">返回</button>
          </div>
        </div>

        <div class="card mb-4">
          <div class="card-header d-flex justify-content-between align-items-center">
            <span>社团活动</span>
          </div>
          <div class="card-body">
            <div v-if="activities.length > 0">
              <div class="mb-2" v-for="a in activities" :key="a.id">
                <div class="card p-3" style="cursor: pointer" @click="$router.push(`/activity/detail/${a.id}`)">
                  <div class="d-flex justify-content-between">
                    <div>
                      <strong>{{ a.title }}</strong>
                      <p class="text-muted small mb-0">{{ formatTime(a.startTime) }} | {{ a.location }}</p>
                    </div>
                    <span class="badge" :class="activityStatusClass(a.status)">{{ activityStatusText(a.status) }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="text-muted">暂无活动</div>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card mb-4">
          <div class="card-header">成员列表</div>
          <div class="card-body">
            <div v-if="members.length > 0">
              <div class="mb-2" v-for="m in members" :key="m.id">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <strong>{{ m.userName }}</strong>
                    <span class="text-muted small ms-2">{{ m.studentNo }}</span>
                  </div>
                  <span class="badge" :class="roleBadge(m.role)">
                    {{ roleText(m.role) }}
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="text-muted">暂无成员</div>
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
import { useRoute, useRouter } from 'vue-router'
import api from '../api'

const route = useRoute()
const router = useRouter()
const club = ref({})
const activities = ref([])
const members = ref([])
const myMember = ref(null)
const toast = reactive({ show: false, message: '', type: 'danger' })

const showToast = (message, type) => {
  toast.show = true; toast.message = message; toast.type = type
  setTimeout(() => toast.show = false, 3000)
}

const loadClub = async () => {
  try {
    const result = await api.get(`/club/${route.params.id}`)
    if (result.code === 200) club.value = result.data
  } catch (e) { showToast('加载失败', 'danger') }
}

const loadActivities = async () => {
  try {
    const result = await api.get(`/activity/club/${route.params.id}`)
    if (result.code === 200) activities.value = result.data || []
  } catch (e) { showToast('加载活动列表失败', 'danger') }
}

const loadMembers = async () => {
  try {
    const result = await api.get(`/club-member/club/${route.params.id}`)
    if (result.code === 200) members.value = result.data || []
  } catch (e) { showToast('加载成员列表失败', 'danger') }
}

const loadMyMember = async () => {
  try {
    const result = await api.get('/club-member/my')
    if (result.code === 200) {
      myMember.value = (result.data || []).find(m => m.clubId === Number(route.params.id)) || null
    }
  } catch (e) {}
}

const handleApply = async () => {
  try {
    const result = await api.post(`/club-member/apply?clubId=${route.params.id}`)
    if (result.code === 200) { showToast('申请已提交，请等待审批', 'success'); await loadMyMember() }
    else showToast(result.message || '申请失败', 'danger')
  } catch (e) { showToast('申请失败，请先登录', 'danger') }
}

const handleRequestQuit = async () => {
  if (!confirm('确定申请退出该社团吗？需要管理员审批。')) return
  try {
    const result = await api.post(`/club-member/request-quit/${myMember.value.id}`)
    if (result.code === 200) { showToast('退出申请已提交', 'success'); await loadMyMember() }
    else showToast(result.message, 'danger')
  } catch (e) { showToast('操作失败', 'danger') }
}

const goBack = () => router.back()

const formatTime = (time) => time ? time.substring(0, 16) : ''
const activityStatusClass = (s) => {
  const map = { DRAFT: 'bg-secondary', ONGOING: 'bg-primary', FINISHED: 'bg-info', CANCELLED: 'bg-danger' }
  return map[s] || 'bg-secondary'
}
const activityStatusText = (s) => {
  const map = { DRAFT: '草稿', ONGOING: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }
  return map[s] || s
}
const roleBadge = (r) => {
  const map = { LEADER: 'bg-danger', VICE_LEADER: 'bg-warning', MEMBER: 'bg-primary' }
  return map[r] || 'bg-secondary'
}
const roleText = (r) => {
  const map = { LEADER: '负责人', VICE_LEADER: '副负责人', MEMBER: '成员' }
  return map[r] || r
}

onMounted(() => { loadClub(); loadActivities(); loadMembers(); loadMyMember() })
</script>