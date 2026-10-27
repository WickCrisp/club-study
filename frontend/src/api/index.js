import axios from 'axios'
import { getToken, clearToken } from '../utils/auth'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

const toLogin = () => {
  if (window.location.pathname !== '/login') {
    window.location.replace('/login')
  }
}

// 每个请求自动带上令牌；服务端只认 Authorization: Bearer，不依赖 Cookie
api.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * 响应约定：
 * - 成功：HTTP 200，返回 Result{code,message,data} 本体（页面直接读 result.data）
 * - 未登录：HTTP 401 + code=401，清令牌并跳登录
 * - 其他失败：HTTP 4xx/5xx + 对应 code，原样返回给页面读 message
 *
 * 个别请求（如获取当前用户）可传 { skipAuthRedirect: true }，
 * 这样令牌过期时页面可以先按未登录渲染，而不是立刻整页跳转。
 */
api.interceptors.response.use(
  response => {
    const result = response.data
    if (result && result.code === 401) {
      if (!response.config.skipAuthRedirect) {
        clearToken()
        toLogin()
      }
      return Promise.reject(new Error(result.message || '请先登录'))
    }
    return result
  },
  error => {
    const body = error.response && error.response.data
    const skipRedirect = Boolean(error.config && error.config.skipAuthRedirect)
    if (body && typeof body.code === 'number') {
      if (body.code === 401) {
        if (!skipRedirect) {
          clearToken()
          toLogin()
        }
        return Promise.reject(new Error(body.message || '请先登录'))
      }
      return body
    }
    return Promise.reject(error)
  }
)

export default api
