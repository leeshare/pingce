// pages/mockexam_condition/mockexam_condition.js
const { isLoggedIn } = require('../../utils/auth')
const { listMockPapers, countQuestions } = require('../../utils/api/paper')

// 距下一个 3 月 15 日（陕西综评单招考试）的天数与年份
// 当前日期超过本年 3 月 15 日则按次年计算
function nextExamDate() {
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  const year = now.getFullYear()
  let target = new Date(year, 2, 15, 0, 0, 0)
  if (target.getTime() < now.getTime()) {
    target = new Date(year + 1, 2, 15, 0, 0, 0)
  }
  const diff = Math.ceil((target.getTime() - now.getTime()) / (24 * 60 * 60 * 1000))
  return { days: diff > 0 ? diff : 0, year: target.getFullYear() }
}

// 根据试卷标题/来源推断难度标签
function deriveDifficulty(paper) {
  const text = (paper.title || '') + (paper.source || '') + (paper.description || '')
  if (text.indexOf('基础') > -1) {
    return { difficultyLabel: '基础巩固', difficultyType: 'easy' }
  }
  if (text.indexOf('难') > -1) {
    return { difficultyLabel: '偏难', difficultyType: 'hard' }
  }
  return { difficultyLabel: '难度适中', difficultyType: 'medium' }
}

Page({
  data: {
    examYear: '2025',
    examDaysLeft: 0,
    examDuration: 90,
    examQuestionCount: 100,
    examTotalScore: 300,
    papers: [],
    loading: true,
    loadError: false,
  },

  onLoad() {
    const exam = nextExamDate()
    this.setData({
      examYear: exam.year,
      examDaysLeft: exam.days,
    })
    this.loadPapers()
  },

  // 拉取模考试卷列表
  loadPapers() {
    this.setData({ loading: true, loadError: false })
    listMockPapers({ page: 1, size: 50 })
      .then((res) => {
        const records = (res && res.records) || []
        const papers = records.map((p) => {
          const questionCount = countQuestions(p.questionIds)
          const diff = deriveDifficulty(p)
          return Object.assign({}, p, {
            questionCount: questionCount || 100,
            duration: p.duration || 90,
            totalScore: p.totalScore || 300,
            referenceCount: this.buildReferenceCount(p.id),
            difficultyLabel: diff.difficultyLabel,
            difficultyType: diff.difficultyType,
          })
        })

        // 用第一张试卷的信息更新模考说明（若有）
        const first = papers[0]
        const patch = { loading: false, papers }
        if (first) {
          patch.examDuration = first.duration
          patch.examQuestionCount = first.questionCount
          patch.examTotalScore = first.totalScore
        }
        this.setData(patch)
      })
      .catch((err) => {
        this.setData({ loading: false, loadError: true })
        wx.showToast({ title: err.message || '试卷加载失败', icon: 'none' })
      })
  },

  // 根据试卷ID生成稳定的"参考人数"展示值（数据表无此字段，用ID哈希模拟）
  buildReferenceCount(id) {
    if (!id) return 0
    const base = (id * 137) % 3000
    return base + 186
  },

  // 开始考试
  startExam(e) {
    const { id, title, duration } = e.currentTarget.dataset
    if (!id) return

    if (!isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '请先登录后再开始考试',
        confirmText: '去登录',
        success(res) {
          if (res.confirm) wx.navigateTo({ url: '/pages/login/login' })
        },
      })
      return
    }

    const t = title ? encodeURIComponent(title) : ''
    wx.navigateTo({
      url: `/pages/mockexam/mockexam?paperId=${id}&title=${t}&duration=${duration || 90}`,
    })
  },

  // 查看历史成绩
  goHistory() {
    if (!isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '请先登录后查看历史成绩',
        confirmText: '去登录',
        success(res) {
          if (res.confirm) wx.navigateTo({ url: '/pages/login/login' })
        },
      })
      return
    }
    wx.navigateTo({ url: '/pages/exam_history/exam_history' })
  },

  // 下拉重试（由页面配置或手势触发）
  onPullDownRefresh() {
    this.loadPapers()
    wx.stopPullDownRefresh()
  },
})
