<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <div class="logo-icon" i-ep-link />
          <h1>Dynamic Link Tracker</h1>
        </div>
        <p class="subtitle">登录您的账户</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        label-position="top"
      >
        <el-form-item label="用户名或邮箱" prop="usernameOrEmail">
          <el-input
            v-model="loginForm.usernameOrEmail"
            placeholder="请输入用户名或邮箱"
            size="large"
            clearable
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            clearable
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <div class="login-footer-main">
          <span>还没有账户？</span>
          <el-link type="primary" @click="goToRegister">立即注册</el-link>
        </div>
        <div class="login-footer-guest">
          <span class="text-gray-400 text-sm">只是想先体验一下？</span>
          <el-button type="info" link @click="handleGuestLogin">暂时跳过登录</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { FormInstance, FormRules, ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuth } from '~/composables/useAuth'
import { setGuestMode, removeToken } from '~/api/request'

const router = useRouter()
const { login } = useAuth()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  usernameOrEmail: '',
  password: ''
})

const loginRules: FormRules = {
  usernameOrEmail: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  const valid = await loginFormRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    const success = await login(loginForm)
    if (success) {
      console.log('登录成功，开始跳转到 /links')
      await router.push('/links')
      console.log('跳转完成，当前路径:', router.currentRoute.value.path)
    }
  } catch (err) {
    console.error('跳转失败:', err)
    ElMessage.error('登录后跳转失败，请重试')
    
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}

// 游客模式：不请求后端，直接标记本地状态并进入控制台
const handleGuestLogin = () => {
  // 清除可能残留的登录态
  removeToken()
  // 标记为游客模式
  setGuestMode(true)
  // 跳转到控制台首页
  router.push('/dashboard')
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-bg-color-page);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  background: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-bottom: 12px;

    .logo-icon {
      font-size: 32px;
      color: var(--el-color-primary);
    }

    h1 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .subtitle {
    margin: 0;
    color: var(--el-text-color-secondary);
    font-size: 14px;
  }
}

.login-form {
  :deep(.el-form-item__label) {
    color: var(--el-text-color-primary);
    font-weight: 500;
    margin-bottom: 8px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 8px;
  }
}

.login-footer {
  margin-top: 24px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;

  .login-footer-main {
    text-align: center;

    .el-link {
      margin-left: 4px;
    }
  }

  .login-footer-guest {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}
</style>

