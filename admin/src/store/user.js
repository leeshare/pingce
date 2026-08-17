import { defineStore } from 'pinia'
import { login as loginApi, getInfo as getInfoApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    userInfo: JSON.parse(localStorage.getItem('admin_user') || 'null'),
  }),
  getters: {
    isSuper: (state) => state.userInfo?.isSuper === 1,
    permissions: (state) => state.userInfo?.permissions || [],
    hasPermission: (state) => (code) => {
      const info = state.userInfo
      if (!info) return false
      if (info.isSuper === 1) return true
      const perms = info.permissions || []
      return perms.includes('*') || perms.includes(code)
    },
  },
  actions: {
    async login(payload) {
      const data = await loginApi(payload)
      this.token = data.token
      this.userInfo = data.user
      localStorage.setItem('admin_token', data.token)
      localStorage.setItem('admin_user', JSON.stringify(data.user))
    },
    async refreshInfo() {
      const data = await getInfoApi()
      this.userInfo = data
      localStorage.setItem('admin_user', JSON.stringify(data))
      return data
    },
    async logout() {
      try {
        if (this.token) await logoutApi()
      } catch (e) {
        // ignore
      }
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')
    },
  },
})
