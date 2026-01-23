<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { repository } from '~/../package.json'
import { toggleDark } from '~/composables'
import { useAuth } from '~/composables/useAuth'
import { ElMessage } from 'element-plus'
import { User, Setting, SwitchButton } from '@element-plus/icons-vue'
import { shortLinkApi } from '~/api/shortLink'
import { configApi } from '~/api/config'

// --- 弹窗逻辑控制 ---
const dialogVisible = ref(false)
const formLabelWidth = '100px'

// 系统配置
const baseDomain = ref('http://localhost:8080/')
const defaultExpireDays = ref(7)
const DEFAULT_WORKSPACE = '默认空间'

const linkForm = reactive({
  longUrl: '',
  expireDate: ''
})

// 加载系统配置
const loadSystemConfig = async () => {
  try {
    const configs = await configApi.getAll()
    if (configs.base_domain) {
      baseDomain.value = configs.base_domain.endsWith('/') 
        ? configs.base_domain 
        : configs.base_domain + '/'
    }
    if (configs.default_expire_days) {
      defaultExpireDays.value = Number(configs.default_expire_days)
    }
  } catch (error) {
    console.warn('获取系统配置失败，使用默认值')
  }
}

const openCreateDialog = () => {
  // 已登录：正常打开创建对话框
  if (isLoggedIn.value) {
    // 打开对话框时，如果没有设置过期时间，自动设置默认过期时间
    if (!linkForm.expireDate) {
      const defaultDate = new Date()
      defaultDate.setDate(defaultDate.getDate() + defaultExpireDays.value)
      const year = defaultDate.getFullYear()
      const month = String(defaultDate.getMonth() + 1).padStart(2, '0')
      const day = String(defaultDate.getDate()).padStart(2, '0')
      const hours = String(defaultDate.getHours()).padStart(2, '0')
      const minutes = String(defaultDate.getMinutes()).padStart(2, '0')
      const seconds = String(defaultDate.getSeconds()).padStart(2, '0')
      linkForm.expireDate = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
    dialogVisible.value = true
    return
  }

  // 游客或未登录：统一引导去注册页面
  ElMessage.warning('注册后即可创建属于自己的短链接')
  router.push('/register')
}

// 使用自定义事件来通知列表刷新
const handleCreate = async () => { 

  const submitData = {
    longUrl: linkForm.longUrl,
    workspace: DEFAULT_WORKSPACE,
    expireDate: linkForm.expireDate 
  };

  try {
    // 2. 这里的 res 就是后端返回的 Result.success(shortCode) 中的数据部分
    const res = await shortLinkApi.create(submitData);
    
    // 使用系统配置中的基础域名
    const fullUrl = `${baseDomain.value}${res}`

    // 3. 显示成功提示，并自动复制到剪贴板
    try {
      await navigator.clipboard.writeText(fullUrl)
      ElMessage({
        message: `生成成功！链接已复制到剪贴板：${fullUrl}`,
        type: 'success',
        duration: 4000,
        showClose: true
      })
    } catch (err) {
      ElMessage({
        message: `生成成功！地址：${fullUrl}`,
        type: 'success',
        duration: 5000,
        showClose: true
      })
    }

    dialogVisible.value = false;
    
    // 4. 重置表单
    linkForm.longUrl = '';
    linkForm.expireDate = ''; 

    // 5. 触发全局事件，通知列表刷新
    window.dispatchEvent(new CustomEvent('shortLinkCreated'));

  } catch (error: any) {
    ElMessage.error(error.message || '生成失败，请稍后重试')
  }
};

const router = useRouter()
const { isLoggedIn, userInfo, logout } = useAuth()

// 登出处理
const handleLogout = () => {
  logout()
  router.push('/login')
}

// 页面加载时获取系统配置
onMounted(() => {
  loadSystemConfig()
})
</script>

<template>
  <el-menu class="el-menu-demo" mode="horizontal" :ellipsis="false" router>
    <el-menu-item index="/">
      <div class="flex items-center justify-center gap-2">
        <div class="text-xl" i-ep-link /> 
        <span class="font-bold text-lg">Dynamic Link Tracker</span>
      </div>
    </el-menu-item>

    <div class="flex-grow" />

    <el-menu-item index="/links">
<el-button type="primary" round @click="openCreateDialog">
  + 新建短链接
</el-button>
    </el-menu-item>

    <!-- 用户信息区域 -->
    <template v-if="isLoggedIn && userInfo">
      <el-sub-menu index="user-menu">
      <template #title>
          <div class="user-info">
            <el-avatar :size="32" style="margin-right: 8px;">
              {{ userInfo.nickname?.[0] || userInfo.username[0] }}
            </el-avatar>
            <span>{{ userInfo.nickname || userInfo.username }}</span>
          </div>
      </template>
        <el-menu-item index="user-profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
        <el-menu-item index="user-settings">
          <el-icon><Setting /></el-icon>
          <span>账户设置</span>
        </el-menu-item>
      <el-divider style="margin: 4px 0" />
        <el-menu-item @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-menu-item>
    </el-sub-menu>
    </template>

    <!-- 未登录时显示登录/注册按钮 -->
    <template v-else>
      <el-menu-item index="/login">
        <el-button link>登录</el-button>
      </el-menu-item>
      <el-menu-item index="/register">
        <el-button type="primary" round>注册</el-button>
    </el-menu-item>
    </template>

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
      <el-form-item label="有效期至" :label-width="formLabelWidth">
        <el-date-picker v-model="linkForm.expireDate"
         type="datetime" placeholder="选择过期时间（可选）"
          style="width: 100%"
          format="YYYY-MM-DD HH:mm:ss"
  value-format="YYYY-MM-DD HH:mm:ss"
         />
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

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}
</style>