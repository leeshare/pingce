// utils/auth.js - 登录鉴权工具

const { post } = require('./request')

/**
 * 微信登录流程
 * 注意：wx.getUserProfile 必须在用户 TAP 手势的同步调用链中触发
 * 1. 先调用 wx.getUserProfile（获取用户信息，在 tap 链中）
 * 2. 再调用 wx.login 获取 code（在 getUserProfile 回调中）
 * 3. 调用后端 /auth/wx-login 接口换取 token
 */
function login() {
  return new Promise((resolve, reject) => {
    // 第一步：获取用户信息（必须在 tap 手势链中）
    wx.getUserProfile({
      desc: '用于完善用户资料',
      success(profileRes) {
        const { nickName, avatarUrl, gender } = profileRes.userInfo

        // 第二步：获取登录 code
        wx.login({
          success(loginRes) {
            if (!loginRes.code) {
              reject(new Error('wx.login 获取 code 失败'))
              return
            }

            // 第三步：调用后端登录接口
            post('/auth/wx-login', {
              code: loginRes.code,
              nickname: nickName,
              avatar: avatarUrl,
              gender: gender,
            }, { loading: true })
              .then(data => {
                wx.setStorageSync('token', data.token)
                wx.setStorageSync('userInfo', {
                  userId: data.userId,
                  openid: data.openid,
                  nickname: data.nickname,
                  avatar: data.avatar,
                  memberLevel: data.memberLevel,
                })

                const app = getApp()
                app.globalData.token = data.token
                app.globalData.userInfo = data

                resolve(data)
              })
              .catch(err => {
                reject(err)
              })
          },
          fail(err) {
            reject(new Error('wx.login 失败'))
          },
        })
      },
      fail(err) {
        reject(new Error('用户拒绝授权'))
      },
    })
  })
}

/**
 * 检查是否已登录
 */
function isLoggedIn() {
  return !!wx.getStorageSync('token')
}

/**
 * 退出登录
 */
function logout() {
  wx.removeStorageSync('token')
  wx.removeStorageSync('userInfo')
  const app = getApp()
  app.globalData.token = null
  app.globalData.userInfo = null
  wx.redirectTo({ url: '/pages/login/login' })
}

/**
 * 获取用户信息
 */
function getUserInfo() {
  return wx.getStorageSync('userInfo')
}

module.exports = { login, isLoggedIn, logout, getUserInfo }
