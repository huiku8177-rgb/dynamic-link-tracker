<template>
  <div class="p-4">
    <h2 class="text-2xl font-bold mb-4">链接管理</h2>
    <el-table :data="[]" style="width: 100%" border>
      <el-table-column prop="shortUrl" label="短链接" />
      <el-table-column prop="longUrl" label="原始链接" />
      <el-table-column label="操作" width="120">
        <el-button link type="primary">编辑</el-button>
      </el-table-column>
    </el-table>
  </div>
</template>