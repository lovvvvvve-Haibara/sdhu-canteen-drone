// src/api/drone.js
import request from './request'

/**
 * 分页查询无人机列表
 * GET /api/drones?status=&page=&size=
 */
export function listDrones(params) {
  return request({
    url: '/api/drones',
    method: 'get',
    params
  })
}

/**
 * 获取无人机详情
 * GET /api/drones/{id}
 */
export function getDroneDetail(id) {
  return request({
    url: `/api/drones/${id}`,
    method: 'get'
  })
}

/**
 * 新增无人机
 * POST /api/drones
 */
export function createDrone(data) {
  return request({
    url: '/api/drones',
    method: 'post',
    data
  })
}

/**
 * 更新无人机基本信息
 * PATCH /api/drones/{id}
 */
export function updateDrone(id, data) {
  return request({
    url: `/api/drones/${id}`,
    method: 'patch',
    data
  })
}

/**
 * 修改无人机状态
 * PATCH /api/drones/{id}/status?status=IDLE/IN_MISSION/MAINTENANCE
 */
export function changeDroneStatus(id, status) {
  return request({
    url: `/api/drones/${id}/status`,
    method: 'patch',
    params: { status }
  })
}

/**
 * 删除无人机
 * DELETE /api/drones/{id}/delete
 */
export function deleteDrone(id) {
  return request({
    url: `/api/drones/${id}/delete`,
    method: 'delete'
  })
}
