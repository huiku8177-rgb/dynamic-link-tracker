<template>
  <div class="list-container" style="padding: 24px;">
    <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
      <div style="display: flex; align-items: center; gap: 10px;">
        <h2 style="margin: 0;">我的短链接</h2>
        <el-tag type="info">{{ tableData.length }} 条记录</el-tag>
      </div>
      <el-button type="primary" @click="handleRefresh">刷新列表</el-button>
    </div>

    <el-table 
      :data="tableData" 
      v-loading="loading" 
      style="width: 100%; border-radius: 8px;" 
      border 
      stripe
      empty-text="暂无数据，点击右上角「新建短链接」开始创建"
    >
      <el-table-column label="短链接" min-width="180">
        <template #default="{ row }">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-link 
              type="primary" 
              :href="linkPrefix + row.shortCode" 
              target="_blank" 
              :underline="false"
            >
              {{ linkPrefix.replace('http://', '').replace('https://', '').replace(/\/$/, '') + '/' + row.shortCode }}
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

      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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
          <div style="font-weight:600">{{ linkPrefix.replace('http://', '').replace('https://', '').replace(/\/$/, '') + '/' + selectedItem.shortCode }}</div>
          <el-button size="small" type="primary" @click="copyLink(selectedItem.shortCode!)">复制链接</el-button>
        </div>

        <el-descriptions :column="1" size="small" border>
          <el-descriptions-item label="原始链接">{{ selectedItem.longUrl }}</el-descriptions-item>
          <el-descriptions-item label="访问次数">{{ selectedItem.totalClicks || 0 }}</el-descriptions-item>
          <el-descriptions-item label="有效期">{{ selectedItem.expireTime || '永久有效' }}</el-descriptions-item>
          <el-descriptions-item label="创建于">{{ selectedItem.createTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top:20px;">
          <div style="font-weight:600;margin-bottom:6px;">详细信息</div>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="工作空间">{{ selectedItem.workspace || '-' }}</el-descriptions-item>
            <el-descriptions-item label="短码">{{ selectedItem.shortCode }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-drawer>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑短链接"
      width="520px"
      :close-on-click-modal="false"
      @close="handleEditDialogClose"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="rules"
        label-width="90px"
        status-icon
      >
        <el-form-item label="原始链接" prop="longUrl">
          <el-input
            v-model="editForm.longUrl"
            placeholder="请输入原始链接"
            clearable
          />
        </el-form-item>
        <el-form-item label="有效期" prop="expireTime">
          <el-date-picker
            v-model="editForm.expireTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择有效期"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitEdit">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { shortLinkApi, type ShortLinkItem, type UpdateShortLinkParam } from '~/api/shortLink'
import { configApi } from '~/api/config'

const tableData = ref<ShortLinkItem[]>([])
const loading = ref(false)
const drawerVisible = ref(false)
const selectedItem = ref<ShortLinkItem | null>(null)
const editDialogVisible = ref(false)
const submitting = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive<UpdateShortLinkParam & { id: number | null }>({
  id: null,
  longUrl: '',
  expireTime: ''
})
const rules: FormRules = {
  longUrl: [
    { required: true, message: '请输入原始链接', trigger: 'blur' },
    { type: 'url', message: '请输入合法的 URL', trigger: 'blur' }
  ],
  expireTime: [
    { required: true, message: '请选择有效期', trigger: 'change' }
  ]
}

// 动态获取的链接前缀（从系统配置中获取）
const linkPrefix = ref('http://localhost:8080/')

// 加载系统配置中的基础域名
const loadBaseDomain = async () => {
  try {
    const configs = await configApi.getAll()
    if (configs.base_domain) {
      // 确保末尾有斜杠
      linkPrefix.value = configs.base_domain.endsWith('/') 
        ? configs.base_domain 
        : configs.base_domain + '/'
    }
  } catch (error) {
    console.warn('获取基础域名配置失败，使用默认值')
  }
}

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

const copyLink = async (shortCode: string) => {
  try {
    // 💡 这里已经有拼接逻辑了，所以传入的参数只能是 shortCode
    const fullUrl = `${linkPrefix.value}${shortCode}`;
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

const openEdit = (row: ShortLinkItem) => {
  editForm.id = row.id
  editForm.longUrl = row.longUrl
  editForm.expireTime = row.expireTime || ''
  editDialogVisible.value = true
}

const resetEditForm = () => {
  if (editFormRef.value) editFormRef.value.clearValidate()
  editForm.id = null
  editForm.longUrl = ''
  editForm.expireTime = ''
}

const handleEditDialogClose = () => {
  resetEditForm()
}

const handleSubmitEdit = async () => {
  if (!editFormRef.value) return
  const valid = await editFormRef.value.validate()
  if (!valid || editForm.id === null) return
  submitting.value = true
  try {
    const payload: UpdateShortLinkParam = {
      longUrl: editForm.longUrl.trim(),
      expireTime: editForm.expireTime
    }
    await shortLinkApi.update(editForm.id, payload)
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    resetEditForm()
    loadData()
  } catch (error: any) {
    ElMessage.error(error?.message || '修改失败')
  } finally {
    submitting.value = false
  }
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
  loadData();
};

// 监听短链接创建事件，自动刷新列表
const handleShortLinkCreated = () => {
  loadData();
};

onMounted(async () => {
  // 先加载配置，再加载数据
  await loadBaseDomain();
  loadData();
  window.addEventListener('focus', handleWindowFocus);
  window.addEventListener('shortLinkCreated', handleShortLinkCreated);
});

onUnmounted(() => {
  window.removeEventListener('focus', handleWindowFocus);
  window.removeEventListener('shortLinkCreated', handleShortLinkCreated);
});
</script>