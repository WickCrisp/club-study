import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/club/manage', name: 'ClubManage', component: () => import('../views/ClubManage.vue') },
  { path: '/member/manage', name: 'MemberManage', component: () => import('../views/MemberManage.vue') },
  { path: '/activity/create', name: 'ActivityCreate', component: () => import('../views/ActivityCreate.vue') },
  { path: '/activity/manage', name: 'ActivityManage', component: () => import('../views/ActivityManage.vue') },
  { path: '/user/profile', name: 'UserProfile', component: () => import('../views/UserProfile.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  // 登录页无需认证
  if (to.name === 'Login') {
    return next()
  }
  try {
    const resp = await fetch('/api/user/current')
    const result = await resp.json()
    if (result.code === 200) {
      const user = result.data
      // 后台仅允许 ADMIN 或社团负责人访问
      if (user.role === 'ADMIN') {
        return next()
      }
      // 也允许普通用户访问（他们可能是社团负责人）
      // 具体权限由后端 API 控制
      return next()
    } else {
      next('/login')
    }
  } catch (e) {
    next('/login')
  }
})

export default router
