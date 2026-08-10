// pages/college/college.js
const { get } = require('../../utils/request')

Page({
  data: {
    keyword: '',
    // 筛选 chip
    chips: [
      { key: 'city', label: '地区', value: '' },
      { key: 'nature', label: '类型', value: '' },
      { key: 'type', label: '专业方向', value: '' },
    ],
    // 当前激活的快速筛选 tab
    tabs: [
      { key: 'recommend', label: '推荐' },
      { key: 'public', label: '公办' },
      { key: 'private', label: '民办' },
      { key: 'doubleHigh', label: '双高计划' },
    ],
    activeTab: 'recommend',
    list: [],
    page: 1,
    size: 10,
    total: 0,
    loading: false,
    noMore: false,
  },

  onLoad() {
    this.loadList(true)
  },

  onPullDownRefresh() {
    this.loadList(true, () => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    if (this.data.noMore || this.data.loading) return
    this.loadList(false)
  },

  // 构造查询参数
  buildQuery(reset) {
    const { keyword, chips, activeTab, page, size } = this.data
    const q = {
      keyword: keyword || '',
      page: reset ? 1 : page,
      size,
    }
    chips.forEach(c => {
      if (c.value) q[c.key] = c.value
    })
    if (activeTab === 'public') q.nature = '公办'
    else if (activeTab === 'private') q.nature = '民办'
    else if (activeTab === 'doubleHigh') q.isDoubleHigh = 1
    else q.recommend = 1
    return q
  },

  // 加载列表
  loadList(reset, cb) {
    if (this.data.loading) return
    this.setData({ loading: true })
    if (reset) this.setData({ page: 1, noMore: false })

    const query = this.buildQuery(reset)
    get('/college/list', query, { loading: false })
      .then(res => {
        const records = res.records || []
        const list = reset ? records : this.data.list.concat(records)
        const noMore = list.length >= (res.total || 0)
        this.setData({
          list,
          total: res.total || 0,
          page: query.page + 1,
          noMore,
        })
      })
      .catch(() => {})
      .finally(() => {
        this.setData({ loading: false })
        cb && cb()
      })
  },

  // 搜索输入
  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  // 确认搜索
  onSearchConfirm() {
    this.loadList(true)
  },

  // 清空搜索
  onSearchClear() {
    this.setData({ keyword: '' })
    this.loadList(true)
  },

  // 切换快速 tab
  onTabChange(e) {
    const { key } = e.currentTarget.dataset
    if (key === this.data.activeTab) return
    this.setData({ activeTab: key })
    this.loadList(true)
  },

  // 点击筛选 chip（弹出选择）
  onChipTap(e) {
    const { key } = e.currentTarget.dataset
    const options = this.getChipOptions(key)
    const labels = options.map(o => o.label)
    wx.showActionSheet({
      itemList: labels,
      success: res => {
        const selected = options[res.tapIndex]
        const chips = this.data.chips.map(c =>
          c.key === key ? { ...c, value: selected.value } : c
        )
        this.setData({ chips })
        this.loadList(true)
      },
    })
  },

  // 筛选选项
  getChipOptions(key) {
    if (key === 'city') {
      return [
        { label: '不限', value: '' },
        { label: '西安', value: '西安' },
        { label: '咸阳', value: '咸阳' },
        { label: '宝鸡', value: '宝鸡' },
        { label: '渭南', value: '渭南' },
        { label: '汉中', value: '汉中' },
        { label: '延安', value: '延安' },
      ]
    }
    if (key === 'nature') {
      return [
        { label: '不限', value: '' },
        { label: '公办', value: '公办' },
        { label: '民办', value: '民办' },
      ]
    }
    if (key === 'type') {
      return [
        { label: '不限', value: '' },
        { label: '综合', value: '综合' },
        { label: '理工', value: '理工' },
        { label: '师范', value: '师范' },
        { label: '财经', value: '财经' },
        { label: '医药', value: '医药' },
      ]
    }
    return []
  },

  // 跳转院校详情
  goDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/college/detail?id=${id}` })
  },

  // 智能匹配（跳转志愿测评）
  goAssess() {
    wx.navigateTo({ url: '/pages/assess/assess' })
  },
})
