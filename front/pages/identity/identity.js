// pages/identity/identity.js
const { post } = require('../../utils/request')

// 陕西省市区县数据
const cityData = {
  cities: ['西安市', '咸阳市', '宝鸡市', '渭南市', '铜川市', '汉中市', '安康市', '榆林市', '延安市', '商洛市'],
  districts: {
    '西安市': ['新城区', '碑林区', '莲湖区', '雁塔区', '灞桥区', '未央区', '阎良区', '临潼区', '长安区', '高陵区', '鄠邑区', '蓝田县', '周至县'],
    '咸阳市': ['秦都区', '杨陵区', '渭城区', '三原县', '泾阳县', '乾县', '礼泉县', '永寿县', '长武县', '旬邑县', '淳化县', '武功县', '兴平市', '彬州市'],
    '宝鸡市': ['渭滨区', '金台区', '陈仓区', '凤翔区', '岐山县', '扶风县', '眉县', '陇县', '千阳县', '麟游县', '凤县', '太白县'],
    '渭南市': ['临渭区', '华州区', '潼关县', '大荔县', '合阳县', '澄城县', '蒲城县', '白水县', '富平县', '韩城市', '华阴市'],
    '铜川市': ['王益区', '印台区', '耀州区', '宜君县'],
    '汉中市': ['汉台区', '南郑区', '城固县', '洋县', '西乡县', '勉县', '宁强县', '略阳县', '镇巴县', '留坝县', '佛坪县'],
    '安康市': ['汉滨区', '汉阴县', '石泉县', '宁陕县', '紫阳县', '岚皋县', '平利县', '镇坪县', '旬阳市', '白河县'],
    '榆林市': ['榆阳区', '横山区', '府谷县', '靖边县', '定边县', '绥德县', '米脂县', '佳县', '吴堡县', '清涧县', '子洲县', '神木市'],
    '延安市': ['宝塔区', '安塞区', '延长县', '延川县', '志丹县', '吴起县', '甘泉县', '富县', '洛川县', '宜川县', '黄龙县', '黄陵县', '子长市'],
    '商洛市': ['商州区', '洛南县', '丹凤县', '商南县', '山阳县', '镇安县', '柞水县'],
  },
}

Page({
  data: {
    selectedIdentity: '',
    provinceList: ['陕西省'],
    provinceIndex: 0,
    cityList: [],
    cityIndex: 0,
    districtList: [],
    districtIndex: 0,
    school: '',
    canSubmit: false,

    // 弹窗
    showChangeModal: false,
    changeReason: '',
  },

  onLoad() {
    // 初始化城市列表
    this.setData({ cityList: cityData.cities })
  },

  // 选择考生身份
  selectIdentity(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ selectedIdentity: id })
    this.checkCanSubmit()
  },

  onProvinceChange(e) {
    this.setData({ provinceIndex: e.detail.value })
    this.checkCanSubmit()
  },

  onCityChange(e) {
    const cityIndex = e.detail.value
    const cityName = cityData.cities[cityIndex]
    const districts = cityData.districts[cityName] || []

    this.setData({
      cityIndex,
      districtList: districts,
      districtIndex: 0,
    })
    this.checkCanSubmit()
  },

  onDistrictChange(e) {
    this.setData({ districtIndex: e.detail.value })
    this.checkCanSubmit()
  },

  onSchoolInput(e) {
    this.setData({ school: e.detail.value })
  },

  // 检查是否可以提交
  checkCanSubmit() {
    const { selectedIdentity, cityList, cityIndex, districtList, districtIndex } = this.data
    const hasIdentity = !!selectedIdentity
    const hasCity = cityList.length > 0 && cityIndex >= 0
    const hasDistrict = districtList.length > 0 && districtIndex >= 0

    this.setData({ canSubmit: hasIdentity && hasCity && hasDistrict })
  },

  // 提交身份
  submitIdentity() {
    if (!this.data.canSubmit) return

    const { selectedIdentity, cityList, cityIndex, districtList, districtIndex, school } = this.data
    const cityName = cityList[cityIndex]
    const districtName = districtList[districtIndex]

    wx.showLoading({ title: '提交中...', mask: true })

    post('/auth/identity', {
      identity: selectedIdentity,
      province: '陕西省',
      city: cityName,
      district: districtName,
      school: school || '',
    }, { loading: false })
      .then(() => {
        wx.hideLoading()

        // 更新本地缓存
        const userInfo = wx.getStorageSync('userInfo') || {}
        userInfo.identity = selectedIdentity
        userInfo.city = cityName
        userInfo.district = districtName
        userInfo.school = school || '未填写'
        wx.setStorageSync('userInfo', userInfo)

        const app = getApp()
        if (app.globalData.userInfo) {
          app.globalData.userInfo.identity = selectedIdentity
        }

        wx.showToast({ title: '身份确认成功', icon: 'success' })
        setTimeout(() => {
          wx.switchTab({ url: '/pages/index/index' })
        }, 1000)
      })
      .catch(err => {
        wx.hideLoading()
        wx.showToast({
          title: err.message || '提交失败，请重试',
          icon: 'none',
        })
      })
  },

  // 返回
  goBack() {
    wx.navigateBack({ delta: 1 })
  },

  // 弹窗
  openChangeModal() {
    this.setData({ showChangeModal: true })
  },

  closeChangeModal() {
    this.setData({ showChangeModal: false })
  },

  stopPropagation() {},

  onChangeReasonInput(e) {
    this.setData({ changeReason: e.detail.value })
  },

  submitChange() {
    const reason = this.data.changeReason.trim()
    if (!reason) {
      wx.showToast({ title: '请填写修改原因', icon: 'none' })
      return
    }

    wx.showLoading({ title: '提交中...', mask: true })

    post('/auth/identity-change', { reason }, { loading: false })
      .then(() => {
        wx.hideLoading()
        wx.showToast({ title: '修改申请已提交', icon: 'success' })
        this.setData({ showChangeModal: false, changeReason: '' })
      })
      .catch(err => {
        wx.hideLoading()
        wx.showToast({
          title: err.message || '提交失败，请重试',
          icon: 'none',
        })
      })
  },
})
