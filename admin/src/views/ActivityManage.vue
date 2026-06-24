<template>
  <div class="container">
    <div class="page-header d-flex justify-content-between align-items-center">
      <h2>活动管理</h2>
      <button class="btn btn-primary" @click="$router.push('/activity/create')">+ 发布活动</button>
    </div>

    <ul class="nav nav-tabs mb-4">
      <li class="nav-item">
        <a class="nav-link" :class="{ active: tab === 'my' }" href="#" @click.prevent="tab = 'my'">我发布的活动</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" :class="{ active: tab === 'all' }" href="#" @click.prevent="tab = 'all'">全部活动</a>
      </li>
    </ul>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
    </div>

    <!-- 我发布的活动 -->
    <div v-if="tab === 'my' && !loading">
      <div v-if="myActivities.length > 0">
        <div class="table-responsive">
          <table class="table table-hover">
            <thead><tr><th>标题</th><th>所属社团</th><th>报名人数</th><th>状态</th><th>时间</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="a in myActivities" :key="a.id">
                <td>{{ a.title }}</td>
                <td>{{ a.clubName || '-' }}</td>
                <td>{{ a.currentCount }}/{{ a.maxParticipants || '不限' }}</td>
                <td><span class="badge" :class="statusClass(a.status)">{{ statusText(a.status) }}</span></td>
                <td class="small">{{ formatDate(a.startTime) }}</td>
                <td>
                  <div class="action-buttons">
                    <button class="btn btn-sm btn-outline-primary" @click="editActivity(a)">编辑</button>
                    <button class="btn btn-sm btn-outline-danger" @click="confirmDelete(a.id)">删除</button>
                    <button class="btn btn-sm btn-outline-info" @click="toggleDetail(a)" v-if="expandedId !== a.id">详情</button>
                    <button class="btn btn-sm btn-outline-secondary" @click="expandedId = null" v-else>收起</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="expandedId" class="card mt-3">
          <div class="card-header">活动详情 — 报名列表</div>
          <div class="card-body">
            <div v-if="detailRegistrations.length > 0">
              <h6>报名列表</h6>
              <table class="table table-sm">
                <thead><tr><th>姓名</th><th>学号</th><th>报名时间</th><th>状态</th></tr></thead>
                <tbody>
                  <tr v-for="r in detailRegistrations" :key="r.id">
                    <td>{{ r.userName }}</td><td>{{ r.studentNo }}</td>
                    <td>{{ r.registeredAt }}</td>
                    <td><span class="badge" :class="r.status === 'REGISTERED' ? 'bg-success' : 'bg-secondary'">{{ r.status === 'REGISTERED' ? '已报名' : '已取消' }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="text-muted">暂无报名记录</div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state"><div class="icon"></div><p>暂无发布的活动</p></div>
    </div>

    <!-- 全部活动 -->
    <div v-if="tab === 'all' && !loading">
      <div v-if="allActivities.length > 0">
        <div class="table-responsive">
          <table class="table table-hover">
            <thead><tr><th>标题</th><th>所属社团</th><th>发布人</th><th>报名人数</th><th>状态</th><th>时间</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="a in allActivities" :key="a.id">
                <td>{{ a.title }}</td>
                <td>{{ a.clubName || '-' }}</td>
                <td>{{ a.creatorName || '-' }}</td>
                <td>{{ a.currentCount }}/{{ a.maxParticipants || '不限' }}</td>
                <td><span class="badge" :class="statusClass(a.status)">{{ statusText(a.status) }}</span></td>
                <td class="small">{{ formatDate(a.startTime) }}</td>
                <td><button class="btn btn-sm btn-outline-danger" @click="confirmDelete(a.id)">删除</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-else class="empty-state"><div class="icon"></div><p>暂无活动</p></div>
    </div>

    <!-- 编辑模态框 -->
    <div class="modal fade" ref="modalRef" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">编辑活动</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">所属社团</label>
              <select class="form-select" v-model="editForm.clubId">
                <option value="">请选择社团</option>
                <option v-for="club in clubList" :key="club.id" :value="club.id">{{ club.name }}</option>
              </select>
            </div>
            <div class="mb-3"><label class="form-label">标题</label><input type="text" class="form-control" v-model="editForm.title" /></div>
            <div class="mb-3"><label class="form-label">简介</label><textarea class="form-control" v-model="editForm.description" rows="3"></textarea></div>
            <div class="mb-3"><label class="form-label">地点</label><input type="text" class="form-control" v-model="editForm.location" /></div>
            <div class="row">
              <div class="col-md-6 mb-3"><label class="form-label">开始时间</label><input type="datetime-local" class="form-control" v-model="editForm.startTime" /></div>
              <div class="col-md-6 mb-3"><label class="form-label">结束时间</label><input type="datetime-local" class="form-control" v-model="editForm.endTime" /></div>
            </div>
            <div class="row">
              <div class="col-md-6 mb-3"><label class="form-label">人数限制</label><input type="number" class="form-control" v-model="editForm.maxParticipants" min="0" /></div>
              <div class="col-md-6 mb-3">
                <label class="form-label">状态</label>
                <select class="form-select" v-model="editForm.status">
                  <option value="DRAFT">草稿</option><option value="ONGOING">进行中</option><option value="FINISHED">已结束</option><option value="CANCELLED">已取消</option>
                </select>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" @click="handleEdit" :disabled="editLoading">保存</button>
          </div>
        </div>
      </div>
    </div>

    <div class="toast-container">
      <div v-if="toast.show" class="alert alert-dismissible fade show" :class="'alert-' + toast.type">{{ toast.message }}<button type="button" class="btn-close" @click="toast.show = false"></button></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'

const tab = ref('my')
const loading = ref(false)
const myActivities = ref([])
const allActivities = ref([])
const expandedId = ref(null)
const detailRegistrations = ref([])
const modalRef = ref(null)
const editLoading = ref(false)
const toast = reactive({ show: false, message: '', type: 'danger' })
let modalInstance = null

const editForm = reactive({ id: null, clubId: '', title: '', description: '', location: '', startTime: '', endTime: '', maxParticipants: 0, status: '' })
const clubList = ref([])

const showToast = (m, t) => { toast.show = true; toast.message = m; toast.type = t; setTimeout(() => toast.show = false, 3000) }
const statusClass = (s) => ({ DRAFT:'bg-secondary', ONGOING:'bg-success', FINISHED:'bg-info', CANCELLED:'bg-danger' }[s]||'bg-secondary')
const statusText = (s) => ({ DRAFT:'草稿', ONGOING:'进行中', FINISHED:'已结束', CANCELLED:'已取消' }[s]||s)
const formatDate = (d) => d ? new Date(d).toLocaleDateString('zh-CN') : '-'

const loadData = async () => {
  loading.value = true
  try {
    const [myRes, allRes] = await Promise.all([api.get('/activity/my-published'), api.get('/activity/list')])
    if (myRes.code === 200) myActivities.value = myRes.data || []
    if (allRes.code === 200) allActivities.value = allRes.data || []
  } catch (e) { showToast('加载活动失败', 'danger') }
  finally { loading.value = false }
}

const toggleDetail = async (activity) => {
  if (expandedId.value === activity.id) { expandedId.value = null; return }
  expandedId.value = activity.id
  try {
    const regRes = await api.get(`/registration/activity/${activity.id}`)
    detailRegistrations.value = regRes.code === 200 ? (regRes.data||[]) : []
  } catch (e) { showToast('加载活动失败', 'danger') }
}

const editActivity = (a) => {
  Object.assign(editForm, { id:a.id, clubId:a.clubId||'', title:a.title, description:a.description||'', location:a.location, startTime:a.startTime?a.startTime.substring(0,16):'', endTime:a.endTime?a.endTime.substring(0,16):'', maxParticipants:a.maxParticipants, status:a.status })
  modalInstance.show()
}

const handleEdit = async () => {
  editLoading.value = true
  try {
    const payload = { ...editForm, clubId:Number(editForm.clubId), maxParticipants:Number(editForm.maxParticipants), startTime:editForm.startTime?editForm.startTime+':00':'', endTime:editForm.endTime?editForm.endTime+':00':'' }
    const result = await api.put('/activity/update', payload)
    if (result.code === 200) { showToast('更新成功','success'); modalInstance.hide(); loadData() }
    else showToast(result.message, 'danger')
  } catch (e) { showToast('操作失败', 'danger') }
  finally { editLoading.value = false }
}

const confirmDelete = async (id) => {
  if (!confirm('确定删除该活动吗？')) return
  try {
    const result = await api.delete(`/activity/${id}`)
    if (result.code === 200) { showToast('删除成功','success'); loadData() }
    else showToast(result.message, 'danger')
  } catch (e) { showToast('删除失败', 'danger') }
}

const loadClubs = async () => { try { const r = await api.get('/club/list'); if (r.code===200) clubList.value = r.data||[] } catch (e) {} }

onMounted(() => { loadData(); loadClubs(); modalInstance = new bootstrap.Modal(modalRef.value) })
</script>
