// pages/login/login.js
const { login } = require('../../utils/auth')

Page({
  data: {
    loading: false,
  },

  onLoad() {},

  handleLogin() {
    if (this.data.loading) return
    this.setData({ loading: true })

    login()
      .then(data => {
        wx.showToast({
          title: data.isNewUser ? '欢迎加入' : '登录成功',
          icon: 'success',
        })
        // 登录成功后跳转首页
        setTimeout(() => {
          wx.switchTab({ url: '/pages/index/index' })
        }, 1000)
      })
      .catch(err => {
        console.error('登录失败:', err)
        wx.showToast({
          title: err.message || '登录失败，请重试',
          icon: 'none',
        })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  handleAgreement() {
    wx.showModal({
      title: '用户协议与隐私政策',
      content: '本小程序尊重并保护用户的个人隐私。为了提供更好的服务，我们会收集您的微信昵称、头像等基本信息。具体请参阅完整协议。',
      showCancel: false,
      confirmText: '我知道了',
    })
  },
})
