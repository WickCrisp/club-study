import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

api.interceptors.response.use(
  response => {
    const result = response.data
    if (result.code === 401) {
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(new Error(result.message || '请先登录'))
    }
    return result
  },
  error => {
    return Promise.reject(error)
  }
)

export default api