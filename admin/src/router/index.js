import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true },
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '概览', icon: 'Odometer' },
      },
      {
        path: 'question/list',
        name: 'QuestionList',
        component: () => import('@/views/question/list.vue'),
        meta: { title: '题库管理', icon: 'Document' },
      },
      {
        path: 'college/list',
        name: 'CollegeList',
        component: () => import('@/views/college/list.vue'),
        meta: { title: '院校管理', icon: 'School' },
      },
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('@/views/user/list.vue'),
        meta: { title: '用户管理', icon: 'User' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '首页'} - 管理后台`
  const userStore = useUserStore()
  if (to.meta.public) {
    next()
  } else if (!userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
