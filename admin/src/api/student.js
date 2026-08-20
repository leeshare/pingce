import request from '@/utils/request'

// 学员分页查询
export function pageStudents(params) {
  return request({
    url: '/admin/student/page',
    method: 'get',
    params,
  })
}
