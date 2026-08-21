// utils/api/paper.js - 试卷接口封装（小程序端）
//
// 对应后端 PaperController（/api/paper/*）。
// 模考试卷数据来自 t_paper 表，按 bizSection=1(单招)、status=1(已发布) 查询。

const { get } = require('../request')

/**
 * 拉取模考试卷列表
 * GET /api/paper/list?bizSection=1&status=1
 *
 * 不再用 keyword 过滤，t_paper 表里的单招已发布试卷即为模考卷来源。
 *
 * @param {Object} [options]
 * @param {number} [options.page=1]     页码
 * @param {number} [options.size=20]    每页数量
 * @param {number} [options.year]       年份筛选
 * @returns {Promise<Object>} PageResult<Paper>  { total, current, size, records }
 */
function listMockPapers(options) {
  const { page = 1, size = 20, year } = options || {}
  // 过滤掉 undefined/null，避免 wx.request 把 year=undefined 拼到 query string
  // 导致后端 Integer 绑定失败返回 400
  const params = {
    bizSection: 1,
    status: 1,
    page,
    size,
  }
  if (year != null) params.year = year
  return get('/paper/list', params)
}

/**
 * 试卷详情
 * GET /api/paper/{id}
 *
 * @param {number} id 试卷ID
 * @returns {Promise<Object>} Paper
 */
function detail(id) {
  if (!id) return Promise.reject(new Error('缺少试卷 id'))
  return get(`/paper/${id}`)
}

/**
 * 根据 questionIds 字段计算题目数量
 * @param {string} questionIds 逗号分隔的题目ID
 * @returns {number}
 */
function countQuestions(questionIds) {
  if (!questionIds) return 0
  return questionIds.split(',').filter((s) => s && s.trim()).length
}

module.exports = {
  listMockPapers,
  detail,
  countQuestions,
}
