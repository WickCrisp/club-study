import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getToken } from '../utils/auth'
import api from '../api'
import { useManagedClubs } from '../composables/useManagedClubs'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/club/list', name: 'ClubList', component: () => import('../views/ClubList.vue') },
  { path: '/club/detail/:id', name: 'ClubDetail', component: () => import('../views/ClubDetail.vue') },
  { path: '/activity/list', name: 'ActivityList', component: () => import('../views/ActivityList.vue') },
  { path: '/activity/detail/:id', name: 'ActivityDetail', component: () => import('../views/ActivityDetail.vue') },
  { path: '/announcement/list', name: 'Announcements', component: () => import('../views/Announcements.vue') },
  { path: '/club/my', name: 'MyClubs', component: () => import('../views/MyClubs.vue') },
  // 社团风采已并入「社团浏览」的卡片，旧链接保留重定向，避免收藏夹失效
  { path: '/showcase', redirect: '/club/list' },
  { path: '/assistant', name: 'Assistant', component: () => import('../views/Assistant.vue') },
  { path: '/user/profile', name: 'UserProfile', component: () => import('../views/UserProfile.vue') },
  { path: '/notifications', name: 'Notifications', component: () => import('../views/Notifications.vue') },
  {
    // 负责人管理区：左侧菜单 + 右侧内容，登录后仍保留门户头
    path: '/manage',
    component: () => import('../layouts/ManageLayout.vue'),
    children: [
      { path: '', name: 'ManageHome', component: () => import('../views/manage/ManageHome.vue') },
      { path: 'approvals', name: 'ManageApprovals', component: () => import('../views/manage/ManageApprovals.vue') },
      { path: 'activities', name: 'ManageActivities', component: () => import('../views/manage/ManageActivities.vue') },
      { path: 'activities/create', name: 'ManageActivityCreate', component: () => import('../views/manage/ManageActivityCreate.vue') },
      { path: 'registrations', name: 'ManageRegistrations', component: () => import('../views/manage/ManageRegistrations.vue') },
      { path: 'club', name: 'ManageClub', component: () => import('../views/manage/ManageClub.vue') },
      { path: 'analytics', name: 'ManageAnalytics', component: () => import('../views/manage/ManageAnalytics.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 允许匿名浏览的页面
const PUBLIC_PAGES = ['Login', 'Register', 'Home', 'ClubList', 'ClubDetail',
  'ActivityList', 'ActivityDetail', 'Announcements']

router.beforeEach(async (to, from, next) => {
  if (PUBLIC_PAGES.includes(to.name)) {
    return next()
  }
  // 本地没有令牌就不必发请求，直接去登录页
  if (!getToken()) {
    return next('/login')
  }
  try {
    // skipAuthRedirect：令牌失效时由这里决定去向，避免拦截器再整页跳一次
    const result = await api.get('/user/current', { skipAuthRedirect: true })
    if (result.code !== 200) {
      return next('/login')
    }
    // 管理区额外要求"是某个社团的负责人"
    if (to.path.startsWith('/manage')) {
      const { ensure } = useManagedClubs()
      const clubs = await ensure(true)
      if (!clubs.length) {
        ElMessage.warning('仅社团负责人可进入管理区')
        return next('/')
      }
    }
    return next()
  } catch (e) {
    return next('/login')
  }
})

export default router
