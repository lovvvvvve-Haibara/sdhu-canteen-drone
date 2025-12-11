// src/api/menu.js
import request from './request'

export function getCategories(canteenId) {
  return request({
    url: `/api/canteens/${canteenId}/menu/categories`,
    method: 'get'
  })
}

export function createCategory(canteenId, data) {
  return request({
    url: `/api/canteens/${canteenId}/menu/categories`,
    method: 'post',
    data
  })
}

export function updateCategory(canteenId, categoryId, data) {
  return request({
    url: `/api/canteens/${canteenId}/menu/categories/${categoryId}`,
    method: 'patch',
    data
  })
}

export function deleteCategory(canteenId, categoryId) {
  return request({
    url: `/api/canteens/${canteenId}/menu/categories/${categoryId}`,
    method: 'delete'
  })
}

export function listFoods(canteenId, params) {
  // params: { categoryId?, shelfStatus?, keyword?, page, size }
  return request({
    url: `/api/canteens/${canteenId}/menu/foods`,
    method: 'get',
    params
  })
}

export function getFoodDetail(canteenId, foodId) {
  return request({
    url: `/api/canteens/${canteenId}/menu/foods/${foodId}`,
    method: 'get'
  })
}

export function createFood(canteenId, data) {
  return request({
    url: `/api/canteens/${canteenId}/menu/foods`,
    method: 'post',
    data
  })
}

export function updateFood(canteenId, foodId, data) {
  return request({
    url: `/api/canteens/${canteenId}/menu/foods/${foodId}`,
    method: 'patch',
    data
  })
}

export function updateFoodShelf(canteenId, foodId, onShelf) {
  return request({
    url: `/api/canteens/${canteenId}/menu/foods/${foodId}/shelf`,
    method: 'patch',
    params: { onShelf }
  })
}

export function updateFoodStock(canteenId, foodId, stock) {
  return request({
    url: `/api/canteens/${canteenId}/menu/foods/${foodId}/stock`,
    method: 'patch',
    params: { stock }
  })
}
