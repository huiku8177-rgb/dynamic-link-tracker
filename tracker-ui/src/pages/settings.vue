<template>
  <div class="p-6 space-y-6">
    <!-- 未登录时显示提示 -->
    <LoginPrompt
      v-if="!isLoggedIn"
      icon="warning"
      title="需要登录"
      sub-title="系统设置需要登录后才能访问，请先登录"
    />

    <!-- 已登录时显示设置内容 -->
    <template v-else>
      <!-- 页面标题 -->
      <div>
        <h2 class="text-2xl font-bold mb-2">系统设置 (System Settings)</h2>
        <p class="text-sm text-gray-400">配置全局参数，管理系统运行环境</p>
      </div>

      <!-- 全局参数配置 -->
    <el-card shadow="hover">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-semibold">全局参数配置</span>
          <el-tag v-if="hasUnsavedChanges" type="warning" size="small">有未保存的更改</el-tag>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="150px"
        label-position="left"
      >
        <!-- 基础域名/IP 设置 -->
        <el-form-item label="基础域名/IP" prop="base_domain">
          <el-input
            v-model="formData.base_domain"
            placeholder="例如：http://192.168.1.16:8080 或 https://t.neozeng.com"
            clearable
            @input="hasUnsavedChanges = true"
          >
            <template #prepend>
              <el-icon><i-ep-link /></el-icon>
            </template>
          </el-input>
          <div class="text-xs text-gray-400 mt-1">
            💡 此域名将用于生成短链接的完整 URL。可以是局域网 IP（如 http://192.168.1.16:8080）或公网域名
          </div>
        </el-form-item>

        <!-- 默认有效期 -->
        <el-form-item label="默认有效期" prop="default_expire_days">
          <el-input-number
            v-model.number="formData.default_expire_days"
            :min="1"
            :max="365"
            :step="1"
            @change="hasUnsavedChanges = true"
          />
          <span class="ml-2 text-sm text-gray-400">天</span>
          <div class="text-xs text-gray-400 mt-1">
            💡 新建短链接时的默认过期时长，范围：1-365 天
          </div>
        </el-form-item>

        <!-- 保存按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            :loading="saving"
            :disabled="!hasUnsavedChanges"
            @click="handleSave"
          >
            <el-icon v-if="!saving" class="mr-1"><i-ep-check /></el-icon>
            {{ saving ? '保存中...' : '保存配置' }}
          </el-button>
          <el-button
            v-if="hasUnsavedChanges"
            @click="loadConfigs"
          >
            <el-icon class="mr-1"><i-ep-refresh-left /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 配置说明 -->
    <el-card shadow="hover">
      <template #header>
        <span class="font-semibold">配置说明</span>
      </template>
      <ul class="text-sm text-gray-400 space-y-2">
        <li>
          <strong class="text-gray-300">基础域名/IP：</strong>
          用于生成短链接的完整访问地址。修改后，新生成的短链接将使用新域名
        </li>
        <li>
          <strong class="text-gray-300">默认有效期：</strong>
          创建短链接时的默认过期时长，用户可以在创建时自定义修改
        </li>
        <li>
          <strong class="text-gray-300">局域网使用场景：</strong>
          如果在局域网内使用，请将基础域名设置为服务器的局域网 IP 和端口，例如：http://192.168.1.16:8080
        </li>
        <li>
          <strong class="text-gray-300">公网使用场景：</strong>
          如果已部署到公网，请设置为你的域名，例如：https://t.neozeng.com
        </li>
      </ul>
    </el-card>

    <!-- 当前配置预览 -->
    <el-card shadow="hover">
      <template #header>
        <span class="font-semibold">当前配置预览</span>
      </template>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="基础域名/IP">
          <el-tag size="small">{{ formData.base_domain || '未设置' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="默认有效期">
          <el-tag size="small" type="success">{{ formData.default_expire_days }} 天</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="短链接示例">
          <code class="text-xs text-blue-500">
            {{ formData.base_domain || 'http://localhost:8080' }}/abc123
          </code>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useAuth } from '~/composables/useAuth'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { configApi } from '~/api/config'
import LoginPrompt from '~/components/LoginPrompt.vue'

const { isLoggedIn } = useAuth()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  base_domain: 'http://localhost:8080',
  default_expire_days: 7,
})

// 是否有未保存的更改
const hasUnsavedChanges = ref(false)

// 保存状态
const saving = ref(false)

// 表单验证规则
const rules: FormRules = {
  base_domain: [
    { required: true, message: '请输入基础域名/IP', trigger: 'blur' },
    {
      pattern: /^https?:\/\/.+/,
      message: '请输入有效的 URL（必须以 http:// 或 https:// 开头）',
      trigger: 'blur',
    },
  ],
  default_expire_days: [
    { required: true, message: '请输入默认有效期', trigger: 'blur' },
    {
      type: 'number',
      min: 1,
      max: 365,
      message: '有效期必须在 1-365 天之间',
      trigger: 'change',
    },
  ],
}

// 加载配置
const loadConfigs = async () => {
  try {
    const configs = await configApi.getAll()
    
    // 更新表单数据
    if (configs.base_domain) {
      formData.base_domain = configs.base_domain
    }
    if (configs.default_expire_days) {
      formData.default_expire_days = Number(configs.default_expire_days)
    }
    
    hasUnsavedChanges.value = false
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.warning('加载配置失败，使用默认值')
  }
}

// 保存配置
const handleSave = async () => {
  if (!formRef.value) return

  // 验证表单
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.error('请检查表单填写是否正确')
    return
  }

  saving.value = true
  try {
    // 准备配置数据
    const configs = {
      base_domain: formData.base_domain.trim(),
      default_expire_days: String(formData.default_expire_days),
    }

    // 提交到后端
    await configApi.update(configs)
    
    hasUnsavedChanges.value = false
    ElMessage.success('配置保存成功！')
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

// 页面挂载时加载配置
onMounted(() => {
  loadConfigs()
})
</script>