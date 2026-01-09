<template>
  <div class="list-container" style="padding: 24px;">
    <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
      <div style="display: flex; align-items: center; gap: 10px;">
        <h2 style="margin: 0;">我的短链接</h2>
        <el-tag type="info">{{ tableData.length }} 条记录</el-tag>
      </div>
      <el-button type="primary" @click="handleRefresh">刷新列表</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" style="width: 100%; border-radius: 8px;" border stripe>
      <el-table-column label="短链接" min-width="180">
        <template #default="{ row }">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-link 
              type="primary" 
              :href="linkPrefix + row.shortCode" 
              target="_blank" 
              :underline="false"
            >
              {{ 't.neozeng.com/' + row.shortCode }}
            </el-link>
            <el-button link icon="DocumentCopy" @click="copyLink(row.shortCode)" />
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="longUrl" label="原始链接" show-overflow-tooltip min-width="250" />

      <el-table-column label="访问统计" width="130" align="center">
        <template #default="{ row }">
          <el-tag :type="row.totalClicks > 0 ? 'danger' : 'info'" effect="plain">
            🔥 {{ row.totalClicks || 0 }} 次
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="expireTime" label="有效期" width="180">
        <template #default="{ row }">
          {{ row.expireTime || '永久有效' }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link @click="openDetails(row)">管理</el-button>
          <el-popconfirm title="确定要删除吗？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    
    <el-drawer v-model="drawerVisible" title="链接详情" size="35%" direction="rtl">
      <div v-if="selectedItem">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;">
          <div style="font-weight:600">{{ 't.neozeng.com/' + selectedItem.shortCode }}</div>
          <el-button size="small" type="primary" @click="copyLink(selectedItem.shortCode!)">复制链接</el-button>
        </div>

        <el-descriptions column="1" size="small" border>
          <el-descriptions-item label="原始链接">{{ selectedItem.longUrl }}</el-descriptions-item>
          <el-descriptions-item label="访问次数">{{ selectedItem.totalClicks || 0 }}</el-descriptions-item>
          <el-descriptions-item label="有效期">{{ selectedItem.expireTime || '永久有效' }}</el-descriptions-item>
          <el-descriptions-item label="创建于">{{ selectedItem.createTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top:20px;">
          <div style="font-weight:600;margin-bottom:6px;">后端返回数据 (JSON)</div>
          <pre style="background:#1e1e1e;color:#d4d4d4;padding:12px;border-radius:6px;font-size:12px;overflow:auto;">{{ jsonString }}</pre>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { shortLinkApi, ShortLinkItem } from '~/api/shortLink'
import { DocumentCopy } from '@element-plus/icons-vue'

const tableData = ref<ShortLinkItem[]>([])
const loading = ref(false)
const drawerVisible = ref(false)
const selectedItem = ref<ShortLinkItem | null>(null)

const jsonString = computed(() => selectedItem.value ? JSON.stringify(selectedItem.value, null, 2) : '')

const loadData = async () => {
  loading.value = true
  try {
    const res = await shortLinkApi.list() as any
    tableData.value = Array.isArray(res) ? res : res.data
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

// 💡 确保环境变量末尾有斜杠，如果没有则手动处理
const linkPrefix = import.meta.env.VITE_SHORT_LINK_BASE || 'http://localhost:8080/api/shortLink/';

const copyLink = async (shortCode: string) => {
  try {
    // 💡 这里已经有拼接逻辑了，所以传入的参数只能是 shortCode
    const fullUrl = `${linkPrefix}${shortCode}`;
    await navigator.clipboard.writeText(fullUrl);
    ElMessage.success('链接已复制');
  } catch (err) {
    ElMessage.error('复制失败');
  }
};

const openDetails = (row: ShortLinkItem) => {
  selectedItem.value = row
  drawerVisible.value = true
}

const handleRefresh = () => loadData()

const handleDelete = async (id: number) => {
  try {
    await shortLinkApi.delete(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

const handleWindowFocus = () => {
  console.log('检测到回到页面，自动更新统计数据...');
  loadData();
};

onMounted(() => {
  loadData();
  window.addEventListener('focus', handleWindowFocus);
});

onUnmounted(() => {
  // 💡 补全了之前缺失的闭合逻辑
  window.removeEventListener('focus', handleWindowFocus);
});
</script>