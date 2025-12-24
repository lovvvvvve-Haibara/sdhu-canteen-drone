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
    // 后端格式：{ code, message, data }
    if (typeof res.code !== 'undefined' && res.code !== 0) {
      ElMessage.error({
        message: res.message || '请求失败，请稍后重试',
        showClose: true,
        duration: 4000
      })
      return Promise.reject(new Error(res.message || 'Error'))
    }
    // 关键：只返回 res.data
    return res.data  // 这一行决定了 getMyOrders 的返回值
  },
  (error) => {
    const status = error.response?.status
    const backendMessage = error.response?.data?.message
    let msg =
      backendMessage ||
      error.message ||
      '网络异常，请稍后重试'

    if (status === 401) {
      msg = backendMessage || '登录已失效，请重新登录'
    } else if (status === 403) {
      msg = backendMessage || '当前账号无权限执行此操作'
    } else if (status === 404) {
      msg = backendMessage || '接口不存在，请联系管理员'
    } else if (status && status >= 500) {
      msg = backendMessage || '服务器繁忙，请稍后重试'
    }

    ElMessage.error({
      message: msg,
      showClose: true,
      duration: 4500
    })
    return Promise.reject(error)
  }
)


export default service
