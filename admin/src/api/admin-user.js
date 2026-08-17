import request from '@/utils/request'

// 分页查询管理员
export function pageAdminUsers(params) {
  return request({
    url: '/admin/user/page',
    method: 'get',
    params,
  })
}

// 管理员详情
export function getAdminUser(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'get',
  })
}

// 新增管理员
export function createAdminUser(data) {
  return request({
    url: '/admin/user',
    method: 'post',
    data,
  })
}

// 更新管理员
export function updateAdminUser(id, data) {
  return request({
    url: `/admin/user/${id}`,
    method: 'put',
    data,
  })
}

// 删除管理员
export function deleteAdminUser(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'delete',
  })
}

// 分配权限
export function assignPermissions(id, permissions) {
  return request({
    url: `/admin/user/${id}/permissions`,
    method: 'put',
    data: { permissions },
  })
}

// 内置权限清单
export function listPermissionOptions() {
  return request({
    url: '/admin/user/permission-options',
    method: 'get',
  })
}
