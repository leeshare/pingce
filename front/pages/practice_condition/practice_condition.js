// pages/pastpapers/pastpapers.js
const { isLoggedIn } = require('../../utils/auth')

Page({
  data: {
    years: ['2026', '2025', '2024', '2023', '2022', '2021', '2020', '2019', '2018', '2017', '2016', '2015', '2014'],
    subjects: [
      {
        name: '语文',
        desc: '阅读理解 · 写作 · 基础知识',
        //icon: '📄',
        icon: '/assets/icons/icon-ques-chinese.svg',
        iconBg: 'subject-icon-red',
      },
      {
        name: '数学',
        desc: '代数 · 几何 · 应用题',
        //icon: '🧮',
        icon: '/assets/icons/icon-ques-maths.svg',
        iconBg: 'subject-icon-slate',
      },
      {
        name: '英语',
        desc: '词汇 · 语法 · 阅读理解',
        //icon: '💬',
        icon: '/assets/icons/icon-ques-english.svg',
        iconBg: 'subject-icon-amber',
      },
    ],
    selectedYear: '2026',
    selectedSubject: '',
  },

  selectYear(e) {
    const { year } = e.currentTarget.dataset
    this.setData({ selectedYear: year })
  },

  selectSubject(e) {
    const { subject } = e.currentTarget.dataset
    this.setData({ selectedSubject: subject })
  },

  startPractice() {
    const { selectedYear, selectedSubject } = this.data
    if (!selectedYear || !selectedSubject) return

    if (!isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '请先登录后再开始练习',
        confirmText: '去登录',
        success(res) {
          if (res.confirm) wx.navigateTo({ url: '/pages/login/login' })
        },
      })
      return
    }

    wx.navigateTo({
      url: `/pages/practice/practice?year=${selectedYear}&subject=${selectedSubject}`,
    })
  },
})
