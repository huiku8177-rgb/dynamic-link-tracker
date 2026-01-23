<script lang="ts" setup>
import {
  Document,
  Menu as IconMenu,
  Location,
  Setting,
  Link,
  PieChart
} from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'

const $route = useRoute()

function handleOpen(key: string, keyPath: string[]) {
  console.log('打开菜单:', key)
}
function handleClose(key: string, keyPath: string[]) {
  console.log('关闭菜单:', key)
}
</script>

<template>
  <el-menu
    router
    class="el-menu-vertical-demo"
    :default-active="$route.path"
    @open="handleOpen"
    @close="handleClose"
  >
    <el-menu-item index="/dashboard">
      <el-icon><Location /></el-icon>
      <template #title>控制台</template>
    </el-menu-item>

    <el-menu-item index="/links">
      <el-icon><Link /></el-icon>
      <template #title>链接管理</template>
    </el-menu-item>

    <el-menu-item index="/analytics">
      <el-icon><PieChart /></el-icon>
      <template #title>访问统计</template>
    </el-menu-item>

    <el-menu-item index="/settings">
      <el-icon><Setting /></el-icon>
      <template #title>系统设置</template>
    </el-menu-item>
  </el-menu>
</template>

<style scoped>
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}
</style>