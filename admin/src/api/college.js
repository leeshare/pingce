import request from '@/utils/request'

// 院校分页查询
export function pageColleges(params) {
  return request({
    url: '/admin/college/list',
    method: 'get',
    params,
  })
}

// 院校详情
export function getCollege(id) {
  return request({
    url: `/admin/college/${id}`,
    method: 'get',
  })
}

// 新增院校
export function createCollege(data) {
  return request({
    url: '/admin/college',
    method: 'post',
    data,
  })
}

// 编辑院校
export function updateCollege(data) {
  return request({
    url: '/admin/college',
    method: 'put',
    data,
  })
}

// 删除院校
export function deleteCollege(id) {
  return request({
    url: `/admin/college/${id}`,
    method: 'delete',
  })
}
