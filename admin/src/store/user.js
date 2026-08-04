import { defineStore } from 'pinia'
import { login as loginApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    userInfo: JSON.parse(localStorage.getItem('admin_user') || 'null'),
  }),
  actions: {
    async login(payload) {
      const data = await loginApi(payload)
      this.token = data.token
      this.userInfo = data
      localStorage.setItem('admin_token', data.token)
      localStorage.setItem('admin_user', JSON.stringify(data))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')
    },
  },
})
