<template>
  <router-view v-if="isAuthPage" v-slot="{ Component }" @update-user="fetchUser">
    <component :is="Component" />
  </router-view>

  <el-container v-else class="admin-shell">
    <el-aside :width="collapsed ? '64px' : '224px'" class="sidebar">
      <div class="brand">
        <div class="brand-logo">社</div>
        <span v-if="!collapsed" class="brand-name">社团管理后台</span>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="route.path"
          :collapse="collapsed"
          :collapse-transition="false"
          router
          class="side-menu"
        >
          <el-menu-item-group v-for="group in menus" :key="group.title" :title="group.title">
            <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>
                <span>{{ item.label }}</span>
                <span v-if="item.path === '/approval' && pendingCount > 0" class="menu-count">
                  {{ pendingCount }}
                </span>
              </template>
            </el-menu-item>
          </el-menu-item-group>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header height="56px" class="topbar">
        <div class="topbar-left">
          <el-button text :icon="collapsed ? Expand : Fold" @click="collapsed = !collapsed" />
          <span class="crumb">后台管理 / <b>{{ currentLabel }}</b></span>
        </div>
        <div class="topbar-right">
          <el-tag v-if="currentUser" :type="currentUser.role === 'ADMIN' ? 'danger' : 'info'" effect="plain" round>
            {{ currentUser.role === 'ADMIN' ? '管理员' : '用户' }}
          </el-tag>
          <el-dropdown trigger="click" @command="onUserCommand">
            <span class="user-chip">
              <el-avatar :size="28" class="avatar">{{ avatarText }}</el-avatar>
              <span class="user-name">{{ currentUser ? currentUser.name : '未登录' }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>

      <el-footer height="44px" class="footer">社团活动管理系统（后台）— Wick</el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Odometer, Stamp, OfficeBuilding, Grid, UserFilled, Calendar, EditPen,
  Tickets, Document, User, Fold, Expand, ArrowDown
} from '@element-plus/icons-vue'
import api from './api'
import { getToken, clearToken } from './utils/auth'

const route = useRoute()
const router = useRouter()

const currentUser = ref(null)
const pendingCount = ref(0)
const collapsed = ref(false)

const isAuthPage = computed(() => ['Login'].includes(route.name))

// 菜单即路由表：一处维护，侧边栏和顶部标题都从这里取
const menus = [
  { title: '概览', items: [{ path: '/', label: '仪表盘', icon: Odometer }] },
  { title: '审批', items: [{ path: '/approval', label: '待办中心', icon: Stamp }] },
  {
    title: '社团',
    items: [
      { path: '/club/manage', label: '社团管理', icon: OfficeBuilding },
      { path: '/category/manage', label: '分类管理', icon: Grid },
      { path: '/member/manage', label: '成员审批', icon: UserFilled }
    ]
  },
  {
    title: '活动',
    items: [
      { path: '/activity/manage', label: '活动管理', icon: Calendar },
      { path: '/activity/create', label: '发布活动', icon: EditPen },
      { path: '/registration/manage', label: '报名管理', icon: Tickets }
    ]
  },
  {
    title: '内容与系统',
    items: [
      { path: '/announcement/manage', label: '公告管理', icon: Document },
      { path: '/user/manage', label: '用户管理', icon: User }
    ]
  }
]

const currentLabel = computed(() => {
  const hit = menus.flatMap(g => g.items).find(i => i.path === route.path)
  if (hit) return hit.label
  if (route.path === '/user/profile') return '个人中心'
  return '后台管理'
})

const avatarText = computed(() => (currentUser.value && currentUser.value.name ? currentUser.value.name.slice(0, 1) : '?'))

const fetchUser = async () => {
  if (!getToken()) {
    currentUser.value = null
    return
  }
  try {
    const result = await api.get('/admin/current', { skipAuthRedirect: true })
    if (result.code === 200) currentUser.value = result.data
  } catch (e) {
    currentUser.value = null
  }
}

const fetchPendingCount = async () => {
  try {
    const result = await api.get('/approval/count')
    if (result.code === 200) pendingCount.value = result.data || 0
  } catch (e) {
    pendingCount.value = 0
  }
}

const handleLogout = async () => {
  try { await api.post('/admin/logout') } catch (e) {}
  clearToken()
  // 跟随部署前缀：/admin/ 部署时回 /admin/login，根路径部署时回 /login
  window.location.href = `${import.meta.env.VITE_ADMIN_BASE || '/'}login`
}

const onUserCommand = (command) => {
  if (command === 'profile') router.push('/user/profile')
  if (command === 'logout') handleLogout()
}

onMounted(() => {
  if (!isAuthPage.value) {
    fetchUser()
    fetchPendingCount()
  }
})
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
}

.sidebar {
  background: #1a1a2e;
  display: flex;
  flex-direction: column;
  transition: width 0.2s;
  overflow: hidden;
}

.brand {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex: none;
}

.brand-logo {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: #ffffff;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  flex: none;
}

.brand-name {
  color: #ffffff;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.menu-scroll {
  flex: 1;
}

.menu-count {
  margin-left: auto;
  background: #f56c6c;
  color: #ffffff;
  border-radius: 9px;
  font-size: 11px;
  padding: 0 6px;
  height: 18px;
  line-height: 18px;
}

.topbar {
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.crumb {
  font-size: 13px;
  color: #8a94a6;
}

.crumb b {
  color: #1f2d3d;
  font-weight: 500;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.avatar {
  background: #1a1a2e;
  color: #ffffff;
  font-size: 13px;
}

.user-name {
  font-size: 13px;
  color: #1f2d3d;
}

.arrow {
  color: #9aa4b8;
  font-size: 12px;
}

.main {
  padding: 0;
  background: var(--el-bg-color-page);
}

.footer {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #9aa4b8;
  background: transparent;
}
</style>
