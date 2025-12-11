// src/api/user.js
import request from './request'

export function getUserDetail(id) {
  return request({
    url: `/api/users/${id}`,
    method: 'get'
  })
}

export function getUserList(params) {
  // params: { page, size, role?, status? }
  return request({
    url: '/api/users',
    method: 'get',
    params
  })
}

export function searchUsers(params) {
  // params: { keyword, page, size }
  return request({
    url: '/api/users/search',
    method: 'get',
    params
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/api/users/${id}/status`,
    method: 'patch',
    params: { status }
  })
}

export function updateUserRole(id, role) {
  return request({
    url: `/api/users/${id}/role`,
    method: 'patch',
    params: { role }
  })
}
