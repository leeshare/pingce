// utils/adapter/question.js - 后端 PracticeQuestionVO → 前端渲染结构适配
//
// 适配目的：把后端 PracticeController 返回的 VO 转成 practice.js 当前 mock 的数据结构，
// 让 practice.wxml / practice.js 无需改动即可直接渲染真实数据。
//
// 后端 VO 字段 → 前端字段对照：
//   type(1-7)            → type('单选'/'多选'/'判断'/'填空'/'简答'/'计算'/'复合')
//   typeLabel('单选题')   → typeTag
//   score(BigDecimal)    → score(number)
//   content              → title(普通题) / material(复合题)
//   options[{letter,text}] → options[{letter,text}]（单选/多选强制补字母 A/B/C/D）
//   answer               → correct（单选/多选归一化为字母；多选 'ABD' → ['A','B','D']；填空保持数组）
//   analysis             → analysis
//   (无)                 → knowledge([])（后端暂无此字段，给空数组兜底防渲染异常）
//   subQuestions         → subQuestions（递归适配）

/** 题型数字 → 中文名（与 practice.js mock 保持一致） */
const TYPE_MAP = {
  1: '单选',
  2: '多选',
  3: '判断',
  4: '填空',
  5: '简答',
  6: '计算',
  7: '复合',
}

/**
 * 构建选项列表（单选/多选强制补字母 A/B/C/D，判断题不补）
 * @param {Array} rawOptions 后端 options
 * @param {string} type 中文题型
 * @returns {Array<{letter:string,text:string}>}
 */
function buildOptions(rawOptions, type) {
  if (!rawOptions || !rawOptions.length) return undefined
  return rawOptions.map((opt, idx) => {
    const text = (opt && opt.text) || ''
    let letter = (opt && opt.letter) || ''
    if (type === '单选' || type === '多选') {
      letter = String.fromCharCode(65 + idx)
    }
    return { letter, text }
  })
}

/**
 * 单选/多选答案归一化为字母
 *  - answer 可能是字母 "A" / "ABD"，也可能是选项文本 "李白" / ["李白","杜甫"]
 *  - 单选返回字母字符串，多选返回字母数组（已排序）
 *  - 找不到匹配项时原样返回，避免判分逻辑误判
 *
 * @param {string|string[]|undefined} answer 后端 answer
 * @param {Array<{letter:string,text:string}>} options 已补字母的选项列表
 * @param {string} type '单选' / '多选'
 */
function normalizeChoiceAnswer(answer, options, type) {
  if (answer == null || answer === '') return undefined

  // text → letter 映射，用于把文本答案反查为字母
  const textToLetter = {}
  if (options) {
    options.forEach((opt) => {
      if (opt.text) textToLetter[opt.text] = opt.letter
    })
  }

  const toLetter = (key) => {
    if (key == null) return ''
    const s = String(key).trim()
    if (!s) return ''
    // 单字母 A-Z 直接返回大写
    if (/^[A-Za-z]$/.test(s)) return s.toUpperCase()
    // 文本反查
    return textToLetter[s] || s
  }

  if (type === '单选') {
    const raw = Array.isArray(answer) ? answer[0] : answer
    return toLetter(raw)
  }

  // 多选
  let arr
  if (Array.isArray(answer)) {
    arr = answer
  } else {
    const s = String(answer).trim()
    // 纯字母串 "ABD" 拆字符
    if (/^[A-Za-z]+$/.test(s)) {
      arr = s.split('')
    } else {
      // 文本按分隔符拆
      arr = s.split(/[,，、\s]+/).filter(Boolean)
    }
  }
  return arr.map(toLetter).filter(Boolean).sort()
}

/**
 * 适配单道题（含复合题子题递归）
 * @param {Object} vo 后端 PracticeQuestionVO
 * @returns {Object} 前端渲染结构
 */
function adaptQuestion(vo) {
  if (!vo) return null
  const type = TYPE_MAP[vo.type] || '题目'
  const options = buildOptions(vo.options, type)

  const item = {
    id: vo.id,
    type,
    typeTag: vo.typeLabel || (type + '题'),
    score: Number(vo.score) || 0,
    title: type === '复合' ? undefined : vo.content,
    correct: normalizeAnswer(vo.answer, type, options),
    analysis: vo.analysis || '',
    knowledge: [],
  }
  if (options) item.options = options

  // 复合题：material = content，递归适配子题
  if (type === '复合') {
    item.material = vo.content
    item.subQuestions = (vo.subQuestions || []).map(adaptSubQuestion)
  }

  // 难度/来源（可选，便于后续扩展，前端当前未使用）
  if (vo.difficultyLabel) item.difficultyLabel = vo.difficultyLabel
  if (vo.subType) item.subType = vo.subType

  return item
}

/**
 * 适配子题（子题不会再嵌套复合题）
 */
function adaptSubQuestion(vo) {
  if (!vo) return null
  const type = TYPE_MAP[vo.type] || '题目'
  const options = buildOptions(vo.options, type)
  const item = {
    id: vo.id,
    type,
    typeTag: vo.typeLabel || (type + '题'),
    title: vo.content,
    correct: normalizeAnswer(vo.answer, type, options),
    analysis: vo.analysis || '',
    knowledge: [],
  }
  if (options) item.options = options
  return item
}

/**
 * 规范化答案
 *  - 单选(1)：归一化为字母字符串（兼容后端存字母或选项文本）
 *  - 多选(2)：归一化为字母数组（兼容后端存 'ABD' 或文本数组）
 *  - 判断(3)：字符串原样返回（"正确"/"错误"）
 *  - 填空(4)：数组 ['答1','答2']
 *  - 简答(5)/计算(6)：字符串原样返回
 *  - 复合(7)：无答案，返回 undefined
 *
 * @param {string|string[]|undefined} answer 后端 answer 字段
 * @param {string} type 适配后的中文题型
 * @param {Array} options 已补字母的选项列表（仅单选/多选需要）
 */
function normalizeAnswer(answer, type, options) {
  if (answer == null || answer === '') return undefined
  if (type === '单选' || type === '多选') {
    return normalizeChoiceAnswer(answer, options, type)
  }
  if (type === '填空') {
    if (Array.isArray(answer)) return answer.slice()
    return [String(answer)]
  }
  if (type === '复合') return undefined
  // 判断/简答/计算：字符串
  return Array.isArray(answer) ? String(answer[0] || '') : String(answer)
}

/**
 * 适配整张试卷
 * @param {Array<Object>} list 后端 PracticeQuestionVO 列表
 * @returns {Array<Object>} 前端题目列表
 */
function adaptPaper(list) {
  if (!Array.isArray(list)) return []
  return list.map(adaptQuestion).filter(Boolean)
}

module.exports = {
  adaptQuestion,
  adaptPaper,
  TYPE_MAP,
}
