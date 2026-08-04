// pages/assess/assess.js
Page({
  data: {
    step: 0,
    score: '',
    result: null,
  },

  onLoad() {
    wx.showToast({ title: '志愿测评开发中', icon: 'none' })
  },

  handleNext() {
    this.setData({ step: this.data.step + 1 })
  },

  handleReset() {
    this.setData({ step: 0, score: '', result: null })
  },
})
