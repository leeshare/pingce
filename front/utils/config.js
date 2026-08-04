// utils/config.js - 环境配置

const config = {
  // 开发环境（模拟器用 localhost；真机预览改成本机 IP，如 http://192.168.1.25:8080/api）
  dev: {
    baseUrl: 'http://localhost:8080/api',
    baseUrlRealDevice: 'http://192.168.1.25:8080/api',
    wxAppId: 'wx54c4a3e89a269570',
  },
  // 生产环境
  prod: {
    baseUrl: 'https://your-domain.com/api',
    wxAppId: 'wx54c4a3e89a269570',
  },
}

// 当前环境（dev / prod）
const env = 'dev'

module.exports = {
  ...config[env],
  env,
}
