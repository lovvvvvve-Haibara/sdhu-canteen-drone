// src/api/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from '../utils/auth'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000
})

// 请求拦截：附带 token
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：统一处理 Result<T>
// src/api/request.js
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 兼容后端格式：
    // 1) { success, message, data }
    if (res && typeof res.success === 'boolean') {
      if (!res.success) {
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || 'Error'))
      }
      return res.data
    }
    // 2) { code, message, data }
    if (typeof res?.code !== 'undefined' && res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    // 3) 其他格式，直接返回
    return typeof res?.code !== 'undefined' ? res.data : res
  },
  (error) => {
    const msg =
      error.response?.data?.message ||
      error.response?.data?.error ||
      (error.response?.status
        ? `请求失败（${error.response.status}）`
        : '') ||
      error.message ||
      '网络异常，请稍后重试'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)


export default service
