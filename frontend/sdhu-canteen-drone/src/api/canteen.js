import http from './http'

// 食堂列表：GET /api/canteens?page=&size=&status=
export function listCanteens(params) {
  return http.get('/api/canteens', { params })
}

// 食堂详情：GET /api/canteens/{id}
export function getCanteenById(id) {
  return http.get(`/api/canteens/${id}`)
}
