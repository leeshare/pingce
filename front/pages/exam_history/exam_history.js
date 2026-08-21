// pages/exam_history/exam_history.js
// 历史成绩列表：数据来自 t_exam_record，点击进入作答详情。
const { listExamRecords } = require('../../utils/api/examRecord')

Page({
  data: {
    records: [],
    loading: true,
    loadError: false,
  },

  onLoad() {
    this.loadRecords()
  },

  onPullDownRefresh() {
    this.loadRecords(() => wx.stopPullDownRefresh())
  },

  loadRecords(done) {
    this.setData({ loading: true, loadError: false })
    listExamRecords()
      .then((list) => {
        const records = (list || []).map((r) => Object.assign({}, r, {
          isPass: (r.totalScore || 0) > 0 && (r.score || 0) >= (r.totalScore || 0) * 0.6,
        }))
        this.setData({ records, loading: false })
      })
      .catch(() => {
        this.setData({ loading: false, loadError: true })
      })
      .then(() => { if (done) done() })
  },

  // 点击单条记录，进入作答详情
  gotoDetail(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({ url: `/pages/exam_detail/exam_detail?recordId=${id}` })
  },

  // 重试
  retry() {
    this.loadRecords()
  },
})
