<template>
  <div>
    <div class="page-head">
      <div>
        <h2>活动管理</h2>
        <p>{{ clubName }} · 草稿提交审核后由管理员审批，通过后学生才能报名</p>
      </div>
      <div class="head-actions">
        <el-button :icon="Refresh" @click="load">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="$router.push('/manage/activities/create')">发布活动</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="activities" stripe border
                :header-cell-style="{ background: '#f7f9fc', color: '#4a5568', fontWeight: 600 }">
        <el-table-column label="活动" min-width="220">
          <template #default="{ row }">
            <div class="act-title">{{ row.title }}</div>
            <div class="act-sub">{{ row.location || '地点待定' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="报名" width="110" align="center">
          <template #default="{ row }">
            <span class="count">{{ row.currentCount || 0 }}</span>
            <span class="count-total"> / {{ row.maxParticipants || '不限' }}</span>
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
        <el-table-column label="操作" width="230" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'DRAFT' || row.status === 'REJECTED'" link type="success"
                       :loading="submitting === row.id" @click="submitReview(row)">提交审核</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="还没有发布活动" :image-size="80" />
        </template>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑活动" width="600px" align-center
               modal-class="app-modal-overlay" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
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
              <el-input-number v-model="form.maxParticipants" :min="0" :max="9999" controls-position="right" style="width: 150px" />
              <span class="tip">0 表示不限</span>
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'
import api from '../../api'
import { useManagedClubs } from '../../composables/useManagedClubs'

const { currentClub, currentClubId, ensure } = useManagedClubs()

const activities = ref([])
const loading = ref(false)
const submitting = ref(null)

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, clubId: null, title: '', description: '', location: '',
  startTime: '', endTime: '', maxParticipants: 0, status: 'DRAFT', joinScope: 'PUBLIC'
})

const clubName = computed(() => (currentClub.value ? currentClub.value.name : '—'))
const formatTime = (t) => (t ? t.substring(0, 16).replace('T', ' ') : '—')
const statusText = (s) => ({ DRAFT: '草稿', PENDING: '待审核', REJECTED: '已驳回', ONGOING: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }[s] || s)
const statusType = (s) => ({ DRAFT: 'info', PENDING: 'warning', REJECTED: 'danger', ONGOING: 'success', FINISHED: 'primary', CANCELLED: 'info' }[s] || 'info')

const rules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const load = async () => {
  await ensure()
  if (!currentClub.value) {
    activities.value = []
    return
  }
  loading.value = true
  try {
    const result = await api.get(`/activity/club/${currentClub.value.id}`)
    activities.value = result.code === 200 ? (result.data || []) : []
  } catch (e) {
    activities.value = []
    ElMessage.error('加载活动失败')
  } finally {
    loading.value = false
  }
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    clubId: row.clubId,
    title: row.title,
    description: row.description || '',
    location: row.location || '',
    startTime: row.startTime ? row.startTime.substring(0, 19) : '',
    endTime: row.endTime ? row.endTime.substring(0, 19) : '',
    maxParticipants: row.maxParticipants || 0,
    status: row.status || 'DRAFT',
    joinScope: row.joinScope || 'PUBLIC'
  })
  dialogVisible.value = true
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
    const result = await api.put('/activity/update', {
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
    })
    if (result.code === 200) {
      ElMessage.success('更新成功')
      dialogVisible.value = false
      load()
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
      load()
    } else {
      ElMessage.error(result.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = null
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除活动「${row.title}」吗？报名记录会一并删除。`, '删除活动', {
      type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger'
    })
  } catch (e) {
    return
  }
  try {
    const result = await api.delete(`/activity/${row.id}`)
    if (result.code === 200) {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

watch(currentClubId, load)
onMounted(load)
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

.tip {
  margin-left: 8px;
  font-size: 12px;
  color: #8a94a6;
}
</style>
