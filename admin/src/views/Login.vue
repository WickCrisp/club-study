<template>
  <div class="auth-page">
    <div class="auth-panel">
      <div class="auth-brand">
        <div class="auth-logo">社</div>
        <div>
          <h1 class="auth-title">社团管理后台</h1>
          <p class="auth-sub">仅管理员可登录</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large" @submit.prevent>
        <el-form-item label="学号" prop="studentNo">
          <el-input
            v-model="form.studentNo"
            placeholder="请输入学号"
            :prefix-icon="User"
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button type="primary" size="large" class="auth-submit" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>

      <div class="auth-footer">仅限校内账号登录</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import api from '../api'
import { setToken } from '../utils/auth'

const router = useRouter()
const emit = defineEmits(['update-user'])

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ studentNo: '', password: '' })

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (loading.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('studentNo', form.studentNo.trim())
    params.append('password', form.password)

    const result = await api.post('/admin/login', params)
    if (result.code === 200) {
      setToken(result.data.token)
      emit('update-user')
      router.push('/')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '网络错误，请重试')
  } finally {
    loading.value = false
  }
}
</script>
