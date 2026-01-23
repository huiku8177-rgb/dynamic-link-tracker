<template>
  <div class="empty-state-container">
    <div class="empty-icon">
      <el-icon :size="80" :color="iconColor">
        <component :is="icon" />
      </el-icon>
    </div>
    <h3 class="empty-title">{{ title }}</h3>
    <p class="empty-description">{{ description }}</p>
    <div class="empty-actions" v-if="showActions">
      <slot name="actions">
        <el-button type="primary" @click="$emit('action')">
          {{ actionText }}
        </el-button>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Component } from 'vue'

interface Props {
  title?: string
  description?: string
  icon?: Component | string
  iconColor?: string
  showActions?: boolean
  actionText?: string
}

withDefaults(defineProps<Props>(), {
  title: '暂无数据',
  description: '点击上方按钮开始创建',
  icon: 'Document',
  iconColor: '#909399',
  showActions: false,
  actionText: '开始创建'
})

defineEmits<{
  action: []
}>()
</script>

<style scoped lang="scss">
.empty-state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  min-height: 300px;
}

.empty-icon {
  margin-bottom: 24px;
  opacity: 0.6;
}

.empty-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.empty-description {
  margin: 0 0 24px 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.empty-actions {
  margin-top: 16px;
}
</style>

