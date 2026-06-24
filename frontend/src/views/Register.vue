<template>
  <div class="login-container">
    <div class="login-card" style="width: 480px">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="mb-3">
          <label class="form-label">学号</label>
          <input type="text" class="form-control" v-model="form.studentNo" placeholder="请输入学号" required />
        </div>
        <div class="mb-3">
          <label class="form-label">姓名</label>
          <input type="text" class="form-control" v-model="form.name" placeholder="请输入真实姓名" required />
        </div>
        <div class="mb-3">
          <label class="form-label">密码</label>
          <input type="password" class="form-control" v-model="form.password" placeholder="请输入密码" required />
        </div>
        <div class="mb-3">
          <label class="form-label">手机号</label>
          <input type="text" class="form-control" v-model="form.phone" placeholder="请输入手机号" />
        </div>
        <div class="mb-3">
          <label class="form-label">邮箱</label>
          <input type="email" class="form-control" v-model="form.email" placeholder="请输入邮箱" />
        </div>
        <div class="mb-3">
          <label class="form-label">性别</label>
          <select class="form-select" v-model="form.gender">
            <option :value="0">未知</option>
            <option :value="1">男</option>
            <option :value="2">女</option>
          </select>
        </div>
        <button type="submit" class="btn btn-primary w-100" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </form>
      <div class="text-center mt-3">
        <span>已有账号？</span><router-link to="/login">立即登录</router-link>
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
const form = reactive({ studentNo: '', name: '', password: '', phone: '', email: '', gender: 0 })
const loading = ref(false)
const toast = reactive({ show: false, message: '', type: 'danger' })

const showToast = (message, type) => {
  toast.show = true
  toast.message = message
  toast.type = type
  setTimeout(() => toast.show = false, 3000)
}

const handleRegister = async () => {
  loading.value = true
  try {
    const result = await api.post('/user/register', form)
    if (result.code === 200) {
      showToast('注册成功，即将跳转登录页', 'success')
      setTimeout(() => router.push('/login'), 1500)
    } else {
      showToast(result.message || '注册失败', 'danger')
    }
  } catch (e) {
    showToast('网络错误，请重试', 'danger')
  } finally {
    loading.value = false
  }
}
</script>