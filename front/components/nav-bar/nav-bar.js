// components/nav-bar/nav-bar.js - 自定义导航栏组件
Component({
  properties: {
    title: {
      type: String,
      value: '',
    },
    showBack: {
      type: Boolean,
      value: false,
    },
    bg: {
      type: String,
      value: '#2B6CB0',
    },
    color: {
      type: String,
      value: '#FFFFFF',
    },
  },
  data: {
    statusBarHeight: 20,
    navBarHeight: 44,
  },
  lifetimes: {
    attached() {
      const sys = wx.getSystemInfoSync()
      this.setData({
        statusBarHeight: sys.statusBarHeight,
        navBarHeight: 44,
      })
    },
  },
  methods: {
    onBack() {
      wx.navigateBack({ delta: 1 })
    },
  },
})
