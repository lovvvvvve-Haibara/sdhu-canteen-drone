import http from './http'

// 分类列表：GET /api/canteens/{canteenId}/menu/categories
export function listCategories(canteenId) {
  return http.get(`/api/canteens/${canteenId}/menu/categories`)
}

// 菜品列表（分页、支持分类等筛选）：
// GET /api/canteens/{canteenId}/menu/foods?categoryId=&keyword=&page=&size=
export function listFoods(canteenId, params) {
  return http.get(`/api/canteens/${canteenId}/menu/foods`, { params })
}

// 单个菜品详情：GET /api/canteens/{canteenId}/menu/foods/{foodId}
export function getFoodDetail(canteenId, foodId) {
  return http.get(`/api/canteens/${canteenId}/menu/foods/${foodId}`)
}
