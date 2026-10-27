<template>
  <div class="app-page">
    <div class="page-head">
      <div>
        <h2>社团管理</h2>
        <p>共 {{ clubs.length }} 个社团，可查看成员、调整负责人与分类</p>
      </div>
      <div class="head-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索社团名称"
          clearable
          style="width: 220px"
          @keyup.enter="doSearch"
          @clear="loadClubs"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button :icon="Search" @click="doSearch">搜索</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">创建社团</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="clubs"
        stripe
        border
        :header-cell-style="{ background: '#f7f9fc', color: '#4a5568', fontWeight: 600 }"
      >
        <el-table-column label="社团" min-width="220">
          <template #default="{ row }">
            <div class="club-name">{{ row.name }}</div>
            <div class="club-sub">{{ row.categoryName || '未分类' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="负责人" width="130">
          <template #default="{ row }">
            <span :class="{ muted: !row.leaderName }">{{ row.leaderName || '未指定' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" effect="light" round>
              {{ row.status === 'ACTIVE' ? '活跃' : '已解散' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="130">
          <template #default="{ row }">{{ (row.createdAt || '').substring(0, 10) || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="还没有社团，点右上角创建" :image-size="90" />
        </template>
      </el-table>
    </el-card>

    <el-dialog
      v-model="formVisible"
      :title="form.id ? '编辑社团' : '创建社团'"
      width="480px"
      align-center
      modal-class="app-modal-overlay"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="社团名称" prop="name">
          <el-input v-model="form.name" placeholder="如：算法竞赛社" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="所属分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="社团简介">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" show-word-limit
                    placeholder="一句话说明这个社团做什么" />
        </el-form-item>
        <el-form-item label="负责人学号">
          <el-input v-model="form.leaderStudentNo" placeholder="留空则沿用原负责人（新建时为你自己）" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="ACTIVE">活跃</el-radio>
            <el-radio value="DISBANDED">已解散</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">{{ form.id ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" :title="detailClub ? detailClub.name : '社团详情'" size="560px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="负责人">{{ detailClub ? (detailClub.leaderName || '未指定') : '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ detailClub && detailClub.status === 'ACTIVE' ? '活跃' : '已解散' }}
        </el-descriptions-item>
        <el-descriptions-item label="分类">{{ (detailClub && detailClub.categoryName) || '未分类' }}</el-descriptions-item>
        <el-descriptions-item label="成员数">{{ approvedCount }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">
          {{ (detailClub && detailClub.description) || '暂无简介' }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="drawer-section">成员列表</div>
      <el-table v-loading="memberLoading" :data="detailMembers" size="small" border>
        <el-table-column prop="userName" label="姓名" width="90" />
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">{{ roleText(row.role) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small" effect="light">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="加入时间">
          <template #default="{ row }">{{ (row.joinedAt || '').substring(0, 10) || '—' }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无成员" :image-size="70" />
        </template>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import api from '../api'

const clubs = ref([])
const categories = ref([])
const keyword = ref('')
const loading = ref(false)

const formVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, name: '', categoryId: null, description: '', leaderStudentNo: '', status: 'ACTIVE' })

const drawerVisible = ref(false)
const detailClub = ref(null)
const detailMembers = ref([])
const memberLoading = ref(false)

// 成员数只算审批通过的，待审核的不计入
const approvedCount = computed(() => detailMembers.value.filter(m => m.status === 'APPROVED').length)

const rules = {
  name: [
    { required: true, message: '请输入社团名称', trigger: 'blur' },
    { max: 100, message: '名称不超过 100 字', trigger: 'blur' }
  ]
}

const roleText = (r) => ({ LEADER: '负责人', VICE_LEADER: '副负责人', MEMBER: '成员' }[r] || r)
const statusText = (s) => ({
  PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝', QUIT: '已退出', QUIT_PENDING: '退出待审'
}[s] || s)
const statusType = (s) => ({
  APPROVED: 'success', PENDING: 'warning', QUIT_PENDING: 'warning', REJECTED: 'danger', QUIT: 'info'
}[s] || 'info')

const loadClubs = async () => {
  loading.value = true
  try {
    const result = await api.get('/club/list')
    if (result.code === 200) {
      clubs.value = result.data || []
      keyword.value = ''
    } else {
      ElMessage.error(result.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('加载社团失败')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const result = await api.get('/club-category/list')
    if (result.code === 200) categories.value = result.data || []
  } catch (e) {
    categories.value = []
  }
}

const doSearch = async () => {
  const kw = keyword.value.trim()
  if (!kw) return loadClubs()
  loading.value = true
  try {
    const result = await api.get(`/club/search?keyword=${encodeURIComponent(kw)}`)
    if (result.code === 200) clubs.value = result.data || []
    else ElMessage.error(result.message || '搜索失败')
  } catch (e) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  Object.assign(form, { id: null, name: '', categoryId: null, description: '', leaderStudentNo: '', status: 'ACTIVE' })
  formVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    categoryId: row.categoryId || null,
    description: row.description || '',
    leaderStudentNo: '',
    status: row.status || 'ACTIVE'
  })
  formVisible.value = true
}

const submit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  saving.value = true
  try {
    const payload = {
      id: form.id,
      name: form.name.trim(),
      categoryId: form.categoryId,
      description: form.description,
      leaderStudentNo: form.leaderStudentNo.trim(),
      status: form.status
    }
    const result = form.id
      ? await api.put('/club/update', payload)
      : await api.post('/club/create', payload)

    if (result.code === 200) {
      ElMessage.success(form.id ? '更新成功' : '创建成功')
      formVisible.value = false
      loadClubs()
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const openDetail = async (row) => {
  detailClub.value = row
  detailMembers.value = []
  drawerVisible.value = true
  memberLoading.value = true
  try {
    const result = await api.get(`/club-member/club/${row.id}`)
    if (result.code === 200) detailMembers.value = result.data || []
  } catch (e) {
    detailMembers.value = []
  } finally {
    memberLoading.value = false
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除社团「${row.name}」吗？该社团的活动、成员与风采会一并删除，且不可恢复。`,
      '删除社团',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger' }
    )
  } catch (e) {
    return
  }

  try {
    const result = await api.delete(`/club/${row.id}`)
    if (result.code === 200) {
      ElMessage.success('删除成功')
      loadClubs()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadClubs()
  loadCategories()
})
</script>

<style scoped>
.club-name {
  font-size: 14px;
  color: #1f2d3d;
}

.club-sub {
  margin-top: 2px;
  font-size: 12px;
  color: #8a94a6;
}

.muted {
  color: #b6bfcc;
}

.drawer-section {
  margin: 22px 0 10px;
  font-size: 14px;
  font-weight: 500;
  color: #1f2d3d;
}
</style>
