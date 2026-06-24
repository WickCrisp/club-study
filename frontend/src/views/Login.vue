<template>
  <div class="login-container">
    <div class="login-card">
      <h2>社团活动管理系统前台</h2>
      <form @submit.prevent="handleLogin">
        <div class="mb-3">
          <label class="form-label">学号</label>
          <input type="text" class="form-control" v-model="form.studentNo" placeholder="请输入学号" required />
        </div>
        <div class="mb-3">
          <label class="form-label">密码</label>
          <input type="password" class="form-control" v-model="form.password" placeholder="请输入密码" required />
        </div>
        <button type="submit" class="btn btn-primary w-100" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
      <div class="text-center mt-3">
        <span>还没有账号？</span><router-link to="/register">立即注册</router-link>
      </div>
    </div>
    <div class="toast-container">
      <div v-if="toast.show" class="alert alert-dismissible fade show" :class="'alert-' + toast.type">
        {{ toast.message }}
        <button type="button" class="btn-close" @click="toast.show = false"></button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const emit = defineEmits(['update-user'])
const form = reactive({ studentNo: '', password: '' })
const loading = ref(false)
const toast = reactive({ show: false, message: '', type: 'danger' })

const showToast = (message, type) => {
  toast.show = true
  toast.message = message
  toast.type = type
  setTimeout(() => toast.show = false, 3000)
}

const handleLogin = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('studentNo', form.studentNo)
    params.append('password', form.password)
    const result = await api.post('/user/login', params)
    if (result.code === 200) {
      emit('update-user')
      router.push('/')
    } else {
      showToast(result.message || '登录失败', 'danger')
    }
  } catch (e) {
    showToast('网络错误，请重试', 'danger')
  } finally {
    loading.value = false
  }
}
</script>