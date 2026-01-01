// src/api/order.js
import request from './request'

/**
 * 创建订单（顾客下单）
 */
export function createOrder(data) {
  return request({
    url: '/api/orders',
    method: 'post',
    data
  })
}

/**
 * 获取订单详情
 * GET /api/orders/{id}
 */
export function getOrderDetail(orderId) {
  return request({
    url: `/api/orders/${orderId}`,
    method: 'get'
  })
}

/**
 * 顾客查看自己的订单
 * GET /api/orders/self?userId=&status=&page=&size=
 */
export function listMyOrders(params) {
  return request({
    url: '/api/orders/self',
    method: 'get',
    params
  })
}

export function getMyOrders(params) {
  return listMyOrders(params)
}

/**
 * 顾客取消订单
 * POST /api/orders/{id}/cancel
 */
export function cancelOrder(orderId, reason) {
  return request({
    url: `/api/orders/${orderId}/cancel`,
    method: 'post',
    data: { reason }
  })
}

/**
 * 食堂查看订单列表（分页）
 * GET /api/orders/by-canteen/{canteenId}?status=&page=&size=
 */
export function getCanteenOrders(canteenId, params) {
  return request({
    url: `/api/orders/by-canteen/${canteenId}`,
    method: 'get',
    params
  })
}

/**
 * 更新订单状态
 * POST /api/orders/{id}/status?status=&note=
 */
export function updateOrderStatus(orderId, status, note) {
  return request({
    url: `/api/orders/${orderId}/status`,
    method: 'post',
    params: { status, note }
  })
}

/**
 * 修改配送方式（人工 / 无人机）
 * PATCH /api/orders/{id}/delivery-method?method=
 */
export function changeDeliveryMethod(orderId, method) {
  return request({
    url: `/api/orders/${orderId}/delivery-method`,
    method: 'patch',
    params: { method }
  })
}

/**
 * 为订单分配无人机
 * POST /api/orders/{id}/assign-drone?droneId=
 */
export function assignDroneToOrder(orderId, droneId) {
  return request({
    url: `/api/orders/${orderId}/assign-drone`,
    method: 'post',
    params: { droneId }
  })
}

/**
 * 无人机起飞 → 开始配送
 * POST /api/orders/{id}/start-delivery
 */
export function startDelivery(orderId) {
  return request({
    url: `/api/orders/${orderId}/start-delivery`,
    method: 'post'
  })
}

/**
 * 无人机送达 → 标记完成配送
 * POST /api/orders/{id}/mark-delivered
 */
export function markDelivered(orderId) {
  return request({
    url: `/api/orders/${orderId}/mark-delivered`,
    method: 'post'
  })
}

/**
 * 获取订单时间轴
 * GET /api/orders/{id}/timeline
 */
export function getOrderTimeline(orderId) {
  return request({
    url: `/api/orders/${orderId}/timeline`,
    method: 'get'
  })
}
