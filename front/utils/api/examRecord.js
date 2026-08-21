// utils/api/examRecord.js - 模考记录接口封装（小程序端）
//
// 对应后端 ExamRecordController（/api/exam-record/*）。
// 数据来自 t_exam_record。

const { get } = require('../request')

/**
 * 查询当前用户的历史模考记录列表
 * GET /api/exam-record/list
 *
 * @returns {Promise<Array>} 后端 ExamRecordListVO 列表（按交卷时间倒序）
 */
function listExamRecords() {
  return get('/exam-record/list')
}

/**
 * 查询单次考试作答详情
 * GET /api/exam-record/{recordId}
 *
 * @param {number} recordId 考试记录ID
 * @returns {Promise<Object>} 后端 ExamRecordDetailVO（含题目列表 + 用户作答）
 */
function getExamRecordDetail(recordId) {
  if (!recordId) return Promise.reject(new Error('缺少记录 id'))
  return get(`/exam-record/${recordId}`)
}

module.exports = {
  listExamRecords,
  getExamRecordDetail,
}
