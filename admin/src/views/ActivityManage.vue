<template>
  <div class="app-page">
    <div class="page-head">
      <div>
        <h2>活动管理</h2>
        <p>草稿提交审核后由管理员审批，通过后学生才能报名</p>
      </div>
      <div class="head-actions">
        <el-radio-group v-model="tab">
          <el-radio-button value="my">我发布的</el-radio-button>
          <el-radio-button value="all">全部活动</el-radio-button>
        </el-radio-group>
        <el-button :icon="Refresh" @click="loadData">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="$router.push('/activity/create')">发布活动</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="rows"
        stripe
        border
        :header-cell-style="{ background: '#f7f9fc', color: '#4a5568', fontWeight: 600 }"
      >
        <el-table-column label="活动" min-width="220">
          <template #default="{ row }">
            <div class="act-title">{{ row.title }}</div>
            <div class="act-sub">{{ row.clubName || '未归属社团' }}</div>
          </template>
        </el-table-column>
        <el-table-column v-if="tab === 'all'" label="发布人" width="110">
          <template #default="{ row }">{{ row.creatorName || '—' }}</template>
        </el-table-column>
        <el-table-column label="报名" width="110" align="center">
          <template #default="{ row }">
            <span class="count">{{ row.currentCount || 0 }}</span>
            <span class="count-total"> / {{ row.maxParticipants ? row.maxParticipants : '不限' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light" round>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="160">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'DRAFT' || row.status === 'REJECTED'"
              link type="success"
              :loading="submitting === row.id"
              @click="submitReview(row)"
            >提交审核</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty :description="tab === 'my' ? '还没有发布过活动' : '暂无活动'" :image-size="90" />
        </template>
      </el-table>
    </el-card>

    <el-dialog
      v-model="formVisible"
      title="编辑活动"
      width="600px"
      align-center
      modal-class="app-modal-overlay"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属社团" prop="clubId">
              <el-select v-model="form.clubId" placeholder="选择社团" style="width: 100%">
                <el-option v-for="c in clubs" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动标题" prop="title">
              <el-input v-model="form.title" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动简介">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="活动地点" prop="location">
              <el-input v-model="form.location" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="人数限制">
              <el-input-number v-model="form.maxParticipants" :min="0" :max="9999" controls-position="right" style="width: 160px" />
              <span class="field-tip">0 表示不限</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker v-model="form.startTime" type="datetime" style="width: 100%"
                              value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择开始时间" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker v-model="form.endTime" type="datetime" style="width: 100%"
                              value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择结束时间" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="参与范围">
              <el-radio-group v-model="form.joinScope">
                <el-radio value="PUBLIC">公开</el-radio>
                <el-radio value="MEMBER_ONLY">本社团专属</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="待审核" value="PENDING" />
                <el-option label="进行中" value="ONGOING" />
                <el-option label="已结束" value="FINISHED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" :title="detailActivity ? detailActivity.title : '活动详情'" size="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="所属社团">{{ (detailActivity && detailActivity.clubName) || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ detailActivity ? statusText(detailActivity.status) : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="地点">{{ (detailActivity && detailActivity.location) || '—' }}</el-descriptions-item>
        <el-descriptions-item label="报名情况">
          {{ detailActivity ? (detailActivity.currentCount || 0) : 0 }} /
          {{ detailActivity && detailActivity.maxParticipants ? detailActivity.maxParticipants : '不限' }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间" :span="2">{{ formatTime(detailActivity && detailActivity.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">
          {{ (detailActivity && detailActivity.description) || '暂无简介' }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="drawer-section">报名名单（{{ registrations.length }}）</div>
      <el-table v-loading="regLoading" :data="registrations" size="small" border>
        <el-table-column prop="userName" label="姓名" width="90" />
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column label="报名时间" width="150">
          <template #default="{ row }">{{ formatTime(row.registeredAt) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="regType(row.status)" size="small" effect="light">{{ regText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 'CANCEL_PENDING'">
              <el-button link type="primary" @click="decideCancel(row, true)">通过取消</el-button>
              <el-button link type="danger" @click="decideCancel(row, false)">驳回</el-button>
            </template>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无报名记录" :image-size="70" />
        </template>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import api from '../api'

const tab = ref('my')
const loading = ref(false)
const myActivities = ref([])
const allActivities = ref([])
const clubs = ref([])

const formVisible = ref(false)
const saving = ref(false)
const submitting = ref(null)
const formRef = ref(null)
const form = reactive({
  id: null, clubId: null, title: '', description: '', location: '',
  startTime: '', endTime: '', maxParticipants: 0, status: 'DRAFT', joinScope: 'PUBLIC'
})

const drawerVisible = ref(false)
const detailActivity = ref(null)
const registrations = ref([])
const regLoading = ref(false)

const rows = computed(() => (tab.value === 'my' ? myActivities.value : allActivities.value))

const statusText = (s) => ({
  DRAFT: '草稿', PENDING: '待审核', REJECTED: '已驳回',
  ONGOING: '进行中', FINISHED: '已结束', CANCELLED: '已取消'
}[s] || s)
const statusType = (s) => ({
  DRAFT: 'info', PENDING: 'warning', REJECTED: 'danger',
  ONGOING: 'success', FINISHED: 'primary', CANCELLED: 'info'
}[s] || 'info')
const regText = (s) => ({ REGISTERED: '已报名', CANCEL_PENDING: '取消待审', CANCELLED: '已取消' }[s] || s)
const regType = (s) => ({ REGISTERED: 'success', CANCEL_PENDING: 'warning', CANCELLED: 'info' }[s] || 'info')
const formatTime = (t) => (t ? t.substring(0, 16).replace('T', ' ') : '—')

const rules = {
  clubId: [{ required: true, message: '请选择所属社团', trigger: 'change' }],
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const [myRes, allRes] = await Promise.all([
      api.get('/activity/my-published'),
      api.get('/activity/list')
    ])
    if (myRes.code === 200) myActivities.value = myRes.data || []
    if (allRes.code === 200) allActivities.value = allRes.data || []
  } catch (e) {
    ElMessage.error('加载活动失败')
  } finally {
    loading.value = false
  }
}

const loadClubs = async () => {
  try {
    const result = await api.get('/club/list')
    if (result.code === 200) clubs.value = result.data || []
  } catch (e) {
    clubs.value = []
  }
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    clubId: row.clubId || null,
    title: row.title,
    description: row.description || '',
    location: row.location || '',
    startTime: row.startTime ? row.startTime.substring(0, 19) : '',
    endTime: row.endTime ? row.endTime.substring(0, 19) : '',
    maxParticipants: row.maxParticipants || 0,
    status: row.status || 'DRAFT',
    joinScope: row.joinScope || 'PUBLIC'
  })
  formVisible.value = true
}

const save = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  if (form.startTime && form.endTime && form.endTime <= form.startTime) {
    ElMessage.warning('结束时间需要晚于开始时间')
    return
  }

  saving.value = true
  try {
    const payload = {
      id: form.id,
      clubId: Number(form.clubId),
      title: form.title.trim(),
      description: form.description,
      location: form.location.trim(),
      startTime: form.startTime,
      endTime: form.endTime,
      maxParticipants: Number(form.maxParticipants),
      status: form.status,
      joinScope: form.joinScope
    }
    const result = await api.put('/activity/update', payload)
    if (result.code === 200) {
      ElMessage.success('更新成功')
      formVisible.value = false
      loadData()
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const submitReview = async (row) => {
  submitting.value = row.id
  try {
    const result = await api.post(`/activity/${row.id}/submit`)
    if (result.code === 200) {
      ElMessage.success('已提交审核，等待管理员审批')
      loadData()
    } else {
      ElMessage.error(result.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = null
  }
}

const openDetail = async (row) => {
  detailActivity.value = row
  registrations.value = []
  drawerVisible.value = true
  regLoading.value = true
  try {
    const result = await api.get(`/registration/activity/${row.id}`)
    if (result.code === 200) registrations.value = result.data || []
  } catch (e) {
    registrations.value = []
  } finally {
    regLoading.value = false
  }
}

const decideCancel = async (reg, approved) => {
  const text = approved
    ? `通过后「${reg.userName}」的报名会被取消，名额退回活动。确定吗？`
    : `驳回后「${reg.userName}」仍保留报名。确定吗？`
  try {
    await ElMessageBox.confirm(text, approved ? '通过取消申请' : '驳回取消申请', {
      type: 'warning',
      confirmButtonText: approved ? '确定通过' : '确定驳回',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }

  try {
    const url = `/registration/${approved ? 'approve' : 'reject'}-cancel/${reg.id}`
    const result = await api.put(url)
    if (result.code === 200) {
      ElMessage.success(result.message || '操作成功')
      if (detailActivity.value) {
        const res = await api.get(`/registration/activity/${detailActivity.value.id}`)
        if (res.code === 200) registrations.value = res.data || []
      }
      loadData()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除活动「${row.title}」吗？报名记录会一并删除。`, '删除活动', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger'
    })
  } catch (e) {
    return
  }

  try {
    const result = await api.delete(`/activity/${row.id}`)
    if (result.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadData()
  loadClubs()
})
</script>

<style scoped>
.act-title {
  font-size: 14px;
  color: #1f2d3d;
}

.act-sub {
  margin-top: 2px;
  font-size: 12px;
  color: #8a94a6;
}

.count {
  font-size: 14px;
  color: #1f2d3d;
}

.count-total {
  font-size: 12px;
  color: #a8abb2;
}

.field-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #8a94a6;
}

.drawer-section {
  margin: 22px 0 10px;
  font-size: 14px;
  font-weight: 500;
  color: #1f2d3d;
}

.muted {
  color: #b6bfcc;
}
</style>
