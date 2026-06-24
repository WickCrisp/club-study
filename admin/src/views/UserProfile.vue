<template>
  <div class="container">
    <div class="page-header">
      <h2>个人中心</h2>
    </div>

    <div class="row">
      <div class="col-md-8">
        <div class="card mb-4">
          <div class="card-header">个人信息</div>
          <div class="card-body">
            <form @submit.prevent="handleUpdate">
              <div class="mb-3">
                <label class="form-label">学号</label>
                <input type="text" class="form-control" :value="form.studentNo" disabled />
              </div>
              <div class="mb-3">
                <label class="form-label">姓名</label>
                <input type="text" class="form-control" v-model="form.name" required />
              </div>
              <div class="mb-3">
                <label class="form-label">手机号</label>
                <input type="text" class="form-control" v-model="form.phone" />
              </div>
              <div class="mb-3">
                <label class="form-label">邮箱</label>
                <input type="email" class="form-control" v-model="form.email" />
              </div>
              <div class="mb-3">
                <label class="form-label">性别</label>
                <select class="form-select" v-model="form.gender">
                  <option :value="0">未知</option>
                  <option :value="1">男</option>
                  <option :value="2">女</option>
                </select>
              </div>
              <button type="submit" class="btn btn-primary" :disabled="loading">保存修改</button>
            </form>
          </div>
        </div>

        <div class="card mb-4">
          <div class="card-header">修改密码</div>
          <div class="card-body">
            <form @submit.prevent="handlePasswordChange">
              <div class="mb-3">
                <label class="form-label">旧密码</label>
                <input type="password" class="form-control" v-model="pwdForm.oldPassword" required />
              </div>
              <div class="mb-3">
                <label class="form-label">新密码</label>
                <input type="password" class="form-control" v-model="pwdForm.newPassword" required />
              </div>
              <button type="submit" class="btn btn-warning" :disabled="pwdLoading">修改密码</button>
            </form>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card mb-4">
          <div class="card-header">账号信息</div>
          <div class="card-body">
            <p><strong>角色：</strong>
              <span class="badge" :class="form.role === 'ADMIN' ? 'bg-danger' : 'bg-info'">
                {{ form.role === 'ADMIN' ? '管理员' : '学生' }}
              </span>
            </p>
            <p><strong>注册时间：</strong> {{ form.createdAt || '-' }}</p>
          </div>
        </div>
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
import { ref, reactive, onMounted } from 'vue'
import api from '../api'

const form = reactive({ studentNo: '', name: '', phone: '', email: '', gender: 0, role: '', createdAt: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const loading = ref(false)
const pwdLoading = ref(false)
const toast = reactive({ show: false, message: '', type: 'danger' })

const emit = defineEmits(['update-user'])

const showToast = (message, type) => {
  toast.show = true; toast.message = message; toast.type = type
  setTimeout(() => toast.show = false, 3000)
}

const loadUser = async () => {
  try {
    const result = await api.get('/admin/current')
    if (result.code === 200) Object.assign(form, result.data)
  } catch (e) { showToast('加载用户信息失败', 'danger') }
}

const handleUpdate = async () => {
  loading.value = true
  try {
    const result = await api.put('/admin/update', { name: form.name, phone: form.phone, email: form.email, gender: form.gender })
    if (result.code === 200) { showToast('更新成功', 'success'); emit('update-user') }
    else showToast(result.message, 'danger')
  } catch (e) { showToast('更新失败', 'danger') }
  finally { loading.value = false }
}

const handlePasswordChange = async () => {
  pwdLoading.value = true
  try {
    const params = new URLSearchParams()
    params.append('oldPassword', pwdForm.oldPassword)
    params.append('newPassword', pwdForm.newPassword)
    const result = await api.put('/admin/updatePassword', params)
    if (result.code === 200) {
      showToast('密码修改成功', 'success')
      pwdForm.oldPassword = ''; pwdForm.newPassword = ''
    } else showToast(result.message, 'danger')
  } catch (e) { showToast('修改失败', 'danger') }
  finally { pwdLoading.value = false }
}

onMounted(() => loadUser())
</script>
