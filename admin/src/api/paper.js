import request from '@/utils/request'

const BASE = '/admin/paper'

// 分页查询试卷
// params: PaperQueryDTO { bizSection, categoryId, status, keyword, page, size }
export function listPapers(params) {
  return request({ url: `${BASE}/list`, method: 'get', params })
}

// 试卷详情
export function getPaperDetail(id) {
  return request({ url: `${BASE}/${id}`, method: 'get' })
}

// 试卷预览（试卷元数据 + 按顺序的题目列表，含复合题子题）
export function previewPaper(id) {
  return request({ url: `${BASE}/${id}/preview`, method: 'get' })
}

// 新增试卷
export function createPaper(data) {
  return request({ url: BASE, method: 'post', data })
}

// 更新试卷
export function updatePaper(data) {
  return request({ url: BASE, method: 'put', data })
}

// 删除试卷
export function deletePaper(id) {
  return request({ url: `${BASE}/${id}`, method: 'delete' })
}

// 切换发布状态（0草稿 1已发布）
export function updatePaperStatus(id, status) {
  return request({ url: `${BASE}/${id}/status`, method: 'put', params: { status } })
}
