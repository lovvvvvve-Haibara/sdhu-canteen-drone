import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/', // 走 Vite 代理
  timeout: 10000,
})

http.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res && typeof res === 'object') {
      // ① 优先处理 { code, message, data } 这种结构（你刚贴的登录成功就是这个）
      if ('code' in res) {
        if (res.code === 0) {
          // ✅ 统一：返回 data 部分给业务层
          return res.data
        } else {
          ElMessage.error(res.message || '请求失败')
          return Promise.reject(new Error(res.message || '请求失败'))
        }
      }

      // ② 兼容之前看到的 { success, message, data } 结构
      if ('success' in res) {
        if (res.success) {
          return res.data
        } else {
          ElMessage.error(res.message || '请求失败')
          return Promise.reject(new Error(res.message || '请求失败'))
        }
      }
    }

    // ③ 兜底：不符合统一格式，就原样返回
    return res
  },
  (error) => {
    const msg =
      error?.response?.data?.message ||
      error?.response?.data?.error ||
      error.message ||
      '网络错误'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default http
