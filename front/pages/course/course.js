// pages/course/course.js
Page({
  data: {
    courses: [],
  },

  onLoad() {
    wx.showToast({ title: '线下课程开发中', icon: 'none' })
  },

  handleBook() {
    wx.showToast({ title: '预约功能开发中', icon: 'none' })
  },
})
