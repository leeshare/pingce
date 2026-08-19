// pages/course/course.js
const { isLoggedIn } = require('../../utils/auth')

Page({
  data: {
    // 主推付费课程（考前面试技巧冲刺班，固定展示）
    featured: {
      id: 'f1',
      name: '考前面试技巧冲刺班',
      desc: '礼仪规范 · 自我介绍 · 高频问答 · 模拟演练',
      price: 99,
      originalPrice: 199,
    },

    // 次级付费课程
    paidCourses: [
      {
        id: 'p1',
        name: '2027 届考生面试攻略',
        desc: '院校偏好 · 评分标准 · 真题拆解 · 避坑指南',
        price: 49,
        icon: '🏅',
        iconBg: 'paid-icon-amber',
      },
    ],

    // 学科免费试听课程
    subjects: [
      {
        name: '语文',
        courses: [
          {
            id: 'c1',
            name: '语文基础知识精讲',
            lessons: 12,
            icon: '📄',
            iconBg: 'free-icon-red',
          },
          {
            id: 'c2',
            name: '阅读理解答题技巧',
            lessons: 8,
            icon: '✍️',
            iconBg: 'free-icon-red',
          },
        ],
      },
      {
        name: '数学',
        courses: [
          {
            id: 'c3',
            name: '数学公式与运算',
            lessons: 15,
            icon: '🧮',
            iconBg: 'free-icon-slate',
          },
        ],
      },
      {
        name: '英语',
        courses: [
          {
            id: 'c4',
            name: '英语高频词汇与语法',
            lessons: 10,
            icon: '💬',
            iconBg: 'free-icon-amber',
          },
        ],
      },
    ],
  },

  onLoad() {},

  // 登录校验
  ensureLogin(cb) {
    if (isLoggedIn()) {
      cb && cb()
      return
    }
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
  },

  // 主推课程购买
  buyFeatured() {
    this.ensureLogin(() => this.confirmBuy(this.data.featured.name, this.data.featured.price))
  },

  // 次级付费课程购买
  buyCourse(e) {
    const { name, price } = e.currentTarget.dataset
    this.ensureLogin(() => this.confirmBuy(name, Number(price)))
  },

  // 弹窗确认购买（演示：唤起微信支付）
  confirmBuy(name, price) {
    wx.showModal({
      title: '确认购买',
      content: `课程：${name}\n价格：¥${price}\n\n（演示：将唤起微信支付）`,
      confirmText: '确认支付',
      success(res) {
        if (res.confirm) {
          wx.showToast({ title: '已唤起微信支付（演示）', icon: 'none' })
        }
      },
    })
  },

  // 免费试听
  tryListen(e) {
    const { name } = e.currentTarget.dataset
    this.ensureLogin(() => {
      wx.showToast({ title: `开始试听：${name}`, icon: 'none' })
    })
  },

  // 我的课程
  goMyCourses() {
    this.ensureLogin(() => {
      wx.showToast({ title: '我的课程列表即将上线', icon: 'none' })
    })
  },
})
