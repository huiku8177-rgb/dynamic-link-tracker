<template>
  <div class="guest-view-container">
    <!-- 顶部提示栏 -->
    <el-alert
      :title="alertTitle"
      type="info"
      :closable="false"
      show-icon
      class="mb-6"
    >
      <template #default>
        <div class="flex items-center justify-between w-full">
          <span>{{ alertMessage }}</span>
          <div class="ml-4">
            <el-button type="primary" size="small" @click="goToLogin">立即登录</el-button>
            <el-button size="small" @click="goToRegister">注册账号</el-button>
          </div>
        </div>
      </template>
    </el-alert>

    <!-- 内容区域 -->
    <slot>
      <!-- 默认显示登录提示 -->
      <LoginPrompt :icon="icon" :title="title" :sub-title="subTitle" />
    </slot>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import LoginPrompt from './LoginPrompt.vue'

interface Props {
  alertTitle?: string
  alertMessage?: string
  icon?: 'warning' | 'info' | 'success' | 'error'
  title?: string
  subTitle?: string
  showAlert?: boolean
}

withDefaults(defineProps<Props>(), {
  alertTitle: '游客模式',
  alertMessage: '您当前以游客身份访问，部分功能需要登录后使用',
  icon: 'info',
  title: '需要登录',
  subTitle: '登录后可以查看完整数据和功能',
  showAlert: true
})

const router = useRouter()

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped lang="scss">
.guest-view-container {
  padding: 24px;
}
</style>

