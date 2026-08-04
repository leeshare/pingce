// pages/college/college.js
Page({
  data: {
    keyword: '',
    colleges: [],
  },

  onLoad() {
    wx.showToast({ title: '院校库开发中', icon: 'none' })
  },

  onSearch() {
    // 搜索逻辑待 W2 实现
  },
})
