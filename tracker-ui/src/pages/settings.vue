<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-6">系统设置</h2>
    <el-form label-position="top">
      <el-card class="mb-6" header="基础配置">
        <el-form-item label="默认跳转域名">
          <el-input placeholder="例如：t.neozeng.com" />
        </el-form-item>
        <el-form-item label="短码长度">
          <el-input-number :min="4" :max="10" />
        </el-form-item>
      </el-card>
      <el-card header="账户安全">
        <el-button type="danger">退出登录</el-button>
      </el-card>
    </el-form>
  </div>
</template>