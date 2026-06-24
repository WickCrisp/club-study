import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/club/list', name: 'ClubList', component: () => import('../views/ClubList.vue') },
  { path: '/club/detail/:id', name: 'ClubDetail', component: () => import('../views/ClubDetail.vue') },
  { path: '/activity/list', name: 'ActivityList', component: () => import('../views/ActivityList.vue') },
  { path: '/activity/detail/:id', name: 'ActivityDetail', component: () => import('../views/ActivityDetail.vue') },
  { path: '/user/profile', name: 'UserProfile', component: () => import('../views/UserProfile.vue') },
  { path: '/notifications', name: 'Notifications', component: () => import('../views/Notifications.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  // 登录和注册页无需认证
  if (to.name === 'Login' || to.name === 'Register') {
    return next()
  }
  // 首页允许未登录浏览
  if (to.name === 'Home') {
    return next()
  }
  // 社团和活动浏览允许未登录浏览
  if (to.name === 'ClubList' || to.name === 'ClubDetail' || to.name === 'ActivityList' || to.name === 'ActivityDetail') {
    return next()
  }
  // 通知中心需要登录
  if (to.name === 'Notifications') {
    try {
      const resp = await fetch('/api/user/current')
      const result = await resp.json()
      if (result.code === 200) return next()
    } catch (e) {}
    return next('/login')
  }
  // 其余页面（个人信息等）需要登录
  try {
    const resp = await fetch('/api/user/current')
    const result = await resp.json()
    if (result.code === 200) {
      next()
    } else {
      next('/login')
    }
  } catch (e) {
    next('/login')
  }
})

export default router
