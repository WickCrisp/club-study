<template>
  <div>
    <div class="page-head">
      <div>
        <h2>发布活动</h2>
        <p>保存后是草稿，需要在「活动管理」里提交审核，管理员通过后学生才能报名</p>
      </div>
      <div class="head-actions">
        <el-button :icon="Back" @click="$router.push('/manage/activities')">返回列表</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属社团">
              <el-input :model-value="clubName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="参与范围">
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
              <el-input-number v-model="form.maxParticipants" :min="0" :max="9999" controls-position="right" style="width: 150px" />
              <span class="tip">0 表示不限</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker v-model="form.timeRange" type="datetimerange" range-separator="至"
                          start-placeholder="开始时间" end-placeholder="结束时间"
                          value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>

        <el-button type="primary" size="large" :loading="saving" @click="handleSubmit">保存为草稿</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import api from '../../api'
import { useManagedClubs } from '../../composables/useManagedClubs'

const router = useRouter()
const { currentClub, ensure } = useManagedClubs()

const formRef = ref(null)
const saving = ref(false)
const form = reactive({ title: '', description: '', location: '', timeRange: [], maxParticipants: 0, joinScope: 'PUBLIC' })

const clubName = computed(() => (currentClub.value ? currentClub.value.name : '—'))

const rules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动起止时间', trigger: 'change' }]
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  if (!currentClub.value) {
    ElMessage.warning('当前没有负责的社团')
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

  saving.value = true
  try {
    const result = await api.post('/activity/create', {
      clubId: currentClub.value.id,
      title: form.title.trim(),
      description: form.description,
      location: form.location.trim(),
      startTime: form.timeRange[0],
      endTime: form.timeRange[1],
      maxParticipants: Number(form.maxParticipants),
      joinScope: form.joinScope,
      status: 'DRAFT'
    })
    if (result.code === 200) {
      ElMessage.success('已保存为草稿，记得去活动管理提交审核')
      router.push('/manage/activities')
    } else {
      ElMessage.error(result.message || '发布失败')
    }
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    saving.value = false
  }
}

onMounted(ensure)
</script>

<style scoped>
.tip {
  margin-left: 8px;
  font-size: 12px;
  color: #8a94a6;
}
</style>
