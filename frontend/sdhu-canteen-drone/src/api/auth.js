// src/api/auth.js
import request from './request'

export function login(data) {
  // data: { usernameOrPhone, password }
  return request({
    url: '/api/users/login',
    method: 'post',
    data
  })
}

export function register(data) {
  // data: { username, displayName, password, phone? }
  return request({
    url: '/api/users/register',
    method: 'post',
    data
  })
}
