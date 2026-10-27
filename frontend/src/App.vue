<template>
  <router-view v-if="isAuthPage" v-slot="{ Component }" @update-user="fetchUser">
    <component :is="Component" />
  </router-view>

  <div v-else class="portal">
    <header class="portal-header">
      <div class="header-inner">
        <router-link to="/" class="brand">
          <span class="brand-logo">社</span>
          <span class="brand-text">社团活动平台</span>
        </router-link>

        <el-menu
          mode="horizontal"
          :default-active="activePath"
          :ellipsis="false"
          router
          class="nav"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/announcement/list">公告</el-menu-item>
          <el-menu-item index="/club/list">社团浏览</el-menu-item>
          <el-menu-item index="/activity/list">活动浏览</el-menu-item>
          <el-menu-item index="/club/my">我参加的社团</el-menu-item>
          <el-menu-item v-if="isLeader" index="/manage">社团管理</el-menu-item>
          <el-menu-item index="/assistant">AI 助手</el-menu-item>
        </el-menu>

        <div class="header-right">
          <el-dropdown v-if="currentUser" trigger="click" @command="onCommand">
            <span class="user-chip">
              <el-avatar :size="28" class="avatar">{{ avatarText }}</el-avatar>
              <span class="user-name">{{ currentUser.name }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="notifications">通知中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button v-else type="primary" @click="$router.push('/login')">登录</el-button>
        </div>
      </div>
    </header>

    <main class="portal-main">
      <router-view v-slot="{ Component }" @update-user="fetchUser">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="portal-footer">社团活动管理系统（前台）— Wick</footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import api from './api'
import { getToken, clearToken } from './utils/auth'
import { useManagedClubs } from './composables/useManagedClubs'

const route = useRoute()
const router = useRouter()
const currentUser = ref(null)
const { isLeader, ensure, reset } = useManagedClubs()

const isAuthPage = computed(() => ['Login', 'Register'].includes(route.name))
// /manage/xxx 也要高亮「社团管理」这一项
const activePath = computed(() => (route.path.startsWith('/manage') ? '/manage' : route.path))
const avatarText = computed(() => (currentUser.value && currentUser.value.name ? currentUser.value.name.slice(0, 1) : '?'))

const fetchUser = async () => {
  if (!getToken()) {
    currentUser.value = null
    return
  }
  try {
    const result = await api.get('/user/current', { skipAuthRedirect: true })
    if (result.code === 200) {
      currentUser.value = result.data
      ensure()
    } else {
      reset()
    }
  } catch (e) {
    currentUser.value = null
    reset()
  }
}

const handleLogout = async () => {
  try { await api.post('/user/logout') } catch (e) {}
  clearToken()
  window.location.href = '/login'
}

const onCommand = (command) => {
  if (command === 'profile') router.push('/user/profile')
  if (command === 'notifications') router.push('/notifications')
  if (command === 'logout') handleLogout()
}

onMounted(() => {
  if (!isAuthPage.value) fetchUser()
})
</script>

<style scoped>
.portal {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.portal-header {
  background: #1a1a2e;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1180px;
  margin: 0 auto;
  padding: 0 16px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  flex: none;
}

.brand-logo {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #ffffff;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 500;
}

.brand-text {
  color: #ffffff;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.nav {
  flex: 1;
  height: 60px;
  border-bottom: none;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #c9d1e0;
  --el-menu-active-color: #ffffff;
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.08);
  --el-menu-hover-text-color: #ffffff;
  --el-menu-item-font-size: 14px;
}

.nav :deep(.el-menu-item) {
  height: 60px;
  line-height: 60px;
  border-bottom: 2px solid transparent;
}

.nav :deep(.el-menu-item.is-active) {
  border-bottom-color: #ffffff;
  background: transparent;
}

.nav :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06);
}

.header-right {
  flex: none;
  display: flex;
  align-items: center;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
  color: #ffffff;
}

.avatar {
  background: #39445c;
  color: #ffffff;
  font-size: 13px;
}

.user-name {
  font-size: 13px;
  color: #e3e8f0;
}

.arrow {
  font-size: 12px;
  color: #8f9bb3;
}

.portal-main {
  flex: 1;
}

.portal-footer {
  background: #1a1a2e;
  color: #8a94a6;
  text-align: center;
  padding: 18px 0;
  font-size: 12px;
}
</style>
