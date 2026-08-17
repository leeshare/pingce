import request from '@/utils/request'

const BASE = '/admin/question'

// ==================== 1. 试题录入 / 编辑 ====================

// 新增题目（录入）
// data: QuestionCreateDTO
export function createQuestion(data) {
  return request({ url: BASE, method: 'post', data })
}

// 更新题目（编辑/校对保存共用）
export function updateQuestion(data) {
  return request({ url: BASE, method: 'put', data })
}

// 题目详情
export function getQuestionDetail(id) {
  return request({ url: `${BASE}/${id}`, method: 'get' })
}

// 复合题(type=7)子题列表
export function getQuestionChildren(id) {
  return request({ url: `${BASE}/${id}/children`, method: 'get' })
}

// 删除题目
export function deleteQuestion(id) {
  return request({ url: `${BASE}/${id}`, method: 'delete' })
}

// 批量删除
export function batchDeleteQuestion(ids) {
  return request({ url: `${BASE}/batch-delete`, method: 'delete', params: { ids: ids.join(',') } })
}

// ==================== 2. 批量导入 ====================

// 导入 Excel（multipart/form-data）
// params: { bizSection, categoryId, year, source, status }
export function importQuestion(file, params) {
  const formData = new FormData()
  formData.append('file', file)
  if (params) {
    Object.keys(params).forEach((k) => {
      if (params[k] !== undefined && params[k] !== null && params[k] !== '') {
        formData.append(k, params[k])
      }
    })
  }
  return request({
    url: `${BASE}/import`,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000,
  })
}

// 批次列表
export function listImportBatch(params) {
  return request({ url: `${BASE}/batch`, method: 'get', params })
}

// 批次详情
export function getImportBatch(batchId) {
  return request({ url: `${BASE}/batch/${batchId}`, method: 'get' })
}

// ==================== 3. 试题校对 ====================

// 校对列表（status=1 待审核）
export function listForProofread(params) {
  return request({ url: `${BASE}/proofread`, method: 'get', params })
}

// 校对保存
export function proofreadSave(data) {
  return request({ url: `${BASE}/proofread`, method: 'put', data })
}

// ==================== 4. 试题编辑 ====================

// 编辑列表（全部状态）
export function listForEdit(params) {
  return request({ url: `${BASE}/list`, method: 'get', params })
}

// ==================== 5. 试题审核 ====================

// 审核（批量通过/驳回）
// data: { ids: [], status: 2|3, remark }
export function reviewQuestion(data) {
  return request({ url: `${BASE}/review`, method: 'post', data })
}

// ==================== 通用 ====================

// 题库中心统计
export function getQuestionStat() {
  return request({ url: `${BASE}/stat`, method: 'get' })
}

// 分类列表（用于下拉选择）
export function listCategories() {
  return request({ url: `${BASE}/categories`, method: 'get' })
}
