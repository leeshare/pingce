// pages/college/detail.js
Page({
  data: {
    collegeId: null,
  college: null,
  programs: [],
  years: [],
  news: [],
  staff: [],
  env: [],
  major: [],
  line: [],
  similar: [],
    content: ''
  },

  onLoad(options) {
    this.setData({ collegeId: options.id })
    // 院校详情将在 W2 数据接口实现后加载
  },

  handleCall() {
    if (!this.data.college || !this.data.college.phone) return
    wx.makePhoneCall({ phoneNumber: this.data.college.phone })
  },

  handleConsult() {
    wx.showToast({ title: '咨询功能开发中', icon: 'none' })
  },
})
