// pages/mockexam/mockexam.js
// 全真模考：整卷一次性提交，提交前可随意修改任意题作答；提交后展示成绩与逐题反馈。
const { listPaperPractice, grade: gradePaper } = require('../../utils/api/question')
const { adaptPaper } = require('../../utils/adapter/question')

Page({
  data: {
    paperId: null,
    paperTitle: '',
    questions: [],
    current: 0,
    question: null,
    // 整卷作答（按题号存）：
    // answers[i] = { selected: [], text: '', subAnswers: [{ selected: [], text: '' }, ...] }
    answers: [],
    // 提交后整体判分结果
    submitted: false,
    // 后端判分结果（按题号顺序），每项含 isCorrect/noReference/userAnswerText/correctAnswerText/subResults
    gradeResults: [],
    // 当前题反馈（仅 submitted 后展示）
    selected: [],
    textAnswer: '',
    subAnswers: [],
    isCorrect: false,
    noReference: false,
    userAnswerText: '',
    correctAnswerText: '',
    subResults: [],
    subNoReference: [],
    subUserAnswers: [],
    subCorrectAnswers: [],
    subCorrectCount: 0,
    // 成绩统计
    totalCount: 0,
    answeredCount: 0,
    correctCount: 0,
    wrongCount: 0,
    noRefCount: 0,
    totalScore: 0,
    gotScore: 0,
    passScore: 0,
    passed: false,
    // 加载状态
    loading: true,
    loadError: false,
    // 倒计时
    durationSec: 0,
    remainSec: 0,
    timeUp: false,
    timerText: '00:00',
    // 动画
    animClass: 'anim-fade',
  },

  _timer: null,

  onLoad(opts) {
    const paperId = opts.paperId || ''
    const title = opts.title || '全真模考'
    const duration = Number(opts.duration) || 90
    this.setData({
      paperId,
      paperTitle: decodeURIComponent(title),
      durationSec: duration * 60,
      remainSec: duration * 60,
      timerText: this.formatTime(duration * 60),
    })
    if (!paperId) {
      this.setData({ loading: false, loadError: true })
      wx.showToast({ title: '缺少试卷参数', icon: 'none' })
      return
    }
    this.loadQuestions(paperId)
  },

  onUnload() {
    this.clearTimer()
  },

  // ===== 拉取题目 =====
  loadQuestions(paperId) {
    this.setData({ loading: true, loadError: false })
    listPaperPractice(paperId)
      .then((list) => {
        const questions = adaptPaper(list)
        if (!questions.length) {
          this.setData({ loading: false, questions: [] })
          wx.showModal({
            title: '提示',
            content: '该试卷暂无题目',
            showCancel: false,
            success() { wx.navigateBack() },
          })
          return
        }
        // 初始化作答容器
        const answers = questions.map((q) => this.initAnswer(q))
        this.setData({
          loading: false,
          questions,
          answers,
          totalCount: questions.length,
          totalScore: questions.reduce((s, q) => s + (q.score || 0), 0),
        })
        this.renderQuestion()
        this.startTimer()
        // 启用返回拦截：离开页面将丢失已作答内容
        this.enableBackAlert()
      })
      .catch((err) => {
        this.setData({ loading: false, loadError: true })
        wx.showToast({ title: err.message || '题目加载失败', icon: 'none' })
      })
  },

  retryLoad() {
    if (this.data.paperId) this.loadQuestions(this.data.paperId)
  },

  // 初始化一道题的作答容器
  initAnswer(q) {
    if (q.type === '复合') {
      return {
        selected: [],
        text: '',
        subAnswers: q.subQuestions.map(() => ({ selected: [], text: '' })),
      }
    }
    return { selected: [], text: '', subAnswers: [] }
  },

  // 渲染当前题：从 answers[current] 恢复作答；若已提交，展示该题反馈
  renderQuestion() {
    const q = this.data.questions[this.data.current]
    if (!q) return
    const ans = this.data.answers[this.data.current] || this.initAnswer(q)

    let patch = {
      question: q,
      selected: ans.selected.slice(),
      textAnswer: ans.text,
      subAnswers: ans.subAnswers.map((s) => ({ selected: s.selected.slice(), text: s.text })),
      animClass: '',
    }

    // 已提交：从后端判分结果取反馈
    if (this.data.submitted) {
      Object.assign(patch, this.buildFeedback(q, this.data.current))
    } else {
      Object.assign(patch, {
        isCorrect: false,
        noReference: false,
        userAnswerText: '',
        correctAnswerText: '',
        subResults: [],
        subNoReference: [],
        subUserAnswers: [],
        subCorrectAnswers: [],
        subCorrectCount: 0,
      })
    }

    this.setData(patch)
    setTimeout(() => this.setData({ animClass: 'anim-fade' }), 20)
  },

  // 同步当前题作答到 answers[current]（切题/提交前调用）
  syncCurrentAnswer() {
    const d = this.data
    if (!d.question) return
    const answers = d.answers.slice()
    answers[d.current] = {
      selected: d.selected.slice(),
      text: d.textAnswer,
      subAnswers: d.subAnswers.map((s) => ({ selected: s.selected.slice(), text: s.text })),
    }
    this.setData({ answers })
  },

  // ===== 普通题选项选择 =====
  selectOption(e) {
    if (this.data.submitted) return
    const idx = Number(e.currentTarget.dataset.idx)
    const q = this.data.question
    if (q.type === '多选') {
      const selected = this.data.selected.slice()
      const pos = selected.indexOf(idx)
      if (pos === -1) selected.push(idx)
      else selected.splice(pos, 1)
      this.setData({ selected })
    } else {
      this.setData({ selected: [idx] })
    }
  },

  onTextInput(e) {
    if (this.data.submitted) return
    this.setData({ textAnswer: e.detail.value })
  },

  // ===== 复合题子题 =====
  selectSubOption(e) {
    if (this.data.submitted) return
    const sIdx = Number(e.currentTarget.dataset.sidx)
    const oIdx = Number(e.currentTarget.dataset.oidx)
    const sub = this.data.question.subQuestions[sIdx]
    const subAnswers = this.data.subAnswers.slice()
    const cur = Object.assign({}, subAnswers[sIdx])
    if (sub.type === '多选') {
      const pos = cur.selected.indexOf(oIdx)
      if (pos === -1) cur.selected = cur.selected.concat([oIdx])
      else cur.selected = cur.selected.filter((i) => i !== oIdx)
    } else {
      cur.selected = [oIdx]
    }
    subAnswers[sIdx] = cur
    this.setData({ subAnswers })
  },

  onSubTextInput(e) {
    if (this.data.submitted) return
    const sIdx = Number(e.currentTarget.dataset.idx)
    const subAnswers = this.data.subAnswers.slice()
    subAnswers[sIdx] = Object.assign({}, subAnswers[sIdx], { text: e.detail.value })
    this.setData({ subAnswers })
  },

  // ===== 切题 =====
  prevQuestion() {
    if (this.data.current === 0) return
    this.syncCurrentAnswer()
    this.setData({ current: this.data.current - 1 })
    this.renderQuestion()
  },

  nextQuestion() {
    this.syncCurrentAnswer()
    if (this.data.current + 1 >= this.data.questions.length) return
    this.setData({ current: this.data.current + 1 })
    this.renderQuestion()
  },

  // 跳转到答题卡指定题
  gotoQuestion(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    this.syncCurrentAnswer()
    this.setData({ current: idx })
    this.renderQuestion()
  },

  // ===== 提交整卷 =====
  submitPaper() {
    if (this.data.submitted) return
    this.syncCurrentAnswer()

    const questions = this.data.questions
    const answers = this.data.answers
    const total = questions.length

    // 构建后端判分请求体
    const gradeAnswers = questions.map((q, i) => {
      const ans = answers[i] || { selected: [], text: '', subAnswers: [] }
      const item = {
        questionId: q.id,
        type: q.typeRaw,
        selected: ans.selected || [],
        text: ans.text || '',
      }
      if (q.type === '复合' && q.subQuestions) {
        item.subAnswers = q.subQuestions.map((sub, si) => {
          const sa = (ans.subAnswers && ans.subAnswers[si]) || { selected: [], text: '' }
          return {
            questionId: sub.id,
            type: sub.typeRaw,
            selected: sa.selected || [],
            text: sa.text || '',
          }
        })
      }
      return item
    })

    wx.showLoading({ title: '提交中...', mask: true })
    // 用时 = 考试时长 - 剩余时长
    const durationSec = Math.max(0, this.data.durationSec - this.data.remainSec)
    gradePaper({ paperId: this.data.paperId, durationSec, answers: gradeAnswers })
      .then((res) => {
        wx.hideLoading()
        this.applyGradeResult(res, total)
      })
      .catch((err) => {
        wx.hideLoading()
        console.error('[mockexam] 后端判分失败，降级前端判分', err)
        this.fallbackGrade(total)
      })
  },

  // 应用后端判分结果
  applyGradeResult(res, total) {
    const results = (res && res.results) || []
    const summary = (res && res.summary) || {}
    const correct = summary.correct || 0
    const wrong = summary.wrong || 0
    const noRef = summary.noRef || 0
    const gotScore = summary.gotScore || 0
    const answered = summary.answered || 0
    const passScore = this.data.passScore
    const passed = passScore > 0 ? gotScore >= passScore : false

    this.setData({
      submitted: true,
      gradeResults: results,
      answeredCount: answered,
      correctCount: correct,
      wrongCount: wrong,
      noRefCount: noRef,
      gotScore,
      passed,
    })
    this.clearTimer()
    this.disableBackAlert()

    const delay = this._fromConfirmModal ? 300 : 0
    this._fromConfirmModal = false
    setTimeout(() => {
      wx.showModal({
        title: passed ? '考试通过' : '考试完成',
        content: `共 ${total} 题，答对 ${correct} 题，答错 ${wrong} 题，无参考答案 ${noRef} 题，得分 ${gotScore} 分`,
        showCancel: false,
        confirmText: '查看结果',
        success: () => {
          this.setData({ current: 0 })
          this.renderQuestion()
        },
      })
    }, delay)
  },

  // 后端判分失败时的降级方案（前端本地判分，不记录错题）
  fallbackGrade(total) {
    const questions = this.data.questions
    const answers = this.data.answers
    let answered = 0
    let correct = 0
    let wrong = 0
    let noRef = 0
    let gotScore = 0
    const gradeResults = []

    try {
      questions.forEach((q, i) => {
        const ans = answers[i] || { selected: [], text: '', subAnswers: [] }
        if (this.isAnswered(q, ans)) answered++
        const result = this.gradeOne(q, ans) || {}
        gradeResults.push({
          questionId: q.id,
          isCorrect: result.isCorrect,
          noReference: result.noReference,
          userAnswerText: result.userAnswerText,
          correctAnswerText: result.correctAnswerText,
          subResults: [],
        })
        if (result.noReference) {
          noRef++
        } else if (result.isCorrect) {
          correct++
          gotScore += (q.score || 0)
        } else {
          wrong++
        }
      })
    } catch (err) {
      console.error('[mockexam] 降级判分也失败了', err)
    }

    const passScore = this.data.passScore
    const passed = passScore > 0 ? gotScore >= passScore : false

    this.setData({
      submitted: true,
      gradeResults,
      answeredCount: answered,
      correctCount: correct,
      wrongCount: wrong,
      noRefCount: noRef,
      gotScore,
      passed,
    })
    this.clearTimer()
    this.disableBackAlert()

    const delay = this._fromConfirmModal ? 300 : 0
    this._fromConfirmModal = false
    setTimeout(() => {
      wx.showModal({
        title: passed ? '考试通过' : '考试完成',
        content: `共 ${total} 题，答对 ${correct} 题，答错 ${wrong} 题，无参考答案 ${noRef} 题，得分 ${gotScore} 分`,
        showCancel: false,
        confirmText: '查看结果',
        success: () => {
          this.setData({ current: 0 })
          this.renderQuestion()
        },
      })
    }, delay)
  },

  // 从后端判分结果构建当前题的展示反馈
  buildFeedback(q, idx) {
    const result = this.data.gradeResults[idx]
    if (!result) {
      return {
        isCorrect: false,
        noReference: false,
        userAnswerText: '',
        correctAnswerText: '',
        subResults: [],
        subNoReference: [],
        subUserAnswers: [],
        subCorrectAnswers: [],
        subCorrectCount: 0,
      }
    }

    // 复合题：从 subResults 衍生前端需要的数组
    if (q.type === '复合' && result.subResults) {
      const subResults = []
      const subNoReference = []
      const subUserAnswers = []
      const subCorrectAnswers = []
      let correctCount = 0
      result.subResults.forEach((sr) => {
        const ic = sr.isCorrect || false
        if (ic) correctCount++
        subResults.push(ic)
        subNoReference.push(sr.noReference || false)
        subUserAnswers.push(sr.userAnswerText || '')
        subCorrectAnswers.push(sr.correctAnswerText || '')
      })
      return {
        isCorrect: correctCount === q.subQuestions.length,
        noReference: false,
        userAnswerText: '',
        correctAnswerText: '',
        subResults,
        subNoReference,
        subUserAnswers,
        subCorrectAnswers,
        subCorrectCount: correctCount,
      }
    }

    return {
      isCorrect: result.isCorrect || false,
      noReference: result.noReference || false,
      userAnswerText: result.userAnswerText || '',
      correctAnswerText: result.correctAnswerText || '',
      subResults: [],
      subNoReference: [],
      subUserAnswers: [],
      subCorrectAnswers: [],
      subCorrectCount: 0,
    }
  },

  // 未提交时二次确认（最后一题点提交）
  confirmSubmit() {
    if (this.data.submitted) return
    this.syncCurrentAnswer()

    let unanswered = 0
    let firstIdx = -1
    this.data.questions.forEach((q, i) => {
      if (!this.isAnswered(q, this.data.answers[i])) {
        unanswered++
        if (firstIdx === -1) firstIdx = i
      }
    })

    if (unanswered > 0) {
      // 有未作答：取消按钮改为「去作答」，点击跳转到第一道未作答题
      wx.showModal({
        title: '仍有未作答',
        content: `还有 ${unanswered} 题未作答，是否继续提交？`,
        confirmText: '继续提交',
        cancelText: '去作答',
        success: (res) => {
          if (res.confirm) {
            this._fromConfirmModal = true
            this.submitPaper()
          } else if (res.cancel) {
            this.setData({ current: firstIdx })
            this.renderQuestion()
          }
        },
      })
    } else {
      wx.showModal({
        title: '提交确认',
        content: '已全部作答，确认提交试卷吗？提交后不可修改。',
        confirmText: '提交',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            this._fromConfirmModal = true
            this.submitPaper()
          }
        },
      })
    }
  },

  // 判断一道题是否已作答
  isAnswered(q, ans) {
    if (!ans) return false
    if (q.type === '复合') {
      return q.subQuestions.every((sub, i) => {
        const sa = ans.subAnswers[i]
        if (!sa) return false
        if (sub.type === '简答' || sub.type === '填空') {
          return sa.text && sa.text.trim() !== ''
        }
        return sa.selected && sa.selected.length > 0
      })
    }
    if (q.type === '简答' || q.type === '计算' || q.type === '填空') {
      return ans.text && ans.text.trim() !== ''
    }
    return ans.selected && ans.selected.length > 0
  },

  // 判分单道题，返回反馈字段（与 practice.js 判分逻辑保持一致）
  gradeOne(q, ans) {
    if (!q || !ans) {
      return { isCorrect: false, noReference: false, userAnswerText: '', correctAnswerText: '', subResults: [], subNoReference: [], subUserAnswers: [], subCorrectAnswers: [], subCorrectCount: 0 }
    }

    if (q.type === '复合') {
      const subResults = []
      const subNoReference = []
      const subUserAnswers = []
      const subCorrectAnswers = []
      let correctCount = 0
      q.subQuestions.forEach((sub, idx) => {
        const sa = ans.subAnswers[idx] || { selected: [], text: '' }
        const hasRef = this.hasCorrectAnswer(sub)
        let userText, correctText, isCorrect, noRef
        if (sub.type === '简答' || sub.type === '填空') {
          userText = (sa.text || '').trim()
          correctText = hasRef ? sub.correct : ''
          isCorrect = false
          noRef = true
        } else if (!hasRef) {
          noRef = true
          isCorrect = false
          correctText = ''
          // 用 safeOptionKeys 兜底越界索引
          userText = this.safeOptionKeys(sub, sa.selected).join('、')
        } else if (sub.type === '多选') {
          noRef = false
          const userKeys = this.safeOptionKeys(sub, sa.selected).slice().sort()
          const sortedCorrect = sub.correct.slice().sort()
          isCorrect = JSON.stringify(userKeys) === JSON.stringify(sortedCorrect)
          userText = userKeys.join('、')
          correctText = sub.correct.join('、')
        } else {
          noRef = false
          const userKeys = this.safeOptionKeys(sub, sa.selected)
          const userKey = userKeys[0]
          isCorrect = userKey === sub.correct
          userText = userKey
          correctText = sub.correct
        }
        if (isCorrect) correctCount++
        subResults.push(isCorrect)
        subNoReference.push(noRef)
        subUserAnswers.push(userText)
        subCorrectAnswers.push(correctText)
      })
      return { isCorrect: correctCount === q.subQuestions.length, noReference: false, subResults, subNoReference, subUserAnswers, subCorrectAnswers, subCorrectCount: correctCount }
    }

    if (q.type === '简答' || q.type === '计算') {
      const hasRef = this.hasCorrectAnswer(q)
      const userText = (ans.text || '').trim()
      // 简答/计算为主观题，无法精确判分；
      // 仅当用户作答与参考答案完全一致（忽略大小写、首尾空格、内部多空格）时算正确，
      // 否则一律算"答错"，由用户对照参考答案自评
      let isCorrect = false
      if (hasRef && userText) {
        const normUser = userText.toLowerCase().replace(/\s+/g, ' ')
        const normCorrect = String(q.correct).trim().toLowerCase().replace(/\s+/g, ' ')
        isCorrect = normUser === normCorrect
      }
      return {
        isCorrect,
        noReference: !hasRef,
        userAnswerText: userText,
        correctAnswerText: hasRef ? q.correct : '',
      }
    }

    if (q.type === '填空') {
      const userText = (ans.text || '').trim()
      const userArr = userText.split('|').map((s) => s.trim()).filter((s) => s !== '')
      const correctArr = Array.isArray(q.correct) ? q.correct.map((c) => String(c)) : (q.correct != null ? [String(q.correct)] : [])
      let isCorrect = false
      let noReference = false
      if (correctArr.length === 0) {
        noReference = true
      } else if (userArr.length === correctArr.length) {
        isCorrect = true
        for (let i = 0; i < userArr.length; i++) {
          if (userArr[i].toLowerCase() !== correctArr[i].trim().toLowerCase()) { isCorrect = false; break }
        }
      }
      return {
        isCorrect,
        noReference,
        userAnswerText: userArr.join(' | '),
        correctAnswerText: correctArr.length ? correctArr.join(' | ') : '',
      }
    }

    // 单选/多选/判断
    const hasRef = this.hasCorrectAnswer(q)
    const userKeys = this.safeOptionKeys(q, ans.selected)
    if (!hasRef) {
      return { isCorrect: false, noReference: true, userAnswerText: userKeys.join('、'), correctAnswerText: '' }
    }
    let isCorrect
    if (q.type === '多选') {
      const sortedUser = userKeys.slice().sort()
      const sortedCorrect = q.correct.slice().sort()
      isCorrect = JSON.stringify(sortedUser) === JSON.stringify(sortedCorrect)
    } else {
      isCorrect = userKeys[0] === q.correct
    }
    return {
      isCorrect,
      noReference: false,
      userAnswerText: userKeys.join('、'),
      correctAnswerText: Array.isArray(q.correct) ? q.correct.join('、') : q.correct,
    }
  },

  // 安全取选项键：selected 里的索引越界时跳过，避免 options[i] 为 undefined 报错
  safeOptionKeys(q, selected) {
    if (!q || !q.options || !selected) return []
    const hasLetter = q.options[0] && q.options[0].letter
    const keys = []
    for (let i = 0; i < selected.length; i++) {
      const opt = q.options[selected[i]]
      if (!opt) continue
      keys.push(hasLetter ? opt.letter : opt.text)
    }
    return keys
  },

  hasCorrectAnswer(q) {
    if (!q) return false
    const c = q.correct
    if (c == null) return false
    if (typeof c === 'string') return c.trim() !== ''
    if (Array.isArray(c)) return c.length > 0
    return true
  },

  reportWrongQuestion(questionId) {
    if (!questionId) return
    reportWrong(questionId).catch(() => {})
  },

  // ===== 返回拦截 =====
  enableBackAlert() {
    if (wx.enableAlertBeforeUnload) {
      wx.enableAlertBeforeUnload({
        message: '返回将丢失已作答内容，确定离开吗？',
      })
    }
  },

  disableBackAlert() {
    if (wx.disableAlertBeforeUnload) {
      wx.disableAlertBeforeUnload()
    }
  },

  // ===== 倒计时 =====
  startTimer() {
    this.clearTimer()
    // this.data.remainSec = 60 测试自动提交
    this._timer = setInterval(() => {
      let remain = this.data.remainSec - 1
      if (remain <= 0) {
        remain = 0
        this.setData({ remainSec: 0, timerText: '00:00', timeUp: true })
        this.clearTimer()
        if (!this.data.submitted) {
          wx.showModal({
            title: '考试时间到',
            content: '已自动提交试卷',
            showCancel: false,
            success: () => this.submitPaper(),
          })
        }
        return
      }
      this.setData({ remainSec: remain, timerText: this.formatTime(remain) })
    }, 1000)
  },

  clearTimer() {
    if (this._timer) {
      clearInterval(this._timer)
      this._timer = null
    }
  },

  formatTime(sec) {
    const m = Math.floor(sec / 60)
    const s = sec % 60
    return `${m < 10 ? '0' + m : m}:${s < 10 ? '0' + s : s}`
  },

  // 完成返回
  finishExam() {
    wx.navigateBack()
  },
})
