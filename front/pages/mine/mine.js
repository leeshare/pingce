// pages/mine/mine.js
const { isLoggedIn, logout, getUserInfo } = require('../../utils/auth')

Page({
  data: {
    isLogged: false,
    userInfo: null,
    menus: [
      { id: 'profile', name: '个人资料', icon: '👤', url: '' },
      { id: 'identity', name: '修改身份', icon: '🎓', url: '/pages/identity/identity' },
      { id: 'member', name: '会员中心', icon: '⭐', url: '' },
      { id: 'order', name: '我的订单', icon: '📋', url: '' },
      { id: 'course', name: '我的课程', icon: '📚', url: '' },
      { id: 'feedback', name: '意见反馈', icon: '💬', url: '' },
      { id: 'about', name: '关于我们', icon: 'ℹ️', url: '' },
    ],
  },

  onLoad() {
    this.refreshUser()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 })
    }
    this.refreshUser()
  },

  refreshUser() {
    if (isLoggedIn()) {
      this.setData({
        isLogged: true,
        userInfo: getUserInfo(),
      })
    } else {
      this.setData({ isLogged: false, userInfo: null })
    }
  },

  handleLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },

  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          logout()
          this.refreshUser()
        }
      },
    })
  },

  handleMenu(e) {
    const { name, url } = e.currentTarget.dataset
    if (url) {
      wx.navigateTo({ url })
    } else {
      wx.showToast({ title: `${name} 开发中`, icon: 'none' })
    }
  },
})
