<template>
  <div class="container">
    <div class="page-header">
      <h2>社团浏览</h2>
    </div>

    <ul class="nav nav-tabs mb-4">
      <li class="nav-item">
        <a class="nav-link" :class="{ active: tab === 'all' }" href="#" @click.prevent="tab = 'all'">
          本校社团
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link" :class="{ active: tab === 'my' }" href="#" @click.prevent="tab = 'my'">
          我参加的社团
        </a>
      </li>
    </ul>

    <!-- 我参加的社团 -->
    <div v-if="tab === 'my'">
      <div v-if="myClubs.length > 0" class="row">
        <div class="col-md-4 mb-3" v-for="m in myClubs" :key="m.id">
          <div class="card h-100">
            <div class="card-body">
              <h5 class="card-title">{{ m.clubName }}</h5>
              <p class="text-muted small">
                角色：<span class="badge" :class="roleBadge(m.role)">{{ roleText(m.role) }}</span>
              </p>
              <span class="badge" :class="memberStatusBadge(m.status)">
                {{ memberStatusText(m.status) }}
              </span>
              <div class="mt-3">
                <button class="btn btn-sm btn-outline-primary me-1" @click="viewClub(m.clubId)">查看详情</button>
                <button v-if="m.status === 'APPROVED'" class="btn btn-sm btn-outline-warning" @click="handleRequestQuit(m)">申请退出</button>
                <span v-if="m.status === 'QUIT_PENDING'" class="text-warning small">退出审核中</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="icon"></div>
        <p>还未加入任何社团</p>
        <button class="btn btn-primary mt-2" @click="tab = 'all'">去浏览社团</button>
      </div>
    </div>

    <!-- 本校社团 -->
    <div v-if="tab === 'all'">
      <div class="row mb-3">
        <div class="col-md-4 search-box">
          <div class="input-group">
            <input type="text" class="form-control" v-model="keyword" @keyup.enter="doSearch" placeholder="搜索社团名称..." />
            <button class="btn btn-primary" @click="doSearch">搜索</button>
          </div>
        </div>
      </div>

      <div v-if="allClubs.length > 0" class="row">
        <div class="col-md-4 mb-3" v-for="club in allClubs" :key="club.id">
          <div class="card h-100">
            <div class="card-body">
              <h5 class="card-title">{{ club.name }}</h5>
              <p class="text-muted small">{{ club.description || '暂无简介' }}</p>
              <p class="text-muted small">负责人：{{ club.leaderName || '未指定' }}</p>
              <span class="badge" :class="club.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary'">
                {{ club.status === 'ACTIVE' ? '活跃' : '已解散' }}
              </span>
              <div class="mt-3">
                <button class="btn btn-sm btn-outline-primary" @click="viewClub(club.id)">查看详情</button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="icon"></div>
        <p>暂无社团</p>
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
const tab = ref('my')
const myClubs = ref([])
const allClubs = ref([])
const keyword = ref('')
const toast = reactive({ show: false, message: '', type: 'danger' })

const showToast = (message, type) => {
  toast.show = true; toast.message = message; toast.type = type
  setTimeout(() => toast.show = false, 3000)
}

const loadMyClubs = async () => {
  try {
    const result = await api.get('/club-member/my')
    if (result.code === 200) {
      myClubs.value = (result.data || []).filter(m =>
        m.status === 'APPROVED' || m.status === 'QUIT_PENDING'
      )
    }
  } catch (e) { showToast('加载我的社团失败', 'danger') }
}

const loadAllClubs = async () => {
  try {
    const result = await api.get('/club/list')
    if (result.code === 200) allClubs.value = result.data || []
  } catch (e) { showToast('加载社团列表失败', 'danger') }
}

const doSearch = async () => {
  try {
    const url = keyword.value ? `/club/search?keyword=${encodeURIComponent(keyword.value)}` : '/club/list'
    const result = await api.get(url)
    if (result.code === 200) allClubs.value = result.data || []
  } catch (e) { showToast('搜索失败', 'danger') }
}

const viewClub = (id) => router.push(`/club/detail/${id}`)

const handleRequestQuit = async (member) => {
  if (!confirm(`确定申请退出「${member.clubName}」吗？需要管理员审批。`)) return
  try {
    const result = await api.post(`/club-member/request-quit/${member.id}`)
    if (result.code === 200) { showToast('退出申请已提交', 'success'); loadMyClubs() }
    else showToast(result.message, 'danger')
  } catch (e) { showToast('操作失败', 'danger') }
}

const roleBadge = (r) => {
  const map = { LEADER: 'bg-danger', VICE_LEADER: 'bg-warning', MEMBER: 'bg-primary' }
  return map[r] || 'bg-secondary'
}
const roleText = (r) => {
  const map = { LEADER: '负责人', VICE_LEADER: '副负责人', MEMBER: '成员' }
  return map[r] || r
}
const memberStatusBadge = (s) => {
  const map = { APPROVED: 'bg-success', QUIT_PENDING: 'bg-warning text-dark' }
  return map[s] || 'bg-secondary'
}
const memberStatusText = (s) => {
  const map = { APPROVED: '已加入', QUIT_PENDING: '退出待审' }
  return map[s] || s
}

onMounted(() => { loadMyClubs(); loadAllClubs() })
</script>
