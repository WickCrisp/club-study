<template>
  <div>
    <div class="page-head">
      <div>
        <h2>社团信息</h2>
        <p>修改后前台立即生效；负责人变更需要联系管理员</p>
      </div>
      <div class="head-actions">
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="panel">
          <template #header><span class="panel-title">基本信息</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" v-loading="loading">
            <el-form-item label="社团名称" prop="name">
              <el-input v-model="form.name" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="所属分类">
              <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="社团简介">
              <el-input v-model="form.description" type="textarea" :rows="4" maxlength="200" show-word-limit
                        placeholder="介绍一下这个社团在做什么" />
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="ACTIVE">活跃</el-radio>
                <el-radio value="DISBANDED">已解散</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-button type="primary" :loading="saving" @click="save">保存修改</el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="panel">
          <template #header><span class="panel-title">社团概况</span></template>
          <div class="logo-row">
            <div class="logo">{{ (form.name || '社').slice(0, 1) }}</div>
            <div>
              <div class="name">{{ form.name || '未命名' }}</div>
              <el-tag :type="form.status === 'ACTIVE' ? 'success' : 'info'" size="small" effect="plain" round>
                {{ form.status === 'ACTIVE' ? '活跃' : '已解散' }}
              </el-tag>
            </div>
          </div>
          <el-descriptions :column="1" class="desc">
            <el-descriptions-item label="负责人">{{ leaderName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ (createdAt || '').substring(0, 10) || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import api from '../../api'
import { useManagedClubs } from '../../composables/useManagedClubs'

const { currentClub, currentClubId, ensure } = useManagedClubs()

const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const categories = ref([])
const leaderName = ref('')
const createdAt = ref('')

const form = reactive({ id: null, name: '', categoryId: null, description: '', status: 'ACTIVE' })

const rules = {
  name: [
    { required: true, message: '请输入社团名称', trigger: 'blur' },
    { max: 100, message: '名称不超过 100 字', trigger: 'blur' }
  ]
}

const fill = (club) => {
  Object.assign(form, {
    id: club.id,
    name: club.name,
    categoryId: club.categoryId || null,
    description: club.description || '',
    status: club.status || 'ACTIVE'
  })
  leaderName.value = club.leaderName || ''
  createdAt.value = club.createdAt || ''
}

const load = async () => {
  await ensure()
  if (!currentClub.value) return
  loading.value = true
  try {
    const [detail, cats] = await Promise.all([
      api.get(`/club/${currentClub.value.id}`).catch(() => null),
      api.get('/club-category/list').catch(() => null)
    ])
    if (detail && detail.code === 200 && detail.data) fill(detail.data)
    else fill(currentClub.value)
    if (cats && cats.code === 200) categories.value = cats.data || []
  } finally {
    loading.value = false
  }
}

const save = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  saving.value = true
  try {
    const result = await api.put('/club/update', {
      id: form.id,
      name: form.name.trim(),
      categoryId: form.categoryId,
      description: form.description,
      status: form.status,
      leaderStudentNo: ''
    })
    if (result.code === 200) {
      ElMessage.success('已保存')
      await ensure(true)
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

watch(currentClubId, load)
onMounted(load)
</script>

<style scoped>
.panel {
  border-radius: 10px;
  margin-bottom: 16px;
}

.panel-title {
  font-size: 15px;
  font-weight: 500;
  color: #1f2d3d;
}

.logo-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.logo {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: #1a1a2e;
  color: #ffffff;
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}

.name {
  margin-bottom: 6px;
  font-size: 16px;
  font-weight: 500;
  color: #1f2d3d;
}

.desc {
  margin-top: 4px;
}
</style>
