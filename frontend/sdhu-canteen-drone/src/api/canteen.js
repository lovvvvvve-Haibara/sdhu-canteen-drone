// src/api/canteen.js
import request from './request'

export function listCanteens(params) {
  // params: { page, size, status? }
  return request({
    url: '/api/canteens',
    method: 'get',
    params
  })
}

export function getCanteenDetail(id) {
  return request({
    url: `/api/canteens/${id}`,
    method: 'get'
  })
}

export function createCanteen(data) {
  return request({
    url: '/api/canteens',
    method: 'post',
    data
  })
}

export function updateCanteen(id, data) {
  return request({
    url: `/api/canteens/${id}`,
    method: 'patch',
    data
  })
}

export function updateCanteenStatus(id, status) {
  return request({
    url: `/api/canteens/${id}/status`,
    method: 'patch',
    params: { status }
  })
}
