// pages/login/login.js
const { login } = require('../../utils/auth')
const { post, get } = require('../../utils/request')

Page({
  data: {
    loading: false,
    agreed: false,
    showPhoneSection: false,
    useSmsLogin: false,
    phone: '',
    code: '',
    codeCountdown: 0,
    smsLoading: false,
    _timer: null,
  },

  onLoad() {},

  onUnload() {
    if (this.data._timer) {
      clearInterval(this.data._timer)
    }
  },

  toggleAgree() {
    this.setData({ agreed: !this.data.agreed })
  },

  checkAgree() {
    if (!this.data.agreed) {
      wx.showToast({
        title: '请先阅读并同意用户服务协议与隐私政策',
        icon: 'none',
      })
      return false
    }
    return true
  },

  // 手机号获取成功后，检查是否已选考生身份，决定跳转
  navigateAfterLogin() {
    get('/auth/identity', {}, { loading: false })
      .then(data => {
        if (data && data.identity) {
          // 已选择身份，进入首页
          const userInfo = wx.getStorageSync('userInfo') || {}
          userInfo.identity = data.identity
          wx.setStorageSync('userInfo', userInfo)
          wx.switchTab({ url: '/pages/index/index' })
        } else {
          // 未选择身份，进入身份选择页
          wx.redirectTo({ url: '/pages/identity/identity' })
        }
      })
      .catch(() => {
        // 接口异常时默认进入首页
        wx.switchTab({ url: '/pages/index/index' })
      })
  },

  // 步骤一：微信账号登录
  handleLogin() {
    if (!this.checkAgree()) return
    if (this.data.loading) return
    this.setData({ loading: true })

    login()
      .then(data => {
        wx.showToast({ title: '微信登录成功', icon: 'success' })
        if (data && data.phone) {
          // 已绑定手机号，检查是否已选身份
          setTimeout(() => {
            this.navigateAfterLogin()
          }, 800)
        } else {
          this.setData({ showPhoneSection: true })
        }
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

  // 步骤二：微信手机号一键授权
  handleGetPhone(e) {
    if (!this.checkAgree()) return

    const detail = e.detail || {}
    const errMsg = detail.errMsg || ''

    // 用户主动取消
    if (errMsg.indexOf('cancel') > -1 || errMsg.indexOf('deny') > -1) {
      wx.showToast({ title: '您已取消授权', icon: 'none' })
      return
    }

    // 无权限：自动切换到短信验证码登录
    if (errMsg.indexOf('no permission') > -1) {
      console.warn('getPhoneNumber 无权限，切换到验证码登录')
      wx.showModal({
        title: '提示',
        //content: '当前小程序未开通微信手机号一键授权接口，将使用手机号验证码登录。',
        content: '当前小程序未开通微信手机号一键授权接口，将使用模拟手机号获取成功。',
        showCancel: false,
        confirmText: '好的',
        success: () => {
          //this.setData({ useSmsLogin: true })

          wx.hideLoading()
          const userInfo = wx.getStorageSync('userInfo') || {}
          userInfo.phone = "15122223333"
          wx.setStorageSync('userInfo', userInfo)
          const app = getApp()
          if (app.globalData.userInfo) {
            app.globalData.userInfo.phone = "15122223333"
          }
          wx.showToast({ title: '模拟手机号授权成功', icon: 'success' })
          setTimeout(() => {
            this.navigateAfterLogin()
          }, 1000)
          
        },
      })
      return
    }

    // 兼容新旧两种返回格式
    const code = detail.code
    const encryptedData = detail.encryptedData
    const iv = detail.iv

    if (!code && !encryptedData) {
      console.error('getPhoneNumber 回调异常:', detail)
      wx.showToast({
        title: errMsg || '手机号授权失败，请重试',
        icon: 'none',
      })
      return
    }

    wx.showLoading({ title: '授权中...', mask: true })

    post('/auth/bind-phone', { code, encryptedData, iv }, { loading: false })
      .then(data => {
        wx.hideLoading()
        const userInfo = wx.getStorageSync('userInfo') || {}
        userInfo.phone = data.phone
        wx.setStorageSync('userInfo', userInfo)
        const app = getApp()
        if (app.globalData.userInfo) {
          app.globalData.userInfo.phone = data.phone
        }
        wx.showToast({ title: '手机号授权成功', icon: 'success' })
        setTimeout(() => {
          this.navigateAfterLogin()
        }, 1000)
      })
      .catch(err => {
        wx.hideLoading()
        wx.showToast({
          title: err.message || '手机号授权失败，请重试',
          icon: 'none',
        })
      })
  },

  // 切换到验证码登录
  switchToSms() {
    this.setData({ useSmsLogin: true })
  },

  // 切换回微信授权
  switchToWx() {
    this.setData({ useSmsLogin: false })
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },

  onCodeInput(e) {
    this.setData({ code: e.detail.value })
  },

  // 发送短信验证码
  sendSmsCode() {
    if (this.data.codeCountdown > 0) return

    const phone = this.data.phone.trim()
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }

    wx.showLoading({ title: '发送中...', mask: true })

    post('/auth/send-sms-code', { phone }, { loading: false })
      .then(() => {
        wx.hideLoading()
        wx.showToast({ title: '验证码已发送', icon: 'success' })

        // 60秒倒计时
        this.setData({ codeCountdown: 60 })
        const timer = setInterval(() => {
          const next = this.data.codeCountdown - 1
          if (next <= 0) {
            clearInterval(timer)
            this.setData({ codeCountdown: 0, _timer: null })
          } else {
            this.setData({ codeCountdown: next })
          }
        }, 1000)
        this.setData({ _timer: timer })
      })
      .catch(err => {
        wx.hideLoading()
        wx.showToast({
          title: err.message || '验证码发送失败，请重试',
          icon: 'none',
        })
      })
  },

  // 验证码登录
  handleSmsLogin() {
    if (!this.checkAgree()) return

    const phone = this.data.phone.trim()
    const code = this.data.code.trim()

    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    if (!/^\d{4,6}$/.test(code)) {
      wx.showToast({ title: '请输入验证码', icon: 'none' })
      return
    }

    this.setData({ smsLoading: true })

    post('/auth/sms-login', { phone, code }, { loading: false })
      .then(data => {
        const userInfo = wx.getStorageSync('userInfo') || {}
        userInfo.phone = phone
        if (data && data.token) {
          wx.setStorageSync('token', data.token)
          const app = getApp()
          app.globalData.token = data.token
        }
        wx.setStorageSync('userInfo', userInfo)
        const app = getApp()
        if (app.globalData.userInfo) {
          app.globalData.userInfo.phone = phone
        }
        wx.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          this.navigateAfterLogin()
        }, 800)
      })
      .catch(err => {
        wx.showToast({
          title: err.message || '验证码错误，请重试',
          icon: 'none',
        })
      })
      .finally(() => {
        this.setData({ smsLoading: false })
      })
  },

  handleAgreement() {
    wx.showModal({
      title: '用户服务协议',
      content: '本小程序尊重并保护用户的个人隐私。为了提供更好的服务，我们会收集您的微信昵称、头像等基本信息。具体请参阅完整协议。',
      showCancel: false,
      confirmText: '我知道了',
    })
  },

  handlePrivacy() {
    wx.showModal({
      title: '隐私政策',
      content: '本小程序尊重并保护用户的个人隐私。为了提供更好的服务，我们会收集您的微信公开信息与手机号，用于考试通知、成绩推送与客服联系。具体请参阅完整隐私政策。',
      showCancel: false,
      confirmText: '我知道了',
    })
  },
})
