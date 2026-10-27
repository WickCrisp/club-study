import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'
import api from '../api'

const routes = [
  { path: '/', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/approval', name: 'ApprovalCenter', component: () => import('../views/ApprovalCenter.vue') },
  { path: '/club/manage', name: 'ClubManage', component: () => import('../views/ClubManage.vue') },
  { path: '/category/manage', name: 'CategoryManage', component: () => import('../views/CategoryManage.vue') },
  { path: '/member/manage', name: 'MemberManage', component: () => import('../views/MemberManage.vue') },
  { path: '/activity/create', name: 'ActivityCreate', component: () => import('../views/ActivityCreate.vue') },
  { path: '/activity/manage', name: 'ActivityManage', component: () => import('../views/ActivityManage.vue') },
  { path: '/registration/manage', name: 'RegistrationManage', component: () => import('../views/RegistrationManage.vue') },
  { path: '/announcement/manage', name: 'AnnouncementManage', component: () => import('../views/AnnouncementManage.vue') },
  { path: '/user/manage', name: 'UserManage', component: () => import('../views/UserManage.vue') },
  { path: '/user/profile', name: 'UserProfile', component: () => import('../views/UserProfile.vue') },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_ADMIN_BASE || '/'),
  routes
})

router.beforeEach(async (to, from, next) => {
  // 登录页无需认证
  if (to.name === 'Login') {
    return next()
  }
  if (!getToken()) {
    return next('/login')
  }
  try {
    // 管理端不区分角色：社团负责人也用这里，具体接口权限由后端 403 体现
    const result = await api.get('/admin/current', { skipAuthRedirect: true })
    if (result.code === 200) {
      return next()
    }
    return next('/login')
  } catch (e) {
    return next('/login')
  }
})

export default router
