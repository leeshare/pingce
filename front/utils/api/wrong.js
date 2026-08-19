// utils/api/wrong.js - 错题本接口封装（小程序端）
//
// 对应后端 WrongQuestionController（/api/wrong/*）。
// 当前仅提供上报接口；错题列表、掌握标记等接口后续按需扩展。

const { post } = require('../request')

/**
 * 上报一道错题
 * POST /api/wrong/report?questionId=123
 *
 * 后端幂等：同一题重复上报会累加 wrong_count，并重置 mastered=0。
 * 调用方无需关心是否已上报过。
 *
 * @param {number} questionId 题目ID（复合题为子题ID）
 * @returns {Promise<boolean>} true=入库成功
 */
function report(questionId) {
  if (!questionId) return Promise.reject(new Error('缺少 questionId'))
  // 后端用 @RequestParam 接 questionId，post 默认走 body，这里显式拼到 query
  return post(`/wrong/report?questionId=${questionId}`)
}

module.exports = {
  report,
}
