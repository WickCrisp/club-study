<template>
  <div class="container">
    <div class="page-header">
      <h2>发布活动</h2>
    </div>

    <div class="card">
      <div class="card-body">
        <form @submit.prevent="handleSubmit">
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label">所属社团 *</label>
              <select class="form-select" v-model="form.clubId" required>
                <option value="">请选择社团</option>
                <option v-for="club in clubs" :key="club.id" :value="club.id">{{ club.name }}</option>
              </select>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label">活动标题 *</label>
              <input type="text" class="form-control" v-model="form.title" placeholder="请输入活动标题" required />
            </div>
          </div>
          <div class="mb-3">
            <label class="form-label">活动简介</label>
            <textarea class="form-control" v-model="form.description" rows="3" placeholder="请输入活动简介"></textarea>
          </div>
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label">活动地点 *</label>
              <input type="text" class="form-control" v-model="form.location" placeholder="请输入活动地点" required />
            </div>
            <div class="col-md-3 mb-3">
              <label class="form-label">人数限制</label>
              <input type="number" class="form-control" v-model="form.maxParticipants" placeholder="0表示不限" min="0" />
            </div>
            <div class="col-md-3 mb-3">
              <label class="form-label">状态</label>
              <select class="form-select" v-model="form.status">
                <option value="DRAFT">草稿</option>
              </select>
            </div>
          </div>
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label">开始时间 *</label>
              <input type="datetime-local" class="form-control" v-model="form.startTime" required />
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label">结束时间 *</label>
              <input type="datetime-local" class="form-control" v-model="form.endTime" required />
            </div>
          </div>
          <div class="d-flex gap-2">
            <button type="submit" class="btn btn-primary" :disabled="loading">
              {{ loading ? '发布中...' : '发布活动' }}
            </button>
            <button type="button" class="btn btn-outline-primary" @click="$router.push('/activity/manage')">
              返回列表
            </button>
          </div>
        </form>
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
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const clubs = ref([])
const loading = ref(false)
const toast = reactive({ show: false, message: '', type: 'danger' })

const form = reactive({
  clubId: '', title: '', description: '', location: '',
  startTime: '', endTime: '', maxParticipants: 0, status: 'DRAFT'
})

const showToast = (message, type) => {
  toast.show = true; toast.message = message; toast.type = type
  setTimeout(() => toast.show = false, 3000)
}

const loadClubs = async () => {
  try {
    const result = await api.get('/club/list')
    if (result.code === 200) clubs.value = result.data || []
  } catch (e) { showToast('加载社团列表失败', 'danger') }
}

const handleSubmit = async () => {
  loading.value = true
  try {
    const payload = {
      ...form,
      clubId: Number(form.clubId),
      maxParticipants: Number(form.maxParticipants),
      startTime: form.startTime ? form.startTime + ':00' : '',
      endTime: form.endTime ? form.endTime + ':00' : '',
    }
    const result = await api.post('/activity/create', payload)
    if (result.code === 200) {
      showToast('活动发布成功', 'success')
      setTimeout(() => router.push('/activity/manage'), 1000)
    } else {
      showToast(result.message, 'danger')
    }
  } catch (e) { showToast('操作失败', 'danger') }
  finally { loading.value = false }
}

onMounted(() => loadClubs())
</script>
