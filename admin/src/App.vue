<template>
  <div class="app-wrapper">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark" v-if="!isAuthPage">
      <div class="container">
        <router-link class="navbar-brand" to="/">社团活动管理系统后台</router-link>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#adminNavbar" aria-label="导航菜单">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="adminNavbar">
          <ul class="navbar-nav me-auto">
            <li class="nav-item"><router-link class="nav-link" to="/">仪表盘</router-link></li>
            <li class="nav-item"><router-link class="nav-link" to="/club/manage">社团管理</router-link></li>
            <li class="nav-item"><router-link class="nav-link" to="/member/manage">成员审批</router-link></li>
            <li class="nav-item"><router-link class="nav-link" to="/activity/manage">活动管理</router-link></li>
            <li class="nav-item"><router-link class="nav-link" to="/activity/create">发布活动</router-link></li>
          </ul>
          <ul class="navbar-nav">
            <li class="nav-item dropdown" v-if="currentUser">
              <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                 {{ currentUser.name }}
                <span class="badge bg-info ms-1">{{ currentUser.role === 'ADMIN' ? '管理员' : '用户' }}</span>
              </a>
              <ul class="dropdown-menu dropdown-menu-end">
                <li><router-link class="dropdown-item" to="/user/profile">个人中心</router-link></li>
                <li><hr class="dropdown-divider" /></li>
                <li><a class="dropdown-item" href="#" @click.prevent="handleLogout">退出登录</a></li>
              </ul>
            </li>
            <li class="nav-item" v-if="!currentUser">
              <router-link class="nav-link" to="/login">登录</router-link>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <div class="main-content">
      <router-view v-slot="{ Component }" @update-user="fetchUser">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <div class="footer" v-if="!isAuthPage">
      <div class="container">社团活动管理系统(后台)—Wick</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from './api'

const route = useRoute()
const currentUser = ref(null)

const isAuthPage = computed(() => ['Login'].includes(route.name))

const fetchUser = async () => {
  try {
    const result = await api.get('/admin/current')
    if (result.code === 200) currentUser.value = result.data
  } catch (e) {
    currentUser.value = null
  }
}

const handleLogout = async () => {
  try { await api.post('/admin/logout') } catch (e) {}
  window.location.href = '/login'
}

onMounted(() => {
  if (!isAuthPage.value) fetchUser()
})
</script>
