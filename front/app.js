// app.js - 小程序入口
App({
  globalData: {
    baseUrl: 'http://localhost:8080/api',
    token: null,
    userInfo: null,
    systemInfo: null,
  },

  onLaunch() {
    // 获取系统信息（用于适配状态栏高度等）
    const systemInfo = wx.getSystemInfoSync()
    this.globalData.systemInfo = systemInfo

    // 检查本地缓存的 token
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
    }

    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      this.globalData.userInfo = userInfo
    }
  },
})
