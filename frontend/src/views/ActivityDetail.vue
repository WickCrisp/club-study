<template>
  <div class="container">
    <div class="page-header">
      <h2>{{ activity.title || '活动详情' }}</h2>
    </div>

    <div v-if="activity.id" class="row">
      <div class="col-md-8">
        <div class="card mb-4">
          <div class="card-body">
            <h5>基本信息</h5>
            <p><strong>社团：</strong>{{ activity.clubName || '未知' }}</p>
            <p><strong>发布者：</strong>{{ activity.creatorName || '未知' }}</p>
            <p><strong>地点：</strong>{{ activity.location }}</p>
            <p><strong>开始时间：</strong>{{ formatTime(activity.startTime) }}</p>
            <p><strong>结束时间：</strong>{{ formatTime(activity.endTime) }}</p>
            <p><strong>人数：</strong>{{ activity.currentCount || 0 }}/{{ activity.maxParticipants || '不限' }}</p>
            <p><strong>状态：</strong>
              <span class="badge" :class="statusClass(activity.status)">{{ statusText(activity.status) }}</span>
            </p>
            <p><strong>描述：</strong>{{ activity.description || '暂无描述' }}</p>
            <div class="mt-3">
              <button v-if="!registration" class="btn btn-primary me-2" @click="handleRegister">报名参加</button>
              <button v-if="registration && registration.status === 'REGISTERED'" class="btn btn-warning me-2" @click="handleRequestCancel">申请取消报名</button>
              <button v-if="registration && registration.status === 'CANCEL_PENDING'" class="btn btn-secondary me-2" disabled>取消申请审核中...</button>
              <button class="btn btn-outline-primary" @click="$router.back()">返回</button>
            </div>
          </div>
        </div>

        <div v-if="summary" class="card mb-4">
          <div class="card-header">活动总结</div>
          <div class="card-body">
            <p><strong>内容总结：</strong>{{ summary.content }}</p>
            <p><strong>效果评估：</strong>{{ summary.evaluation }}</p>
            <p><strong>改进建议：</strong>{{ summary.suggestion }}</p>
            <p v-if="summary.images"><strong>活动图片：</strong>{{ summary.images }}</p>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card mb-4">
          <div class="card-header">报名列表</div>
          <div class="card-body">
            <div v-if="registrations.length > 0">
              <div class="mb-2" v-for="r in registrations" :key="r.id">
                <div class="d-flex justify-content-between">
                  <span>{{ r.userName || r.userId }}</span>
                  <span class="badge" :class="r.status === 'REGISTERED' ? 'bg-success' : 'bg-secondary'">
                    {{ r.status === 'REGISTERED' ? '已报名' : '已取消' }}
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="text-muted">暂无报名</div>
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
import { useRoute } from 'vue-router'
import api from '../api'

const route = useRoute()
const activity = ref({})
const registration = ref(null)
const registrations = ref([])
const summary = ref(null)
const toast = reactive({ show: false, message: '', type: 'danger' })

const showToast = (message, type) => {
  toast.show = true; toast.message = message; toast.type = type
  setTimeout(() => toast.show = false, 3000)
}

const loadActivity = async () => {
  try {
    const result = await api.get(`/activity/${route.params.id}`)
    if (result.code === 200) activity.value = result.data
  } catch (e) { showToast('加载活动失败', 'danger') }
}

const loadMyRegistration = async () => {
  try {
    const result = await api.get(`/registration/check?activityId=${route.params.id}`)
    if (result.code === 200 && result.data) {
      registration.value = result.data
    }
  } catch (e) { showToast('加载报名信息失败', 'danger') }
}

const loadRegistrations = async () => {
  try {
    const result = await api.get(`/registration/activity/${route.params.id}`)
    if (result.code === 200) registrations.value = result.data || []
  } catch (e) { showToast('加载报名列表失败', 'danger') }
}

const loadSummary = async () => {
  try {
    const result = await api.get(`/summary/activity/${route.params.id}`)
    if (result.code === 200 && result.data) summary.value = result.data
  } catch (e) { showToast('加载活动总结失败', 'danger') }
}

const handleRegister = async () => {
  try {
    const result = await api.post(`/registration/register?activityId=${route.params.id}`)
    if (result.code === 200) {
      showToast('报名成功', 'success')
      await loadMyRegistration()
      await loadRegistrations()
    } else {
      showToast(result.message, 'danger')
    }
  } catch (e) { showToast('报名失败', 'danger') }
}

const handleRequestCancel = async () => {
  if (!confirm('确定申请取消报名吗？需要管理员审批。')) return
  try {
    const result = await api.post(`/registration/request-cancel/${registration.value.id}`)
    if (result.code === 200) {
      showToast('取消申请已提交，请等待审批', 'success')
      await loadMyRegistration()
      await loadRegistrations()
    } else showToast(result.message, 'danger')
  } catch (e) { showToast('操作失败', 'danger') }
}

const handleCancel = async () => {
  if (!confirm('确定取消报名？')) return
  try {
    const result = await api.put(`/registration/cancel/${registration.value.id}`)
    if (result.code === 200) {
      showToast('已取消报名', 'success')
      registration.value = null
      await loadRegistrations()
    } else showToast(result.message, 'danger')
  } catch (e) { showToast('取消失败', 'danger') }
}

const formatTime = (time) => time ? time.substring(0, 16) : ''
const statusClass = (s) => {
  const map = { DRAFT: 'bg-secondary', ONGOING: 'bg-primary', FINISHED: 'bg-info', CANCELLED: 'bg-danger' }
  return map[s] || 'bg-secondary'
}
const statusText = (s) => {
  const map = { DRAFT: '草稿', ONGOING: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }
  return map[s] || s
}

onMounted(() => {
  loadActivity()
  loadMyRegistration()
  loadRegistrations()
  loadSummary()
})
</script>