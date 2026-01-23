<template>
  <div class="login-prompt-container">
    <el-result
      :icon="icon"
      :title="title"
      :sub-title="subTitle"
    >
      <template #extra>
        <el-button type="primary" @click="goToLogin">前往登录</el-button>
        <el-button @click="goToRegister">立即注册</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

interface Props {
  icon?: 'warning' | 'info' | 'success' | 'error'
  title?: string
  subTitle?: string
}

withDefaults(defineProps<Props>(), {
  icon: 'warning',
  title: '需要登录',
  subTitle: '请先登录后再使用此功能'
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
.login-prompt-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px;
}
</style>
