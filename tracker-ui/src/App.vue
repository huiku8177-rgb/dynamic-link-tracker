<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 判断是否为登录/注册页面（需要全屏显示）
const isAuthPage = computed(() => {
  return route.path === '/login' || route.path === '/register'
})
</script>

<template>
  <el-config-provider namespace="ep">
    <!-- 登录/注册页面不显示 Header 和 Sidebar -->
    <template v-if="isAuthPage">
      <RouterView />
    </template>
    <!-- 其他页面显示完整布局 -->
    <template v-else>
      <BaseHeader />
      <div class="main-container flex">
        <BaseSide />
        <div w="full" py="4">
          <RouterView />
        </div>
      </div>
    </template>
  </el-config-provider>
</template>

<style>
#app {
  text-align: center;
  color: var(--ep-text-color-primary);
}

.main-container {
  height: calc(100vh - var(--ep-menu-item-height) - 4px);
}
</style>
