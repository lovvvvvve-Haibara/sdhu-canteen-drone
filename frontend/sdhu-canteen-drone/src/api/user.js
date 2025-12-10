import http from './http'

// 登录：POST /api/users/login
// 请求体 SelfUser.LoginReq：{ usernameOrPhone, password }
export function login(data) {
  return http.post('/api/users/login', data)
}

// 注册：POST /api/users/register
// 请求体 SelfUser.RegisterReq：{ username, displayName, password }
export function register(data) {
  return http.post('/api/users/register', data)
}

// 获取当前用户信息：GET /api/users/self?userId=...
export function fetchSelf(userId) {
  return http.get('/api/users/self', { params: { userId } })
}
