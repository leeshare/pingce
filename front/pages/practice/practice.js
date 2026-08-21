// pages/practice/practice.js
const { listPractice } = require('../../utils/api/question')
const { adaptPaper } = require('../../utils/adapter/question')
const { report: reportWrong } = require('../../utils/api/wrong')

Page({
  data: {
    questions: [],
    current: 0,
    question: null,
    categoryLabel: '历年真题',
    // 加载状态
    loading: true,
    loadError: false,
    // 作答状态
    selected: [],        // 普通题选中的选项索引
    textAnswer: '',      // 简答/计算/填空题文本
    subAnswers: [],      // 复合题：[{ selected: [], text: '' }, ...]
    answered: false,
    // 反馈
    isCorrect: false,
    noReference: false,  // 题库未录入正确答案，不判对错
    userAnswerText: '',
    correctAnswerText: '',
    subResults: [],
    subNoReference: [],  // 复合题子题：是否无参考答案
    subUserAnswers: [],
    subCorrectAnswers: [],
    subCorrectCount: 0,
    // 各题作答快照（用于上一题恢复），按题号存
    // history[i] = { selected, textAnswer, subAnswers, answered, isCorrect, userAnswerText, correctAnswerText, subResults, subUserAnswers, subCorrectAnswers, subCorrectCount }
    history: [],
    // 动画
    animClass: 'anim-fade',
  },

  onLoad(opts) {
    // 从历年真题页传入 year + subject
    const year = opts.year || ''
    const subject = opts.subject || ''
    const label = year && subject
      ? `${year} 年真题 · ${subject}`
      : '历年真题'
    this.setData({ categoryLabel: label })
    this.loadQuestions({ year: Number(year), subject })
  },

  // ===== 拉取题目 =====
  loadQuestions(params) {
    this.setData({ loading: true, loadError: false })
    listPractice(params)
      .then((list) => {
        const questions = adaptPaper(list)
        if (!questions.length) {
          this.setData({ loading: false, questions: [] })
          wx.showModal({
            title: '提示',
            content: '该年份/科目暂无真题',
            showCancel: false,
            success() { wx.navigateBack() },
          })
          return
        }
        this.setData({ loading: false, questions })
        this.renderQuestion()
      })
      .catch((err) => {
        this.setData({ loading: false, loadError: true })
        wx.showToast({ title: err.message || '题目加载失败', icon: 'none' })
      })
  },

  // 重试
  retryLoad() {
    const { categoryLabel } = this.data
    // 从 label 反解 year + subject（"2026 年真题 · 语文"）
    const m = categoryLabel.match(/^(\d+)\s*年真题\s*·\s*(.+)$/)
    if (m) {
      this.loadQuestions({ year: Number(m[1]), subject: m[2] })
    }
  },

  // 渲染当前题目
  // 若 history[current] 有快照（回到上一题），恢复作答与反馈状态；否则重置为初始未作答
  renderQuestion() {
    const q = this.data.questions[this.data.current]
    if (!q) return

    const snapshot = this.data.history[this.data.current]
    let patch
    if (snapshot) {
      // 恢复快照
      patch = Object.assign({ question: q, animClass: '' }, snapshot)
    } else {
      // 初始化
      patch = {
        question: q,
        selected: [],
        textAnswer: '',
        answered: false,
        isCorrect: false,
        noReference: false,
        userAnswerText: '',
        correctAnswerText: '',
        subResults: [],
        subNoReference: [],
        subUserAnswers: [],
        subCorrectAnswers: [],
        subCorrectCount: 0,
        animClass: '',
      }
      if (q.type === '复合') {
        patch.subAnswers = q.subQuestions.map(() => ({ selected: [], text: '' }))
      } else {
        patch.subAnswers = []
      }
    }

    this.setData(patch)
    // 触发淡入动画
    setTimeout(() => this.setData({ animClass: 'anim-fade' }), 20)
  },

  // 保存当前题作答快照到 history[current]
  saveSnapshot() {
    const d = this.data
    this.data.history[d.current] = {
      selected: d.selected.slice(),
      textAnswer: d.textAnswer,
      subAnswers: d.subAnswers.map((s) => ({ selected: s.selected.slice(), text: s.text })),
      answered: d.answered,
      isCorrect: d.isCorrect,
      noReference: d.noReference,
      userAnswerText: d.userAnswerText,
      correctAnswerText: d.correctAnswerText,
      subResults: d.subResults.slice(),
      subNoReference: d.subNoReference.slice(),
      subUserAnswers: d.subUserAnswers.slice(),
      subCorrectAnswers: d.subCorrectAnswers.slice(),
      subCorrectCount: d.subCorrectCount,
    }
  },

  // ===== 普通题选项选择 =====
  selectOption(e) {
    if (this.data.answered) return
    const idx = Number(e.currentTarget.dataset.idx)
    const q = this.data.question

    if (q.type === '多选') {
      const selected = this.data.selected.slice()
      const pos = selected.indexOf(idx)
      if (pos === -1) selected.push(idx)
      else selected.splice(pos, 1)
      this.setData({ selected })
    } else {
      // 单选/判断
      this.setData({ selected: [idx] })
    }
  },

  // ===== 文本输入 =====
  onTextInput(e) {
    this.setData({ textAnswer: e.detail.value })
  },

  // ===== 复合题子题选项选择 =====
  selectSubOption(e) {
    if (this.data.answered) return
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

  // ===== 复合题子题文本输入 =====
  onSubTextInput(e) {
    const sIdx = Number(e.currentTarget.dataset.idx)
    const subAnswers = this.data.subAnswers.slice()
    subAnswers[sIdx] = Object.assign({}, subAnswers[sIdx], { text: e.detail.value })
    this.setData({ subAnswers })
  },

  // ===== 提交答案 =====
  submitAnswer() {
    if (this.data.answered) return
    const q = this.data.question

    if (q.type === '复合') {
      // 校验每个子题
      for (let i = 0; i < q.subQuestions.length; i++) {
        const sub = q.subQuestions[i]
        const ans = this.data.subAnswers[i]
        if (sub.type === '简答' || sub.type === '填空') {
          if (!ans.text || !ans.text.trim()) {
            wx.showToast({ title: `请完成第 ${i + 1} 小题的作答`, icon: 'none' })
            return
          }
        } else if (!ans.selected || ans.selected.length === 0) {
          wx.showToast({ title: `请选择第 ${i + 1} 小题的答案`, icon: 'none' })
          return
        }
      }
      this.gradeComposite(q)
    } else if (q.type === '简答' || q.type === '计算') {
      if (!this.data.textAnswer.trim()) {
        wx.showToast({ title: '请输入答案后再提交', icon: 'none' })
        return
      }
      this.gradeText(q)
    } else if (q.type === '填空') {
      if (!this.data.textAnswer.trim()) {
        wx.showToast({ title: '请输入答案后再提交', icon: 'none' })
        return
      }
      this.gradeFill(q)
    } else {
      if (this.data.selected.length === 0) {
        wx.showToast({ title: '请选择答案后再提交', icon: 'none' })
        return
      }
      this.gradeChoice(q)
    }

    this.setData({ answered: true })
    // 保存作答快照，便于上一题恢复
    this.saveSnapshot()
  },

  // 判分：单选/多选/判断
  // letter 为空（判断题 options 为 ["正确","错误"]）时回退用 text 比对
  gradeChoice(q) {
    const selected = this.data.selected
    const hasLetter = q.options[0] && q.options[0].letter
    const userKeys = selected.map((i) => hasLetter ? q.options[i].letter : q.options[i].text)
    const correct = q.correct
    const hasRef = this.hasCorrectAnswer(q)

    // 无参考答案：不判对错
    if (!hasRef) {
      this.setData({
        isCorrect: false,
        noReference: true,
        userAnswerText: userKeys.join('、'),
        correctAnswerText: '',
      })
      return
    }

    let isCorrect
    if (q.type === '多选') {
      const sortedUser = userKeys.slice().sort()
      const sortedCorrect = correct.slice().sort()
      isCorrect = JSON.stringify(sortedUser) === JSON.stringify(sortedCorrect)
    } else {
      // 单选/判断
      isCorrect = userKeys[0] === correct
    }

    this.setData({
      isCorrect,
      noReference: false,
      userAnswerText: userKeys.join('、'),
      correctAnswerText: Array.isArray(correct) ? correct.join('、') : correct,
    })

    // 错题上报：题库录入了正确答案且答错时上报；无正确答案不调用错题api
    if (!isCorrect && q.id) {
      this.reportWrongQuestion(q.id)
    }
  },

  // 判分：简答/计算（主观题）
  // 有参考答案时：忽略大小写、空白后完全相等算对；答错时上报错题
  // 无参考答案时：显示"无参考答案"，不判对错
  gradeText(q) {
    const hasRef = this.hasCorrectAnswer(q)
    const userText = this.data.textAnswer.trim()
    let isCorrect = false
    if (hasRef && userText) {
      const normUser = userText.toLowerCase().replace(/\s+/g, ' ')
      const normCorrect = String(q.correct).trim().toLowerCase().replace(/\s+/g, ' ')
      isCorrect = normUser === normCorrect
    }
    this.setData({
      isCorrect,
      noReference: !hasRef,
      userAnswerText: userText,
      correctAnswerText: hasRef ? q.correct : '',
    })

    // 错题上报：题库录入了正确答案且答错时上报；无正确答案不调用错题api
    if (!isCorrect && hasRef && q.id) {
      this.reportWrongQuestion(q.id)
    }
  },

  // 判分：填空题
  // 用户输入按 | 拆分，与正确答案数组逐项比对（忽略大小写、去首尾空格）
  // 注：题库可能未录入正确答案（correct 为 undefined），此时不判对错，只展示用户作答
  gradeFill(q) {
    const userText = this.data.textAnswer.trim()
    const userArr = userText.split('|').map((s) => s.trim()).filter((s) => s !== '')
    const correctArr = Array.isArray(q.correct)
      ? q.correct.map((c) => String(c))
      : (q.correct != null ? [String(q.correct)] : [])

    let isCorrect = false
    let noReference = false
    if (correctArr.length === 0) {
      // 题库未录入正确答案，不判对错
      noReference = true
    } else if (userArr.length === correctArr.length) {
      isCorrect = true
      for (let i = 0; i < userArr.length; i++) {
        if (userArr[i].toLowerCase() !== correctArr[i].trim().toLowerCase()) {
          isCorrect = false
          break
        }
      }
    }

    this.setData({
      isCorrect,
      noReference,
      userAnswerText: userArr.join(' | '),
      correctAnswerText: correctArr.length ? correctArr.join(' | ') : '',
    })

    // 错题上报：题库录入了正确答案且答错时上报；无正确答案不调用错题api
    if (!isCorrect && !noReference && q.id) {
      this.reportWrongQuestion(q.id)
    }
  },

  // 判分：复合题
  // 子题单选/判断/多选同样需要兼容 letter 为空（判断题）的情况
  // 无正确答案的子题不判对错（subNoReference=true），不计入 correctCount
  gradeComposite(q) {
    const subResults = []
    const subNoReference = []
    const subUserAnswers = []
    const subCorrectAnswers = []
    let correctCount = 0
    const wrongSubIds = []  // 待上报的子题ID

    q.subQuestions.forEach((sub, idx) => {
      const ans = this.data.subAnswers[idx]
      const hasRef = this.hasCorrectAnswer(sub)
      let userText, correctText, isCorrect, noRef

      if (sub.type === '简答' || sub.type === '填空') {
        // 主观题不判对错
        userText = ans.text.trim()
        correctText = hasRef ? sub.correct : ''
        isCorrect = false
        noRef = true
      } else if (!hasRef) {
        // 客观题无正确答案，不判对错
        noRef = true
        isCorrect = false
        correctText = ''
        if (sub.type === '多选') {
          userText = ans.selected.map((i) => sub.options[i].text).join('、')
        } else {
          const hasLetter = sub.options[0] && sub.options[0].letter
          userText = ans.selected[0] != null
            ? (hasLetter ? sub.options[ans.selected[0]].letter : sub.options[ans.selected[0]].text)
            : ''
        }
      } else if (sub.type === '多选') {
        noRef = false
        const hasLetter = sub.options[0] && sub.options[0].letter
        const userKeys = ans.selected.map((i) => hasLetter ? sub.options[i].letter : sub.options[i].text).sort()
        const sortedCorrect = sub.correct.slice().sort()
        isCorrect = JSON.stringify(userKeys) === JSON.stringify(sortedCorrect)
        userText = userKeys.join('、')
        correctText = sub.correct.join('、')
      } else {
        // 单选/判断
        noRef = false
        const hasLetter = sub.options[0] && sub.options[0].letter
        const userKey = hasLetter ? sub.options[ans.selected[0]].letter : sub.options[ans.selected[0]].text
        isCorrect = userKey === sub.correct
        userText = userKey
        correctText = sub.correct
      }

      if (isCorrect) correctCount++
      // 子题错题上报：有正确答案且答错时上报；无正确答案不调用错题api
      if (!isCorrect && !noRef && sub.id) {
        wrongSubIds.push(sub.id)
      }
      subResults.push(isCorrect)
      subNoReference.push(noRef)
      subUserAnswers.push(userText)
      subCorrectAnswers.push(correctText)
    })

    this.setData({
      subResults,
      subNoReference,
      subUserAnswers,
      subCorrectAnswers,
      subCorrectCount: correctCount,
    })

    // 批量上报错子题
    wrongSubIds.forEach((sid) => this.reportWrongQuestion(sid))
  },

  // 判断题目是否录入了正确答案
  // 统一规则：无正确答案（null/undefined/空字符串/空数组）的题目不调用错题api
  // 覆盖：单选/多选/判断（字符串或数组）、填空（数组）、简答/计算（参考答案字符串）
  hasCorrectAnswer(q) {
    if (!q) return false
    const c = q.correct
    if (c == null) return false
    if (typeof c === 'string') return c.trim() !== ''
    if (Array.isArray(c)) return c.length > 0
    return true
  },

  // 错题上报（静默，失败不影响做题）
  reportWrongQuestion(questionId) {
    if (!questionId) return
    reportWrong(questionId).catch(() => {
      // 静默失败：错题上报不应阻塞做题流程
    })
  },

  // ===== 上一题 =====
  // 已是第 1 题则忽略；切题前保存当前作答快照（含未提交的选项/文本），renderQuestion 会按快照恢复
  prevQuestion() {
    if (this.data.current === 0) return
    this.saveSnapshot()
    this.setData({ current: this.data.current - 1 })
    this.renderQuestion()
  },

  // ===== 下一题 =====
  nextQuestion() {
    if (this.data.current + 1 >= this.data.questions.length) {
      // 完成练习
      wx.showModal({
        title: '练习完成',
        content: '本轮真题练习已完成，是否返回？',
        showCancel: false,
        success() {
          wx.navigateBack()
        },
      })
      return
    }
    this.saveSnapshot()
    this.setData({ current: this.data.current + 1 })
    this.renderQuestion()
  },
})
