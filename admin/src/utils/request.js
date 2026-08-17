import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || '/api',
  timeout: 10000,
})

// 请求拦截
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout().finally(() => {
        window.location.href = '/login'
      })
      return Promise.reject(new Error('登录已过期，请重新登录'))
    }
    // 优先取后端业务消息（如"题干与现有题目重复..."），fallback 到 axios 默认 message
    const bizMsg = error.response?.data?.message
    const msg = bizMsg || error.message || '网络错误'
    ElMessage.error(msg)
    // 抛 Error 实例而非 axios 原始 error 对象，避免 dev-server overlay 显示 [object Object]
    return Promise.reject(new Error(msg))
  },
)

export default service
