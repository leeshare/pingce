// pages/index/index.js
const { isLoggedIn } = require('../../utils/auth')
const { get } = require('../../utils/request')

Page({
  data: {
    // 轮播图
    banners: [
      { id: 1, image: '/assets/banners/banner1.png', title: '2026 陕西单招真题上线' },
      { id: 2, image: '/assets/banners/banner2.png', title: '综评志愿测评免费体验' },
    ],
    // 功能入口
    modules: [
      { id: 'practice', name: '刷题练习', icon: '📝', color: '#2B6CB0', url: '/pages/practice/practice' },
      { id: 'exam', name: '全真模考', icon: '🏆', color: '#38A169', url: '/pages/exam/exam' },
      { id: 'wrongbook', name: '错题本', icon: '❌', color: '#E53E3E', url: '/pages/wrongbook/wrongbook' },
      { id: 'college', name: '院校库', icon: '🏫', color: '#DD6B20', url: '/pages/college/college' },
      { id: 'assess', name: '志愿测评', icon: '🎯', color: '#805AD5', url: '/pages/assess/assess' },
      { id: 'course', name: '线下课程', icon: '📚', color: '#319795', url: '/pages/course/course' },
    ],
    // 今日推荐题目
    todayQuestions: [],
    // 热门院校
    hotColleges: [],
    // 登录状态
    isLogged: false,
    userInfo: null,
  },

  onLoad() {
    // 检查登录状态
    if (!isLoggedIn()) {
      // 未登录时仍然显示首页（但功能需登录后使用）
      this.setData({ isLogged: false })
    } else {
      const app = getApp()
      this.setData({
        isLogged: true,
        userInfo: app.globalData.userInfo,
      })
    }
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 })
    }
  },

  // 点击功能模块
  handleModule(e) {
    const { url, id } = e.currentTarget.dataset

    if (!isLoggedIn()) {
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
      return
    }

    // tabBar 页面用 switchTab，非 tabBar 页面用 navigateTo
    const tabUrls = ['/pages/practice/practice', '/pages/wrongbook/wrongbook']
    if (tabUrls.includes(url)) {
      wx.switchTab({ url })
    } else {
      wx.navigateTo({ url })
    }
  },

  // 轮播图点击
  handleBanner(e) {
    const { id } = e.currentTarget.dataset
    console.log('点击轮播图:', id)
  },
})
