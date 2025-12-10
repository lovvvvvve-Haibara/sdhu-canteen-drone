import http from './http'

// 创建订单：POST /api/orders
// 请求体 OrderDto.OrderCreateReq：
// {
//   customerId: number,
//   canteenId: number,
//   deliveryMethod: "DRONE" | "MANUAL",
//   deliveryAddress: string,
//   items: [{ foodId: number, qty: number }]
// }
export function createOrder(data) {
  return http.post('/api/orders', data)
}

// 我的订单列表：GET /api/orders/self?userId=&status=&page=&size=
export function listMyOrders(params) {
  return http.get('/api/orders/self', { params })
}

// 订单详情：GET /api/orders/{id}
export function getOrderDetail(id) {
  return http.get(`/api/orders/${id}`)
}
