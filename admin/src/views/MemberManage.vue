<template>
  <div class="container">
    <div class="page-header">
      <h2>成员审批</h2>
    </div>

    <!-- 待审批 -->
    <div>
      <div v-if="managedClubs.length > 0">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <span class="text-muted">共 {{ pendingTotal }} 条待审批</span>
          <button class="btn btn-sm btn-outline-primary" @click="loadManagedClubs">刷新</button>
        </div>
        <div class="card mb-3" v-for="club in managedClubs" :key="club.id">
          <div class="card-header">{{ club.name }}</div>
          <div class="card-body">
            <div v-if="club.pendingMembers && club.pendingMembers.length > 0">
              <table class="table table-hover">
                <thead><tr><th>类型</th><th>申请人</th><th>学号</th><th>申请理由</th><th>申请时间</th><th>操作</th></tr></thead>
                <tbody>
                  <tr v-for="m in club.pendingMembers" :key="m.id">
                    <td>
                      <span class="badge" :class="m.status === 'PENDING' ? 'bg-info' : 'bg-warning'">
                        {{ m.status === 'PENDING' ? '入社' : '退出' }}
                      </span>
                    </td>
                    <td>{{ m.userName }}</td>
                    <td>{{ m.studentNo }}</td>
                    <td>{{ m.applyReason || '无' }}</td>
                    <td>{{ m.createdAt }}</td>
                    <td>
                      <template v-if="m.status === 'PENDING'">
                        <button class="btn btn-sm btn-success me-1" @click="handleApprove(m.id)">通过</button>
                        <button class="btn btn-sm btn-danger" @click="handleReject(m.id)">拒绝</button>
                      </template>
                      <template v-else-if="m.status === 'QUIT_PENDING'">
                        <button class="btn btn-sm btn-warning me-1" @click="handleApproveQuit(m.id)">通过退出</button>
                        <button class="btn btn-sm btn-outline-danger" @click="handleRejectQuit(m.id)">驳回</button>
                      </template>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="text-muted">暂无待审批申请</div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="icon"></div>
        <p>暂未管理任何社团</p>
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

const managedClubs = ref([])
const pendingTotal = ref(0)
const toast = reactive({ show: false, message: '', type: 'danger' })

const showToast = (message, type) => {
  toast.show = true; toast.message = message; toast.type = type
  setTimeout(() => toast.show = false, 3000)
}

const loadManagedClubs = async () => {
  try {
    const result = await api.get('/club/my-managed')
    if (result.code === 200) {
      const clubs = result.data || []
      let total = 0
      for (const club of clubs) {
        const res = await api.get(`/club-member/pending/${club.id}`)
        club.pendingMembers = res.code === 200 ? (res.data || []) : []
        total += club.pendingMembers.length
      }
      managedClubs.value = clubs
      pendingTotal.value = total
    }
  } catch (e) { showToast('加载失败', 'danger') }
}

const handleApprove = async (id) => {
  try {
    const result = await api.put(`/club-member/approve/${id}`)
    if (result.code === 200) { showToast('已通过申请', 'success'); loadManagedClubs() }
    else showToast(result.message, 'danger')
  } catch (e) { showToast('操作失败', 'danger') }
}

const handleReject = async (id) => {
  try {
    const result = await api.put(`/club-member/reject/${id}`)
    if (result.code === 200) { showToast('已拒绝申请', 'success'); loadManagedClubs() }
    else showToast(result.message, 'danger')
  } catch (e) { showToast('操作失败', 'danger') }
}

const handleApproveQuit = async (id) => {
  try {
    const result = await api.put(`/club-member/approve-quit/${id}`)
    if (result.code === 200) { showToast('已通过退出申请', 'success'); loadManagedClubs() }
    else showToast(result.message, 'danger')
  } catch (e) { showToast('操作失败', 'danger') }
}

const handleRejectQuit = async (id) => {
  try {
    const result = await api.put(`/club-member/reject-quit/${id}`)
    if (result.code === 200) { showToast('已驳回退出申请', 'success'); loadManagedClubs() }
    else showToast(result.message, 'danger')
  } catch (e) { showToast('操作失败', 'danger') }
}

onMounted(() => { loadManagedClubs() })
</script>
