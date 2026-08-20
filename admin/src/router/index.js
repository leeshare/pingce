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
        path: 'question/view',
        name: 'QuestionView',
        component: () => import('@/views/question/view.vue'),
        meta: { title: '试题查看', icon: 'Reading', permission: 'question:view' },
      },
      {
        path: 'question/entry',
        name: 'QuestionEntry',
        component: () => import('@/views/question/entry.vue'),
        meta: { title: '试题录入', icon: 'EditPen', permission: 'question:entry' },
      },
      {
        path: 'question/import',
        name: 'QuestionImport',
        component: () => import('@/views/question/import.vue'),
        meta: { title: '批量导入', icon: 'Upload', permission: 'question:import' },
      },
      {
        path: 'question/proofread',
        name: 'QuestionProofread',
        component: () => import('@/views/question/proofread.vue'),
        meta: { title: '试题校对', icon: 'View', permission: 'question:proofread' },
      },
      {
        path: 'question/edit',
        name: 'QuestionEdit',
        component: () => import('@/views/question/edit.vue'),
        meta: { title: '试题编辑', icon: 'Edit', permission: 'question:edit' },
      },
      {
        path: 'question/review',
        name: 'QuestionReview',
        component: () => import('@/views/question/review.vue'),
        meta: { title: '试题审核', icon: 'Checked', permission: 'question:review' },
      },
      {
        path: 'college/list',
        name: 'CollegeList',
        component: () => import('@/views/college/list.vue'),
        meta: { title: '院校管理', icon: 'School', permission: 'college:list' },
      },
      {
        path: 'course/list',
        name: 'CourseList',
        component: () => import('@/views/course/list.vue'),
        meta: { title: '课程管理', icon: 'Reading', permission: 'course:list' },
      },
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('@/views/user/list.vue'),
        meta: { title: '用户管理', icon: 'User', permission: 'admin:user:list' },
      },
      {
        path: 'student/list',
        name: 'StudentList',
        component: () => import('@/views/student/list.vue'),
        meta: { title: '学员列表', icon: 'UserFilled', permission: 'admin:student:list' },
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
    return
  }
  if (!userStore.token) {
    next('/login')
    return
  }
  // 校验权限
  if (to.meta.permission && !userStore.hasPermission(to.meta.permission)) {
    next({ path: '/dashboard' })
    return
  }
  next()
})

export default router
