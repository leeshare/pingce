<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span>综评刷题</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#2C5282"
        text-color="#fff"
        active-text-color="#F6AD55"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>概览</span>
        </el-menu-item>
        <el-sub-menu v-if="hasAnyQuestionPerm" index="/question">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>题库中心</span>
          </template>
          <el-menu-item v-if="userStore.hasPermission('question:view')" index="/question/view">
            <el-icon><Reading /></el-icon>
            <span>试题查看</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('question:entry')" index="/question/entry">
            <el-icon><EditPen /></el-icon>
            <span>试题录入</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('question:import')" index="/question/import">
            <el-icon><Upload /></el-icon>
            <span>批量导入</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('question:proofread')" index="/question/proofread">
            <el-icon><View /></el-icon>
            <span>试题校对</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('question:edit')" index="/question/edit">
            <el-icon><Edit /></el-icon>
            <span>试题编辑</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('question:review')" index="/question/review">
            <el-icon><Checked /></el-icon>
            <span>试题审核</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu
          v-if="userStore.hasPermission('college:list') || userStore.hasPermission('course:list')"
          index="/content"
        >
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item v-if="userStore.hasPermission('college:list')" index="/college/list">
            <el-icon><School /></el-icon>
            <span>院校管理</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('course:list')" index="/course/list">
            <el-icon><VideoCamera /></el-icon>
            <span>课程管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu
          v-if="userStore.hasPermission('paper:maintain') || userStore.hasPermission('paper:publish')"
          index="/paper"
        >
          <template #title>
            <el-icon><Notebook /></el-icon>
            <span>试卷管理</span>
          </template>
          <el-menu-item v-if="userStore.hasPermission('paper:maintain')" index="/paper/maintain">
            <el-icon><Edit /></el-icon>
            <span>真卷维护</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('paper:publish')" index="/paper/publish">
            <el-icon><Promotion /></el-icon>
            <span>试卷发布</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu
          v-if="userStore.hasPermission('admin:user:list') || userStore.hasPermission('admin:student:list')"
          index="/user"
        >
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item v-if="userStore.hasPermission('admin:user:list')" index="/user/list">
            <el-icon><Avatar /></el-icon>
            <span>管理员列表</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('admin:student:list')" index="/student/list">
            <el-icon><UserFilled /></el-icon>
            <span>学员列表</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span>{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-tag v-if="userStore.isSuper" type="danger" size="small" effect="dark" class="super-tag">超管</el-tag>
              {{ userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '管理后台')

const QUESTION_PERMS = ['question:view', 'question:entry', 'question:import', 'question:proofread', 'question:edit', 'question:review']
const hasAnyQuestionPerm = computed(() =>
  userStore.isSuper || QUESTION_PERMS.some((p) => userStore.hasPermission(p)),
)

const handleCommand = async (cmd) => {
  if (cmd === 'logout') {
    await userStore.logout()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.layout {
  height: 100vh;
}

.sidebar {
  background-color: #2C5282;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    font-weight: 600;
    background-color: #2B6CB0;
  }

  :deep(.el-menu) {
    border-right: none;
  }
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid #E2E8F0;

  .header-left {
    font-size: 18px;
    font-weight: 600;
    color: #1A202C;
  }

  .user-info {
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 6px;
    color: #4A5568;

    .super-tag {
      margin-right: 4px;
    }
  }
}

.main {
  background: #F7FAFC;
  padding: 24px;
}
</style>
