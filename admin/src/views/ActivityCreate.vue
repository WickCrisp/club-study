<template>
  <div class="app-page">
    <div class="page-head">
      <div>
        <h2>发布活动</h2>
        <p>保存后为草稿，需要在活动管理里提交审核，管理员通过后学生才能报名</p>
      </div>
      <div class="head-actions">
        <el-button :icon="Back" @click="$router.push('/activity/manage')">返回列表</el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
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
                <el-form-item label="参与范围" prop="joinScope">
                  <el-radio-group v-model="form.joinScope">
                    <el-radio value="PUBLIC">公开活动</el-radio>
                    <el-radio value="MEMBER_ONLY">本社团专属</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="活动标题" prop="title">
              <el-input v-model="form.title" placeholder="如：新生篮球友谊赛" maxlength="100" show-word-limit />
            </el-form-item>

            <el-form-item label="活动简介">
              <el-input v-model="form.description" type="textarea" :rows="3"
                        placeholder="简单说明活动内容、适合谁参加" maxlength="500" show-word-limit />
            </el-form-item>

            <el-row :gutter="16">
              <el-col :span="14">
                <el-form-item label="活动地点" prop="location">
                  <el-input v-model="form.location" placeholder="如：东区篮球场" />
                </el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="人数限制">
                  <el-input-number v-model="form.maxParticipants" :min="0" :max="9999" controls-position="right" style="width: 160px" />
                  <span class="field-tip">0 表示不限</span>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="活动时间" prop="timeRange">
              <el-date-picker
                v-model="form.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>

            <div class="form-footer">
              <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">保存为草稿</el-button>
              <el-button size="large" @click="resetForm">重置</el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="tip-card">
          <template #header><span class="tip-title">发布流程</span></template>
          <ol class="flow">
            <li><b>保存草稿</b>：填完信息点保存，此时学生看不到</li>
            <li><b>提交审核</b>：在活动管理里点「提交审核」</li>
            <li><b>管理员审批</b>：待办中心通过后，活动变为进行中</li>
            <li><b>学生报名</b>：公开活动所有人可报，专属活动仅本社团成员可报</li>
          </ol>
          <div class="note">
            <div class="note-title">注意</div>
            <p>名额报满后，后续报名会直接失败；取消报名需要审批通过，名额才会退回。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const clubs = ref([])
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  clubId: null,
  title: '',
  description: '',
  location: '',
  timeRange: [],
  maxParticipants: 0,
  joinScope: 'PUBLIC'
})

const rules = {
  clubId: [{ required: true, message: '请选择所属社团', trigger: 'change' }],
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { max: 100, message: '标题不超过 100 字', trigger: 'blur' }
  ],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动起止时间', trigger: 'change' }]
}

const loadClubs = async () => {
  try {
    const result = await api.get('/club/list')
    if (result.code === 200) clubs.value = result.data || []
  } catch (e) {
    ElMessage.error('加载社团列表失败')
  }
}

const resetForm = () => {
  formRef.value.resetFields()
  form.timeRange = []
  form.maxParticipants = 0
  form.joinScope = 'PUBLIC'
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  if (!form.timeRange || form.timeRange.length !== 2) {
    ElMessage.warning('请选择活动起止时间')
    return
  }
  if (form.timeRange[1] <= form.timeRange[0]) {
    ElMessage.warning('结束时间需要晚于开始时间')
    return
  }

  loading.value = true
  try {
    const payload = {
      clubId: Number(form.clubId),
      title: form.title.trim(),
      description: form.description,
      location: form.location.trim(),
      startTime: form.timeRange[0],
      endTime: form.timeRange[1],
      maxParticipants: Number(form.maxParticipants),
      joinScope: form.joinScope,
      status: 'DRAFT'
    }
    const result = await api.post('/activity/create', payload)
    if (result.code === 200) {
      ElMessage.success('已保存为草稿，记得去活动管理提交审核')
      router.push('/activity/manage')
    } else {
      ElMessage.error(result.message || '发布失败')
    }
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadClubs)
</script>

<style scoped>
.field-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #8a94a6;
}

.form-footer {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}

.tip-card {
  border-radius: 10px;
}

.tip-title {
  font-size: 15px;
  font-weight: 500;
  color: #1f2d3d;
}

.flow {
  margin: 0 0 16px;
  padding-left: 18px;
  font-size: 13px;
  line-height: 2;
  color: #4a5568;
}

.flow b {
  color: #1f2d3d;
  font-weight: 500;
}

.note {
  padding: 12px 14px;
  background: #fdf6ec;
  border-radius: 8px;
}

.note-title {
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 500;
  color: #b88230;
}

.note p {
  margin: 0;
  font-size: 12px;
  line-height: 1.7;
  color: #8a6d3b;
}
</style>
