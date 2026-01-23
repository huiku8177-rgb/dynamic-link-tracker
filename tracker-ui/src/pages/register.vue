<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <div class="logo">
          <div class="logo-icon" i-ep-link />
          <h1>Dynamic Link Tracker</h1>
        </div>
        <p class="subtitle">创建您的账户</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        label-position="top"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="3-50个字符"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="6-20个字符"
            size="large"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="昵称（可选）" prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="不填写则使用用户名"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><Avatar /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleRegister"
            style="width: 100%"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账户？</span>
        <el-link type="primary" @click="goToLogin">立即登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Message, Avatar } from '@element-plus/icons-vue'
import { useAuth } from '~/composables/useAuth'

const router = useRouter()
const { register } = useAuth()

const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

// 自定义验证：确认密码
const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在 3-50 个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在 6-20 个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  const valid = await registerFormRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    const success = await register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      nickname: registerForm.nickname || undefined
    })
    if (success) {
      router.push('/')
    }
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped lang="scss">
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-bg-color-page);
  padding: 20px;
}

.register-box {
  width: 100%;
  max-width: 480px;
  padding: 40px;
  background: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.register-header {
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

.register-form {
  :deep(.el-form-item__label) {
    color: var(--el-text-color-primary);
    font-weight: 500;
    margin-bottom: 8px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 8px;
  }
}

.register-footer {
  margin-top: 24px;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;

  .el-link {
    margin-left: 4px;
  }
}
</style>

