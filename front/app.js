// app.js - 小程序入口
App({
  globalData: {
    baseUrl: 'http://localhost:8080/api',
    token: null,
    userInfo: null,
    systemInfo: null,
  },

  onLaunch() {
    const systemInfo = wx.getSystemInfoSync()
    this.globalData.systemInfo = systemInfo

    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
    }

    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      this.globalData.userInfo = userInfo
    }

    if (!token) {
      wx.reLaunch({ url: '/pages/login/login' })
    }
  },
})
