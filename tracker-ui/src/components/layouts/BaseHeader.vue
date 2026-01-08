<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { repository } from '~/../package.json'
import { toggleDark } from '~/composables'
import { Plus, Link } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// --- 弹窗逻辑控制 ---
const dialogVisible = ref(false)
const formLabelWidth = '100px'

const linkForm = reactive({
  longUrl: '',
  workspace: '个人项目',
  expireDate: ''
})

const openCreateDialog = () => {
  dialogVisible.value = true
}

const handleCreate = () => {
  // 明天这里将对接后端 Java 接口
  console.log('提交数据:', linkForm)
  ElMessage.success('短链接生成成功！（后端模拟）')
  dialogVisible.value = false
  // 清空表单
  linkForm.longUrl = ''
}
</script>

<template>
  <el-menu class="el-menu-demo" mode="horizontal" :ellipsis="false" router>
    <el-menu-item index="/">
      <div class="flex items-center justify-center gap-2">
        <div class="text-xl" i-ep-link /> 
        <span class="font-bold text-lg">Dynamic Link Tracker</span>
      </div>
    </el-menu-item>

    <div class="flex-grow" /> <el-menu-item index="/create">
<el-button type="primary" round @click="openCreateDialog">
  + 新建短链接
</el-button>
    </el-menu-item>

    <el-sub-menu index="2">
      <template #title>
        默认工作空间
      </template>
      <el-menu-item index="2-1">个人项目</el-menu-item>
      <el-menu-item index="2-2">团队协作</el-menu-item>
      <el-divider style="margin: 4px 0" />
      <el-menu-item index="2-3">管理工作区...</el-menu-item>
    </el-sub-menu>

    <el-menu-item index="/notifications">
      消息通知
    </el-menu-item>

    <el-menu-item h="full" @click="toggleDark()">
      <button
        class="w-full cursor-pointer border-none bg-transparent"
        style="height: var(--ep-menu-item-height)"
      >
        <i inline-flex i="dark:ep-moon ep-sunny" />
      </button>
    </el-menu-item>

    <el-menu-item h="full">
      <a class="size-full flex items-center justify-center" :href="repository.url" target="_blank" title="查看源码">
        <div i-ri-github-fill />
      </a>
    </el-menu-item>
  </el-menu>


  <el-dialog v-model="dialogVisible" title="创建新的短链接" width="500px" append-to-body>
    <el-form :model="linkForm">
      <el-form-item label="原始链接" :label-width="formLabelWidth">
        <el-input v-model="linkForm.longUrl" autocomplete="off" placeholder="请粘贴以 http(s):// 开头的长链接" />
      </el-form-item>
      <el-form-item label="所属空间" :label-width="formLabelWidth">
        <el-select v-model="linkForm.workspace" placeholder="选择归属项目">
          <el-option label="个人项目" value="personal" />
          <el-option label="团队协作" value="team" />
        </el-select>
      </el-form-item>
      <el-form-item label="有效期至" :label-width="formLabelWidth">
        <el-date-picker v-model="linkForm.expireDate" type="date" placeholder="选择过期时间（可选）" style="width: 100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">立即生成</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="scss">
.el-menu-demo {
  // 移除之前 nth-child(1) 的靠左逻辑，使用 flex-grow 更加灵活
  border-bottom: 1px solid var(--el-border-color-light);
  
  .flex-grow {
    flex-grow: 1;
  }
}
</style>