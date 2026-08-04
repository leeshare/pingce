// pages/practice/practice.js
const { isLoggedIn } = require('../../utils/auth')

Page({
  data: {
    categories: [
      { id: 1, name: '语文', count: 1200 },
      { id: 2, name: '数学', count: 1500 },
      { id: 3, name: '英语', count: 1000 },
      { id: 4, name: '职业适应性测试', count: 800 },
    ],
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 })
    }
  },

  handleStart(e) {
    if (!isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '请先登录后再开始刷题',
        confirmText: '去登录',
        success(res) {
          if (res.confirm) wx.navigateTo({ url: '/pages/login/login' })
        },
      })
      return
    }
    const { id, name } = e.currentTarget.dataset
    wx.showToast({ title: `${name} 刷题开发中`, icon: 'none' })
  },
})
