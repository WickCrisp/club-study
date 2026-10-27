<template>
  <div class="category-page">
    <div class="page-head">
      <div>
        <h2>分类管理</h2>
        <p>社团的归类字典，停用的分类不会出现在前台的筛选里</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增分类</el-button>
    </div>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="categories"
        stripe
        border
        :header-cell-style="{ background: '#f7f9fc', color: '#4a5568', fontWeight: 600 }"
      >
        <el-table-column label="分类名称" min-width="200">
          <template #default="{ row }">
            <span class="cat-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" align="center" />
        <el-table-column label="状态" width="140" align="center">
          <template #default="{ row }">
            <div class="status-cell">
              <el-switch :model-value="row.status === 'ACTIVE'" @change="(val) => toggleStatus(row, val)" />
              <span class="status-text" :class="{ off: row.status !== 'ACTIVE' }">
                {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="还没有分类，先新增一个" :image-size="90" />
        </template>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑分类' : '新增分类'"
      width="420px"
      align-center
      modal-class="app-modal-overlay"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="如：学术科技" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" style="width: 140px" />
          <span class="field-tip">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="ACTIVE">启用</el-radio>
            <el-radio value="DISABLED">停用</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import api from '../api'

const categories = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, name: '', sort: 0, status: 'ACTIVE' })

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 50, message: '名称不超过 50 字', trigger: 'blur' }
  ]
}

const load = async () => {
  loading.value = true
  try {
    const result = await api.get('/club-category/all')
    if (result.code === 200) {
      categories.value = result.data || []
    } else {
      ElMessage.error(result.message || '加载分类失败')
    }
  } catch (e) {
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  Object.assign(form, { id: null, name: '', sort: 0, status: 'ACTIVE' })
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, { id: row.id, name: row.name, sort: row.sort ?? 0, status: row.status || 'ACTIVE' })
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
    const payload = { name: form.name.trim(), sort: Number(form.sort), status: form.status }
    const result = form.id
      ? await api.put(`/club-category/${form.id}`, payload)
      : await api.post('/club-category', payload)

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

const toggleStatus = async (row, active) => {
  try {
    const result = await api.put(`/club-category/${row.id}`, { status: active ? 'ACTIVE' : 'DISABLED' })
    if (result.code === 200) {
      ElMessage.success(active ? '已启用' : '已停用')
      load()
    } else {
      ElMessage.error(result.message || '更新失败')
    }
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除分类「${row.name}」吗？该分类下的社团会变成未分类。`,
      '删除分类',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger' }
    )
  } catch (e) {
    return
  }

  try {
    const result = await api.delete(`/club-category/${row.id}`)
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
.category-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px 16px 40px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.page-head h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
}

.page-head p {
  margin: 0;
  font-size: 13px;
  color: #8a94a6;
}

.cat-name {
  font-size: 14px;
  color: #1f2d3d;
}

.status-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.status-text {
  font-size: 12px;
  color: #4a5568;
}

.status-text.off {
  color: #a8abb2;
}

.field-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #8a94a6;
}
</style>
