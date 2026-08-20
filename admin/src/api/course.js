import request from '@/utils/request'

// 课程分页查询
export function pageCourses(params) {
  return request({
    url: '/admin/course/list',
    method: 'get',
    params,
  })
}

// 课程详情
export function getCourse(id) {
  return request({
    url: `/admin/course/${id}`,
    method: 'get',
  })
}

// 新增课程
export function createCourse(data) {
  return request({
    url: '/admin/course',
    method: 'post',
    data,
  })
}

// 编辑课程
export function updateCourse(data) {
  return request({
    url: '/admin/course',
    method: 'put',
    data,
  })
}

// 删除课程
export function deleteCourse(id) {
  return request({
    url: `/admin/course/${id}`,
    method: 'delete',
  })
}

// 上架/下架
export function toggleCourseStatus(id, status) {
  return request({
    url: `/admin/course/${id}/status`,
    method: 'put',
    params: { status },
  })
}
