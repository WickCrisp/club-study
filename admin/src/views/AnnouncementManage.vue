<template>
  <div class="app-page">
    <div class="page-head">
      <div>
        <h2>公告管理</h2>
        <p>草稿不会出现在前台，发布后所有人都能在公告栏看到</p>
      </div>
      <div class="head-actions">
        <el-button :icon="Refresh" @click="load">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">发布公告</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="announcements"
        stripe
        border
        :header-cell-style="{ background: '#f7f9fc', color: '#4a5568', fontWeight: 600 }"
      >
        <el-table-column label="标题" min-width="280">
          <template #default="{ row }">
            <div class="ann-title">
              <el-tag v-if="row.pinned" type="danger" size="small" effect="plain" round>置顶</el-tag>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" effect="light" round>
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="170">
          <template #default="{ row }">{{ formatTime(row.publishedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 'PUBLISHED' ? 'warning' : 'success'" @click="toggle(row)">
              {{ row.status === 'PUBLISHED' ? '撤回' : '发布' }}
            </el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="还没有公告，点右上角发布" :image-size="90" />
        </template>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑公告' : '发布公告'"
      width="620px"
      align-center
      modal-class="app-modal-overlay"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="一句话说清这条公告讲什么" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="7" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="DRAFT">草稿</el-radio>
                <el-radio value="PUBLISHED">发布</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="置顶">
              <el-switch v-model="form.pinned" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import api from '../api'

const announcements = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, title: '', content: '', status: 'DRAFT', pinned: false })

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 100, message: '标题不超过 100 字', trigger: 'blur' }
  ],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }]
}

const formatTime = (t) => (t ? t.substring(0, 16).replace('T', ' ') : '—')

const load = async () => {
  loading.value = true
  try {
    const result = await api.get('/announcement/admin/list')
    if (result.code === 200) {
      announcements.value = result.data || []
    } else {
      ElMessage.error(result.message || '加载公告失败')
    }
  } catch (e) {
    ElMessage.error('加载公告失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  Object.assign(form, { id: null, title: '', content: '', status: 'DRAFT', pinned: false })
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    title: row.title,
    content: row.content || '',
    status: row.status || 'DRAFT',
    pinned: Boolean(row.pinned)
  })
  dialogVisible.value = true
}

const save = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  saving.value = true
  try {
    const payload = {
      title: form.title.trim(),
      content: form.content,
      status: form.status,
      pinned: form.pinned
    }
    const result = form.id
      ? await api.put(`/announcement/${form.id}`, payload)
      : await api.post('/announcement', payload)

    if (result.code === 200) {
      ElMessage.success('保存成功')
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

const toggle = async (row) => {
  const target = row.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED'
  const text = target === 'PUBLISHED'
    ? `发布后「${row.title}」会立即出现在前台公告栏，确定吗？`
    : `撤回后「${row.title}」会从前台消失，确定吗？`

  try {
    await ElMessageBox.confirm(text, target === 'PUBLISHED' ? '发布公告' : '撤回公告', {
      type: 'warning',
      confirmButtonText: target === 'PUBLISHED' ? '确定发布' : '确定撤回',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }

  try {
    const result = await api.put(`/announcement/${row.id}/status?status=${target}`)
    if (result.code === 200) {
      ElMessage.success('状态已更新')
      load()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除公告「${row.title}」吗？删除后不可恢复。`, '删除公告', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger'
    })
  } catch (e) {
    return
  }

  try {
    const result = await api.delete(`/announcement/${row.id}`)
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

onMounted(load)
</script>

<style scoped>
.ann-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1f2d3d;
}
</style>
