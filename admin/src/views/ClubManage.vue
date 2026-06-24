<template>
  <div class="container">
    <div class="page-header d-flex justify-content-between align-items-center">
      <h2>社团管理</h2>
      <button class="btn btn-primary" @click="showCreateModal">+ 创建社团</button>
    </div>

    <div class="row mb-3">
      <div class="col-md-4 search-box">
        <div class="input-group">
          <input type="text" class="form-control" v-model="keyword" @keyup.enter="doSearch" placeholder="搜索社团名称..." />
          <button class="btn btn-primary" @click="doSearch">搜索</button>
        </div>
      </div>
    </div>

    <div v-if="clubs.length > 0" class="row">
      <div class="col-md-4 mb-3" v-for="club in clubs" :key="club.id">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">{{ club.name }}</h5>
            <p class="text-muted small">{{ club.description || '暂无简介' }}</p>
            <p class="text-muted small">负责人：{{ club.leaderName || '未指定' }}</p>
            <span class="badge" :class="club.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary'">
              {{ club.status === 'ACTIVE' ? '活跃' : '已解散' }}
            </span>
            <div class="mt-3 action-buttons">
              <button class="btn btn-sm btn-outline-info me-1" @click="toggleDetail(club)">详情</button>
              <button class="btn btn-sm btn-outline-warning me-1" @click="editClub(club)">编辑</button>
              <button class="btn btn-sm btn-outline-danger" @click="confirmDelete(club.id)">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 社团详情展开 -->
    <div v-if="detailClub" class="card mb-4 fade-slide-up">
      <div class="card-header d-flex justify-content-between align-items-center">
        <span>{{ detailClub.name }} — 详细信息</span>
        <button class="btn btn-sm btn-outline-secondary" @click="detailClub = null">关闭</button>
      </div>
      <div class="card-body">
        <div class="row mb-3">
          <div class="col-md-3"><strong>社团名称：</strong>{{ detailClub.name }}</div>
          <div class="col-md-3"><strong>负责人：</strong>{{ detailClub.leaderName || '未指定' }}</div>
          <div class="col-md-3"><strong>状态：</strong>{{ detailClub.status === 'ACTIVE' ? '活跃' : '已解散' }}</div>
          <div class="col-md-3"><strong>成员数：</strong>{{ detailMembers.length }}</div>
        </div>
        <p><strong>简介：</strong>{{ detailClub.description || '暂无简介' }}</p>
        <h6 class="mt-3">成员列表</h6>
        <div v-if="detailMembers.length > 0">
          <table class="table table-sm">
            <thead><tr><th>姓名</th><th>学号</th><th>角色</th><th>状态</th><th>加入时间</th></tr></thead>
            <tbody>
              <tr v-for="m in detailMembers" :key="m.id">
                <td>{{ m.userName }}</td>
                <td>{{ m.studentNo }}</td>
                <td>{{ roleText(m.role) }}</td>
                <td><span class="badge" :class="statusBadge(m.status)">{{ statusLabel(m.status) }}</span></td>
                <td>{{ m.joinedAt || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="text-muted">暂无成员</div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="icon"></div>
      <p>暂无社团，点击上方按钮创建</p>
    </div>

    <!-- 创建/编辑模态框 -->
    <div class="modal fade" ref="modalRef" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ isEdit ? '编辑社团' : '创建社团' }}</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">社团名称</label>
              <input type="text" class="form-control" v-model="form.name" placeholder="请输入社团名称" />
            </div>
            <div class="mb-3">
              <label class="form-label">社团简介</label>
              <textarea class="form-control" v-model="form.description" rows="3" placeholder="请输入简介"></textarea>
            </div>
            <div class="mb-3">
              <label class="form-label">负责人学号</label>
              <input type="text" class="form-control" v-model="form.leaderStudentNo" placeholder="请输入负责人的学号" />
            </div>
            <div class="mb-3">
              <label class="form-label">状态</label>
              <select class="form-select" v-model="form.status">
                <option value="ACTIVE">活跃</option>
                <option value="DISBANDED">已解散</option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" @click="handleSubmit" :disabled="loading">
              {{ loading ? '保存中...' : '保存' }}
            </button>
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

const clubs = ref([])
const keyword = ref('')
const loading = ref(false)
const isEdit = ref(false)
const modalRef = ref(null)
const toast = reactive({ show: false, message: '', type: 'danger' })
const detailClub = ref(null)
const detailMembers = ref([])
let modalInstance = null

const form = reactive({ id: null, name: '', description: '', leaderStudentNo: '', status: 'ACTIVE' })

const showToast = (message, type) => {
  toast.show = true; toast.message = message; toast.type = type
  setTimeout(() => toast.show = false, 3000)
}

const loadClubs = async () => {
  try {
    const result = await api.get('/club/list')
    if (result.code === 200) clubs.value = result.data || []
  } catch (e) { showToast('加载失败', 'danger') }
}

const doSearch = async () => {
  try {
    const url = keyword.value ? `/club/search?keyword=${encodeURIComponent(keyword.value)}` : '/club/list'
    const result = await api.get(url)
    if (result.code === 200) clubs.value = result.data || []
  } catch (e) { showToast('搜索失败', 'danger') }
}

const showCreateModal = () => {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', description: '', leaderStudentNo: '', status: 'ACTIVE' })
  modalInstance.show()
}

const editClub = (club) => {
  isEdit.value = true
  Object.assign(form, { id: club.id, name: club.name, description: club.description || '', leaderStudentNo: '', status: club.status || 'ACTIVE' })
  modalInstance.show()
}

const handleSubmit = async () => {
  loading.value = true
  try {
    const payload = { ...form }
    const result = isEdit.value
      ? await api.put('/club/update', payload)
      : await api.post('/club/create', payload)
    if (result.code === 200) {
      showToast(isEdit.value ? '更新成功' : '创建成功', 'success')
      modalInstance.hide()
      loadClubs()
    } else {
      showToast(result.message, 'danger')
    }
  } catch (e) { showToast('操作失败', 'danger') }
  finally { loading.value = false }
}

const toggleDetail = async (club) => {
  if (detailClub.value?.id === club.id) { detailClub.value = null; detailMembers.value = []; return }
  detailClub.value = club
  try {
    const res = await api.get(`/club-member/club/${club.id}`)
    detailMembers.value = res.code === 200 ? (res.data || []) : []
  } catch (e) { detailMembers.value = [] }
}

const statusLabel = (s) => ({ PENDING:'待审核', APPROVED:'已通过', REJECTED:'已拒绝', QUIT:'已退出', QUIT_PENDING:'退出待审' }[s]||s)
const statusBadge = (s) => ({ APPROVED:'bg-success', PENDING:'bg-warning text-dark', QUIT_PENDING:'bg-warning text-dark', REJECTED:'bg-danger', QUIT:'bg-secondary' }[s]||'bg-secondary')
const roleText = (r) => ({ LEADER:'负责人', VICE_LEADER:'副负责人', MEMBER:'成员' }[r]||r)

const confirmDelete = async (id) => {
  if (!confirm('确定删除该社团吗？此操作不可撤销。')) return
  try {
    const result = await api.delete(`/club/${id}`)
    if (result.code === 200) { showToast('删除成功', 'success'); loadClubs() }
    else showToast(result.message, 'danger')
  } catch (e) { showToast('删除失败', 'danger') }
}

onMounted(() => {
  loadClubs()
  modalInstance = new bootstrap.Modal(modalRef.value)
})
</script>
