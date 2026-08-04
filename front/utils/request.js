// utils/request.js - HTTP 请求封装

const { baseUrl } = require('./config')

/**
 * 统一请求封装
 * @param {Object} options { url, method, data, header, loading }
 */
function request(options) {
  const {
    url,
    method = 'GET',
    data = {},
    header = {},
    loading = true,
  } = options

  // 获取 token
  const token = wx.getStorageSync('token')
  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }

  if (loading) {
    wx.showLoading({ title: '加载中...', mask: true })
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${baseUrl}${url}`,
      method: method,
      data: data,
      header: {
        'Content-Type': 'application/json',
        ...header,
      },
      success(res) {
        if (loading) wx.hideLoading()

        // HTTP 状态码判断
        if (res.statusCode === 401) {
          // token 过期，清除并跳转登录
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          wx.redirectTo({ url: '/pages/login/login' })
          reject(new Error('登录已过期，请重新登录'))
          return
        }

        if (res.statusCode >= 200 && res.statusCode < 300) {
          const body = res.data
          if (body.code === 200) {
            resolve(body.data)
          } else {
            wx.showToast({ title: body.message || '请求失败', icon: 'none' })
            reject(new Error(body.message || '请求失败'))
          }
        } else {
          wx.showToast({ title: `网络错误(${res.statusCode})`, icon: 'none' })
          reject(new Error(`网络错误(${res.statusCode})`))
        }
      },
      fail(err) {
        if (loading) wx.hideLoading()
        wx.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      },
    })
  })
}

// 便捷方法
const get = (url, data, options = {}) => request({ url, method: 'GET', data, ...options })
const post = (url, data, options = {}) => request({ url, method: 'POST', data, ...options })
const put = (url, data, options = {}) => request({ url, method: 'PUT', data, ...options })
const del = (url, data, options = {}) => request({ url, method: 'DELETE', data, ...options })

module.exports = { request, get, post, put, del }
