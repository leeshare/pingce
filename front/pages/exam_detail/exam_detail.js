// pages/exam_detail/exam_detail.js
// 模考作答详情：从历史成绩列表进入，展示单次考试的每题作答、正确答案、解析。
const { getExamRecordDetail } = require('../../utils/api/examRecord')
const { adaptPaper } = require('../../utils/adapter/question')

Page({
  data: {
    recordId: null,
    loading: true,
    loadError: false,
    // 试卷元信息
    paperTitle: '',
    score: 0,
    totalScore: 0,
    duration: 0,
    submitTime: '',
    // 适配后的题目列表（含 correct/analysis/options）
    questions: [],
    // 用户作答，按 questionId 索引
    userAnswerMap: {},
    // 每题反馈结果：{ questionId: { isCorrect, noReference, userAnswerText, correctAnswerText, subResults } }
    feedbackMap: {},
    current: 0,
    // 当前题展示对象（由 questions[current] + feedbackMap 派生）
    question: {},
    feedback: {},
    // 预计算的展示值
    isPass: false,
    progressPercent: 0,
    emptyData: false,
    isComposite: false,
    isChoice: false,
    hasOptions: false,
  },

  onLoad(options) {
    const recordId = Number(options.recordId)
    if (!recordId) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      this.setData({ loading: false, loadError: true })
      return
    }
    this.setData({ recordId })
    this.loadDetail()
  },

  loadDetail() {
    this.setData({ loading: true, loadError: false })
    getExamRecordDetail(this.data.recordId)
      .then((res) => {
        if (!res) {
          this.setData({ loading: false, loadError: true })
          return
        }
        this.applyDetail(res)
      })
      .catch(() => {
        this.setData({ loading: false, loadError: true })
      })
  },

  applyDetail(res) {
    // 适配题目列表
    const questions = adaptPaper(res.questions || [])
    // 用户作答按 questionId 索引
    const userAnswerMap = {}
    ;(res.userAnswers || []).forEach((a) => {
      if (a && a.questionId) userAnswerMap[a.questionId] = a
    })
    // 逐题构建反馈
    const feedbackMap = {}
    questions.forEach((q) => {
      const ans = userAnswerMap[q.id] || null
      feedbackMap[q.id] = this.buildFeedback(q, ans)
    })
    this.setData({
      paperTitle: res.paperTitle || '模拟考试',
      score: res.score || 0,
      totalScore: res.totalScore || 0,
      duration: res.duration || 0,
      submitTime: res.submitTime || '',
      questions,
      userAnswerMap,
      feedbackMap,
      isPass: (res.totalScore || 0) > 0 && (res.score || 0) >= (res.totalScore || 0) * 0.6,
      emptyData: questions.length === 0,
      loading: false,
    })
    this.renderCurrent()
  },

  // 同步当前题到 data.question / data.feedback
  renderCurrent() {
    const idx = this.data.current
    const q = this.data.questions[idx]
    if (!q) return
    const isComposite = q.type === '复合'
    const isChoice = q.type === '单选' || q.type === '多选' || q.type === '判断'
    const hasOptions = q.options && q.options.length
    this.setData({
      question: q,
      feedback: this.data.feedbackMap[q.id] || {},
      progressPercent: this.data.questions.length ? ((idx + 1) * 100 / this.data.questions.length) : 0,
      isComposite,
      isChoice,
      hasOptions,
    })
  },

  // 构建单题反馈：用户答案文本、正确答案文本、对错、无参考答案标记
  buildFeedback(q, ans) {
    if (q.type === '复合') {
      const subFeedbacks = []
      let correctCount = 0
      const subAnsList = (ans && ans.subAnswers) || []
      ;(q.subQuestions || []).forEach((sub, idx) => {
        const subAns = subAnsList[idx] || null
        const fb = this.buildSimpleFeedback(sub, subAns)
        let status = '错误'
        let tagClass = 'tag-wrong'
        if (fb.noReference) {
          status = '无参考答案'
          tagClass = 'tag-neutral'
        } else if (fb.isCorrect) {
          status = '正确'
          tagClass = 'tag-correct'
          correctCount++
        }
        subFeedbacks.push({
          title: sub.title || '',
          analysis: sub.analysis || '',
          options: sub.options || [],
          hasOptions: !!(sub.options && sub.options.length),
          correct: sub.correct,
          status,
          tagClass,
          userAnswer: fb.userAnswerText,
          correctAnswer: fb.correctAnswerText,
        })
      })
      return {
        isComposite: true,
        correctCount,
        total: (q.subQuestions || []).length,
        subFeedbacks,
      }
    }
    return this.buildSimpleFeedback(q, ans)
  },

  // 构建非复合题反馈
  buildSimpleFeedback(q, ans) {
    const hasRef = this.hasCorrectAnswer(q)
    const userText = this.buildUserAnswerText(q, ans)
    const correctText = hasRef ? this.buildCorrectAnswerText(q) : ''
    // 判对错：只有有参考答案且用户有作答时才判
    let isCorrect = false
    if (hasRef && userText && userText !== '未作答') {
      isCorrect = this.judgeCorrect(q, ans, userText)
    }
    let statusText = ''
    let statusClass = ''
    let iconText = ''
    let iconClass = ''
    let textClass = ''
    if (!hasRef) {
      statusText = '无参考答案'
      statusClass = 'text-neutral'
      iconText = '?'
      iconClass = 'icon-neutral'
      textClass = 'text-neutral'
    } else if (isCorrect) {
      statusText = '回答正确'
      statusClass = 'text-correct'
      iconText = '✓'
      iconClass = 'icon-correct'
      textClass = 'text-correct'
    } else {
      statusText = '回答错误'
      statusClass = 'text-wrong'
      iconText = '✕'
      iconClass = 'icon-wrong'
      textClass = 'text-wrong'
    }
    return {
      isComposite: false,
      isCorrect,
      noReference: !hasRef,
      userAnswerText: userText,
      correctAnswerText: correctText,
      statusText,
      statusClass,
      iconText,
      iconClass,
      textClass,
    }
  },

  // 是否有正确答案
  hasCorrectAnswer(q) {
    if (!q) return false
    if (q.type === '复合') return false
    if (q.type === '单选' || q.type === '多选') {
      return q.correct !== undefined && q.correct !== null && q.correct !== ''
    }
    if (q.type === '判断') return q.correct !== undefined && q.correct !== null && q.correct !== ''
    if (q.type === '填空') {
      if (Array.isArray(q.correct)) return q.correct.length > 0
      return q.correct !== undefined && q.correct !== null && q.correct !== ''
    }
    if (q.type === '简答' || q.type === '计算') {
      return q.correct !== undefined && q.correct !== null && String(q.correct).trim() !== ''
    }
    return false
  },

  // 构建用户作答文本
  buildUserAnswerText(q, ans) {
    if (!ans) return '未作答'
    if (q.type === '单选' || q.type === '判断') {
      const selected = ans.selected || []
      if (!selected.length) return '未作答'
      const hasLetter = q.options && q.options[0] && q.options[0].letter
      return selected.map((i) => (hasLetter ? q.options[i].letter : q.options[i].text)).join('、')
    }
    if (q.type === '多选') {
      const selected = ans.selected || []
      if (!selected.length) return '未作答'
      const hasLetter = q.options && q.options[0] && q.options[0].letter
      return selected.map((i) => (hasLetter ? q.options[i].letter : q.options[i].text)).join('、')
    }
    if (q.type === '填空' || q.type === '简答' || q.type === '计算') {
      const text = (ans.text || '').trim()
      return text || '未作答'
    }
    return '未作答'
  },

  // 构建正确答案文本
  buildCorrectAnswerText(q) {
    if (q.type === '单选' || q.type === '判断') {
      return Array.isArray(q.correct) ? (q.correct[0] || '') : (q.correct || '')
    }
    if (q.type === '多选') {
      return Array.isArray(q.correct) ? q.correct.join('、') : (q.correct || '')
    }
    if (q.type === '填空') {
      return Array.isArray(q.correct) ? q.correct.join(' | ') : (q.correct || '')
    }
    if (q.type === '简答' || q.type === '计算') {
      return q.correct || ''
    }
    return ''
  },

  // 判分（与 practice.js/mockexam.js 宽松规则一致）
  judgeCorrect(q, ans, userText) {
    if (q.type === '单选' || q.type === '判断') {
      const correct = Array.isArray(q.correct) ? q.correct[0] : q.correct
      return userText === correct
    }
    if (q.type === '多选') {
      const userArr = userText.split('、').sort()
      const correctArr = (Array.isArray(q.correct) ? q.correct.slice() : [q.correct]).sort()
      return JSON.stringify(userArr) === JSON.stringify(correctArr)
    }
    if (q.type === '填空') {
      const userArr = (ans.text || '').split('|').map((s) => s.trim()).filter((s) => s !== '')
      const correctArr = Array.isArray(q.correct) ? q.correct.map((c) => String(c)) : [String(q.correct)]
      if (userArr.length !== correctArr.length) return false
      for (let i = 0; i < userArr.length; i++) {
        if (userArr[i].toLowerCase() !== correctArr[i].trim().toLowerCase()) return false
      }
      return true
    }
    if (q.type === '简答' || q.type === '计算') {
      const normUser = userText.toLowerCase().replace(/\s+/g, ' ')
      const normCorrect = String(q.correct).trim().toLowerCase().replace(/\s+/g, ' ')
      return normUser === normCorrect
    }
    return false
  },

  // 切题
  prevQuestion() {
    if (this.data.current > 0) {
      this.setData({ current: this.data.current - 1 })
      this.renderCurrent()
    }
  },
  nextQuestion() {
    if (this.data.current < this.data.questions.length - 1) {
      this.setData({ current: this.data.current + 1 })
      this.renderCurrent()
    }
  },
  gotoQuestion(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    if (!isNaN(idx) && idx >= 0 && idx < this.data.questions.length) {
      this.setData({ current: idx })
      this.renderCurrent()
    }
  },

  retry() {
    this.loadDetail()
  },
})
