<template>
  <div class="app-page">
    <div class="page-head">
      <div>
        <h2>个人中心</h2>
        <p>维护你的账号资料与登录密码</p>
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
        <el-card shadow="never" class="panel profile-card">
          <div class="avatar">{{ avatarText }}</div>
          <div class="profile-name">{{ form.name || '未设置姓名' }}</div>
          <el-tag :type="form.role === 'ADMIN' ? 'danger' : 'info'" effect="plain" round>
            {{ form.role === 'ADMIN' ? '管理员' : '学生' }}
          </el-tag>

          <el-descriptions :column="1" class="profile-desc">
            <el-descriptions-item label="学号">{{ form.studentNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ (form.createdAt || '').substring(0, 10) || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const emit = defineEmits(['update-user'])

const formRef = ref(null)
const pwdRef = ref(null)
const saving = ref(false)
const pwdSaving = ref(false)

const form = reactive({ studentNo: '', name: '', phone: '', email: '', gender: 0, role: '', createdAt: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const avatarText = computed(() => (form.name ? form.name.slice(0, 1) : '?'))

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

const loadUser = async () => {
  try {
    const result = await api.get('/admin/current', { skipAuthRedirect: true })
    if (result.code === 200) {
      Object.assign(form, result.data)
      form.gender = result.data.gender ?? 0
    }
  } catch (e) {
    ElMessage.error('加载用户信息失败')
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
    const result = await api.put('/admin/update', payload)
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

    const result = await api.put('/admin/updatePassword', params)
    if (result.code === 200) {
      ElMessage.success('密码修改成功')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
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

onMounted(loadUser)
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

.profile-card {
  text-align: center;
  padding-top: 8px;
}

.avatar {
  width: 64px;
  height: 64px;
  margin: 8px auto 12px;
  border-radius: 50%;
  background: #1a1a2e;
  color: #ffffff;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-name {
  margin-bottom: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #1f2d3d;
}

.profile-desc {
  margin-top: 20px;
  text-align: left;
}
</style>
