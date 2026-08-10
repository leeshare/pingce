// pages/index/index.js
const { isLoggedIn, getUserInfo } = require('../../utils/auth')
const { get } = require('../../utils/request')

// 根据当前小时返回问候语
function buildGreeting() {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 11) return '早上好'
  if (h < 13) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
}

Page({
  data: {
    greeting: '早上好',
    nickname: '同学',
    isVip: false,
    todayTarget: '今日目标：完成30道综评真题',
    stats: {
      today: 0,
      total: 0,
      accuracy: 0,
    },
    categories: [
      { id: 'pga', name: '普高综评', desc: '职业适应性测试', icon: '🎓' },
      { id: 'sanxiao', name: '三校生单招', desc: '职业技能测试', icon: '🔧' },
      { id: 'history', name: '近10年真题', desc: '历年真题汇编', icon: '📅' },
      { id: 'mock', name: '模拟试题', desc: '高频考点练习', icon: '📋' },
    ],
    continueLearning: {
      title: '普高综评 · 职业适应性测试',
      percent: 36,
    },
    newsList: [
      { id: 1, tag: '政策', tagType: 'policy', title: '2025年陕西省高职综合评价招生政策解读' },
      { id: 2, tag: '资讯', tagType: 'info', title: '陕西综评单招常见面试问题与答题技巧' },
      { id: 3, tag: '热点', tagType: 'hot', title: '三校生单招职业技能测试大纲发布' },
    ],
  },

  onLoad() {
    this.setData({ greeting: buildGreeting() })
    this.refreshUserState()
  },

  onShow() {
    this.refreshUserState()
    if (isLoggedIn()) {
      this.loadHomeStats()
    }
  },

  // 刷新登录状态与昵称
  refreshUserState() {
    if (isLoggedIn()) {
      const userInfo = getUserInfo() || {}
      this.setData({
        nickname: userInfo.nickname || '同学',
        isVip: (userInfo.memberLevel || 0) > 0,
      })
    } else {
      this.setData({ nickname: '同学', isVip: false })
    }
  },

  // 拉取首页统计数据
  loadHomeStats() {
    get('/stats/home')
      .then(data => {
        if (!data) return
        this.setData({
          stats: {
            today: data.today || 0,
            total: data.total || 0,
            accuracy: data.accuracy || 0,
          },
        })
      })
      .catch(() => {})
  },

  // 跳转登录
  ensureLogin(cb) {
    if (isLoggedIn()) {
      cb && cb()
      return
    }
    wx.showModal({
      title: '提示',
      content: '请先登录后再使用该功能',
      confirmText: '去登录',
      success(res) {
        if (res.confirm) {
          wx.navigateTo({ url: '/pages/login/login' })
        }
      },
    })
  },

  // 开始刷题
  goPractice() {
    this.ensureLogin(() => wx.switchTab({ url: '/pages/practice/practice' }))
  },

  // 全真模考
  goExam() {
    this.ensureLogin(() => wx.navigateTo({ url: '/pages/exam/exam' }))
  },

  // 错题本
  goWrongBook() {
    this.ensureLogin(() => wx.navigateTo({ url: '/pages/wrongbook/wrongbook' }))
  },

  // 分类入口
  goCategory(e) {
    const { id } = e.currentTarget.dataset
    this.ensureLogin(() => {
      wx.navigateTo({ url: `/pages/practice/practice?category=${id}` })
    })
  },

  // 继续学习
  goContinue() {
    this.ensureLogin(() => wx.switchTab({ url: '/pages/practice/practice' }))
  },

  // 全部记录
  goHistory() {
    this.ensureLogin(() => wx.switchTab({ url: '/pages/practice/practice' }))
  },

  // 院校库
  goCollege() {
    this.ensureLogin(() => wx.switchTab({ url: '/pages/college/college' }))
  },

  // 志愿测评
  goAssess() {
    this.ensureLogin(() => wx.navigateTo({ url: '/pages/assess/assess' }))
  },

  // 资讯列表
  goNews() {
    wx.showToast({ title: '资讯中心即将上线', icon: 'none' })
  },

  // 资讯详情
  goNewsDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.showToast({ title: `资讯 ${id} 即将上线`, icon: 'none' })
  },
})
