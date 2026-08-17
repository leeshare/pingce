import request from '@/utils/request'

// 管理员登录
export function login(data) {
  return request({
    url: '/admin/login',
    method: 'post',
    data,
  })
}

// 获取当前管理员信息
export function getInfo() {
  return request({
    url: '/admin/info',
    method: 'get',
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/admin/logout',
    method: 'post',
  })
}
