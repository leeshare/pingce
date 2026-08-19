// utils/api/question.js - 题目接口封装（小程序端）
//
// 对应后端 PracticeController（/api/practice/*），与管理后台接口隔离。
// 后端已强制 bizSection=1 单招、status=2 已通过，前端无需也不可传这两个参数。

const { get } = require('../request')

/**
 * 科目名 → categoryId 映射（来自 seed_question_category.sql）
 * 调用方既可传中文科目名也可直接传 categoryId。
 */
const SUBJECT_TO_CATEGORY = {
  语文: 1,
  数学: 2,
  英语: 3,
  政治: 4,
  历史: 5,
  地理: 6,
  物理: 7,
  化学: 8,
  生物: 9,
  信息技术: 10,
  通用技术: 11,
}

/**
 * 拉取一套真题
 * GET /api/practice/list?year=&categoryId=
 *
 * @param {Object} params
 * @param {number} params.year       真题年份，如 2026
 * @param {number|string} params.categoryId  分类ID（若同时传 subject，以 categoryId 为准）
 * @param {string} [params.subject]  科目名（语文/数学/英语...），无 categoryId 时自动映射
 * @returns {Promise<Array>}         后端 PracticeQuestionVO 列表（未适配）
 */
function listPractice(params) {
  const { year, categoryId, subject } = params || {}
  const cid = categoryId || (subject ? SUBJECT_TO_CATEGORY[subject] : null)
  if (!year || !cid) {
    return Promise.reject(new Error('缺少 year 或 categoryId/subject'))
  }
  return get('/practice/list', { year, categoryId: cid })
}

/**
 * 单题详情（含复合题子题）
 * GET /api/practice/{id}
 *
 * @param {number} id 题目ID
 * @returns {Promise<Object>} 后端 PracticeQuestionVO（未适配）
 */
function detail(id) {
  if (!id) return Promise.reject(new Error('缺少题目 id'))
  return get(`/practice/${id}`)
}

module.exports = {
  listPractice,
  detail,
  SUBJECT_TO_CATEGORY,
}
