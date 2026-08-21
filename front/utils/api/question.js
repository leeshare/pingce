// utils/api/question.js - 题目接口封装（小程序端）
//
// 对应后端 PracticeController（/api/practice/*），与管理后台接口隔离。
// 后端已强制 bizSection=1 单招、status=2 已通过，前端无需也不可传这两个参数。

const { get, post } = require('../request')

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

/**
 * 按试卷ID拉取一套模考试卷题目
 * GET /api/practice/paper/{paperId}
 *
 * 题目来源 t_paper.question_ids，复合题子题已嵌套，按 question_ids 顺序返回。
 *
 * @param {number} paperId 试卷ID
 * @returns {Promise<Array>} 后端 PracticeQuestionVO 列表（未适配）
 */
function listPaperPractice(paperId) {
  if (!paperId) return Promise.reject(new Error('缺少试卷 id'))
  return get(`/practice/paper/${paperId}`)
}

/**
 * 批量判分并记录错题
 * POST /api/practice/grade
 *
 * 后端统一判分，答错且有正确答案的题目自动记录到 t_wrong_question。
 * 适用于模拟考试整卷提交、真题练习逐题提交。
 *
 * @param {Object} data
 * @param {number} [data.paperId]    试卷ID（模拟考试时传）
 * @param {Array}  data.answers      作答列表
 * @param {number} data.answers[].questionId   题目ID
 * @param {number} [data.answers[].type]       题型
 * @param {number[]} [data.answers[].selected] 选项索引（选择题）
 * @param {string} [data.answers[].text]       文本作答（填空/简答/计算）
 * @param {Array}  [data.answers[].subAnswers] 复合题子题作答
 * @returns {Promise<Object>} 判分结果 { results, summary }
 */
function grade(data) {
  if (!data || !data.answers || !data.answers.length) {
    return Promise.reject(new Error('缺少作答数据'))
  }
  return post('/practice/grade', data, { loading: false })
}

module.exports = {
  listPractice,
  detail,
  listPaperPractice,
  grade,
  SUBJECT_TO_CATEGORY,
}
