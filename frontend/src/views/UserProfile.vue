<template>
  <div class="app-page">
    <div class="page-head">
      <div>
        <h2>个人中心</h2>
        <p>维护资料、修改密码，右侧可以快速回看报名记录</p>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="panel">
          <template #header><span class="panel-title">个人信息</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="学号">
                  <el-input :model-value="form.studentNo" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="form.name" maxlength="50" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="form.phone" placeholder="选填，11 位手机号" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="form.email" placeholder="选填" clearable />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :value="0">未知</el-radio>
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-button type="primary" :loading="saving" @click="saveProfile">保存修改</el-button>
          </el-form>
        </el-card>

        <el-card shadow="never" class="panel">
          <template #header><span class="panel-title">修改密码</span></template>
          <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-position="top">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
                </el-form-item>
              </el-col>
            </el-row>
            <el-button type="warning" plain :loading="pwdSaving" @click="changePassword">修改密码</el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="side-head">
              <span class="panel-title">我的报名</span>
              <el-button link type="primary" @click="$router.push('/activity/list')">去活动页</el-button>
            </div>
          </template>

          <div v-if="myRegistrations.length" class="reg-list">
            <div v-for="r in myRegistrations" :key="r.id" class="reg-item" @click="$router.push('/activity/detail/' + r.activityId)">
              <div class="reg-left">
                <div class="reg-name">{{ r.activityTitle || ('活动 #' + r.activityId) }}</div>
                <div class="reg-time">{{ formatTime(r.registeredAt) }}</div>
              </div>
              <el-tag :type="regType(r.status)" size="small" effect="light" round>{{ regText(r.status) }}</el-tag>
            </div>
          </div>
          <el-empty v-else description="还没有报名记录" :image-size="70" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const emit = defineEmits(['update-user'])

const formRef = ref(null)
const pwdRef = ref(null)
const saving = ref(false)
const pwdSaving = ref(false)
const myRegistrations = ref([])

const form = reactive({ studentNo: '', name: '', phone: '', email: '', gender: 0 })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 50, message: '姓名不超过 50 字', trigger: 'blur' }
  ],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次输入的新密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

const formatTime = (t) => (t ? t.substring(0, 16).replace('T', ' ') : '—')
const regText = (s) => ({ REGISTERED: '已报名', CANCEL_PENDING: '取消待审', CANCELLED: '已取消' }[s] || s)
const regType = (s) => ({ REGISTERED: 'success', CANCEL_PENDING: 'warning', CANCELLED: 'info' }[s] || 'info')

const loadUser = async () => {
  try {
    const result = await api.get('/user/current')
    if (result.code === 200) {
      Object.assign(form, result.data)
      form.gender = result.data.gender ?? 0
    }
  } catch (e) {
    ElMessage.error('加载用户信息失败')
  }
}

const loadRegistrations = async () => {
  try {
    const result = await api.get('/registration/my')
    if (result.code === 200) myRegistrations.value = result.data || []
  } catch (e) {
    myRegistrations.value = []
  }
}

const saveProfile = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  saving.value = true
  try {
    const payload = {
      name: form.name.trim(),
      phone: form.phone || null,
      email: form.email || null,
      gender: form.gender
    }
    const result = await api.put('/user/update', payload)
    if (result.code === 200) {
      ElMessage.success('资料已更新')
      emit('update-user')
      loadUser()
    } else {
      ElMessage.error(result.message || '更新失败')
    }
  } catch (e) {
    ElMessage.error('更新失败')
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  try {
    await pwdRef.value.validate()
  } catch (e) {
    return
  }

  pwdSaving.value = true
  try {
    const params = new URLSearchParams()
    params.append('oldPassword', pwdForm.oldPassword)
    params.append('newPassword', pwdForm.newPassword)

    const result = await api.put('/user/updatePassword', params)
    if (result.code === 200) {
      ElMessage.success('密码修改成功')
      pwdRef.value.resetFields()
    } else {
      ElMessage.error(result.message || '修改失败')
    }
  } catch (e) {
    ElMessage.error('修改失败')
  } finally {
    pwdSaving.value = false
  }
}

onMounted(() => {
  loadUser()
  loadRegistrations()
})
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

.side-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.reg-list {
  max-height: 420px;
  overflow-y: auto;
}

.reg-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 2px;
  border-bottom: 1px solid #f2f4f7;
  cursor: pointer;
}

.reg-item:last-child {
  border-bottom: none;
}

.reg-item:hover {
  background: #fafbfc;
}

.reg-name {
  font-size: 13px;
  color: #1f2d3d;
}

.reg-time {
  margin-top: 2px;
  font-size: 11px;
  color: #a8abb2;
}
</style>
