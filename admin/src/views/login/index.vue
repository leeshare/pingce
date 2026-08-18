<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1 class="title">陕西单招学习服务平台</h1>
        <p class="subtitle">管理后台</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  loading.value = true
  try {
    // validate 校验失败会 reject，必须放进 try 否则会冒泡触发 dev-server overlay
    await formRef.value.validate()
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (err) {
    // 校验失败：Element Plus 在表单项上标红，无需额外弹窗
    // 登录失败：由 request.js 拦截器统一 ElMessage.error 提示，这里不再重复弹
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2B6CB0 0%, #3182CE 100%);
}

.login-box {
  width: 400px;
  background: #fff;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .title {
    font-size: 24px;
    font-weight: 600;
    color: #2B6CB0;
    margin: 0 0 8px;
  }

  .subtitle {
    font-size: 14px;
    color: #718096;
    margin: 0;
  }
}

.login-btn {
  width: 100%;
}
</style>
