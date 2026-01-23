<template>
  <div class="p-6 space-y-6">
    <!-- 顶部欢迎 + 后端状态 -->
    <div class="flex items-center justify-between gap-4">
      <div>
        <h2 class="text-2xl font-bold mb-2">控制台总览</h2>
        <p class="text-sm text-gray-400">
          <template v-if="isLoggedIn && userInfo">
            欢迎回来，{{ userInfo.nickname || userInfo.username }}！这里是你的短链接运行总览。
          </template>
          <template v-else>
            这里是短链接运行总览。登录后可以查看个人数据和完整功能。
          </template>
        </p>
      </div>
      <div class="flex items-center gap-3">
        <!-- 后端连接状态（基于 API 请求状态） -->
        <el-tag :type="backendStatus === 'online' ? 'success' : 'danger'">
          {{ backendStatus === 'online' ? '后端连接正常' : '后端连接失败' }}
        </el-tag>
      </div>
    </div>

    <!-- 核心指标卡片区 -->
    <el-row :gutter="16">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover">
          <div class="text-xs text-gray-400 mb-1">总点击量</div>
          <div class="text-2xl font-bold mb-1">{{ overview.totalClicks }}</div>
          <div class="text-xs text-gray-400">所有短链接累计点击次数</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" :class="{ 'opacity-60': !isLoggedIn }">
          <div class="text-xs text-gray-400 mb-1">
            活跃链接数
            <el-tag v-if="!isLoggedIn" type="info" size="small" class="ml-2">需登录</el-tag>
          </div>
          <div class="text-2xl font-bold mb-1">
            {{ isLoggedIn ? overview.activeLinks : '***' }}
          </div>
          <div class="text-xs text-gray-400">当前未过期的短链接</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" :class="{ 'opacity-60': !isLoggedIn }">
          <div class="text-xs text-gray-400 mb-1">
            今日新增
            <el-tag v-if="!isLoggedIn" type="info" size="small" class="ml-2">需登录</el-tag>
          </div>
          <div class="text-2xl font-bold mb-1">
            {{ isLoggedIn ? `+${overview.todayNewLinks}` : '+***' }}
          </div>
          <div class="text-xs text-gray-400">今天新创建的短链接</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover">
          <div class="text-xs text-gray-400 mb-1">总链接数</div>
          <div class="text-2xl font-bold mb-1">{{ overview.totalLinks }}</div>
          <div class="text-xs text-gray-400">包含已过期的全部链接</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 未登录提示 -->
    <GuestView
      v-if="!isLoggedIn"
      alert-title="游客模式"
      alert-message="您当前以游客身份访问，部分数据需要登录后查看"
      icon="info"
      title="登录查看完整数据"
      sub-title="登录后可以查看个人活跃链接数、今日新增等详细数据"
      :show-alert="true"
    >
      <div class="mt-6">
        <el-card shadow="hover">
          <template #header>
            <div class="flex items-center justify-between">
              <span>公开数据预览</span>
              <el-tag type="info" size="small">所有用户汇总</el-tag>
            </div>
          </template>
          <div class="text-sm text-gray-400 mb-4">
            以下数据为所有用户的汇总统计，登录后可查看个人专属数据
          </div>
    <el-row :gutter="16">
            <el-col :span="12">
              <div class="text-xs text-gray-400 mb-1">总点击量</div>
              <div class="text-2xl font-bold">{{ overview.totalClicks }}</div>
            </el-col>
            <el-col :span="12">
              <div class="text-xs text-gray-400 mb-1">总链接数</div>
              <div class="text-2xl font-bold">{{ overview.totalLinks }}</div>
            </el-col>
          </el-row>
        </el-card>
      </div>
    </GuestView>

    <!-- 实时动态 + 说明（已登录用户） -->
    <el-row v-else :gutter="16">
      <el-col :xs="24" :md="16">
        <el-card shadow="hover">
          <template #header>
            <div class="flex items-center justify-between">
              <span>实时动态（最近 5 条访问记录）</span>
            </div>
          </template>
          <el-table
            :data="recentVisits"
            size="small"
            border
            empty-text="暂无访问记录"
          >
            <el-table-column prop="createTime" label="访问时间" min-width="160" />
            <el-table-column prop="shortCode" label="短码" min-width="80">
              <template #default="{ row }">
                <el-tag size="small">{{ row.shortCode }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ip" label="来源 IP" min-width="140" />
            <el-table-column label="设备" min-width="120">
              <template #default="{ row }">
                <span>{{ formatUserAgent(row.userAgent) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <el-card shadow="hover">
          <template #header>
            <span>功能说明</span>
          </template>
          <ul class="text-sm text-gray-400 space-y-2 text-left">
            <li>· 上方 4 个核心指标数据基于短链接列表实时统计计算。</li>
            <li>· 实时动态已接入后端访问日志接口（<code>/api/shortLink/visits/recent</code>），展示最近 5 条访问记录。</li>
            <li>· 每次访问短链接时，后端会自动记录 IP、设备信息（User-Agent）等数据到访问日志表。</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuth } from '~/composables/useAuth'
import { shortLinkApi, type ShortLinkItem, type VisitLog } from '~/api/shortLink'
import GuestView from '~/components/GuestView.vue'

const { isLoggedIn, userInfo } = useAuth()

// 后端连接状态
const backendStatus = ref<'online' | 'offline'>('offline')

// Dashboard 概览数据（已接入真实短链接列表接口进行统计计算）
const overview = ref({
  totalClicks: 0,
  activeLinks: 0,
  todayNewLinks: 0,
  totalLinks: 0
})

// 从短链接列表统计概览数据
const calcOverviewFromList = (list: ShortLinkItem[]) => {
  const now = new Date()

  overview.value.totalLinks = list.length
  overview.value.totalClicks = list.reduce(
    (sum, item) => sum + (item.totalClicks || 0),  
    0
  )

  overview.value.activeLinks = list.filter((item) => {
    if (!item.expireTime) return true // 无过期时间视为永久有效
    const expire = new Date(item.expireTime)
    return expire.getTime() > now.getTime()
  }).length

  const todayStr = now.toDateString()
  overview.value.todayNewLinks = list.filter((item) => {
    if (!item.createTime) return false
    const create = new Date(item.createTime)
    return create.toDateString() === todayStr
  }).length
}

// 最近访问记录（已接入后端真实数据）
const recentVisits = ref<VisitLog[]>([])

// 格式化设备信息（从 User-Agent 提取关键信息）
const formatUserAgent = (ua: string | undefined): string => {
  if (!ua) return '未知设备'
  
  // 简单提取浏览器和设备类型
  let browser = '未知浏览器'
  let device = 'PC'
  
  if (ua.includes('Chrome')) browser = 'Chrome'
  else if (ua.includes('Safari')) browser = 'Safari'
  else if (ua.includes('Firefox')) browser = 'Firefox'
  else if (ua.includes('Edge')) browser = 'Edge'
  
  if (ua.includes('Mobile') || ua.includes('Android')) device = 'Mobile'
  else if (ua.includes('iPhone')) device = 'iPhone'
  else if (ua.includes('iPad')) device = 'iPad'
  
  return `${browser} · ${device}`
}

// 页面挂载时获取数据
onMounted(async () => {
  try {
    // 获取短链接列表统计
    const list = await shortLinkApi.list()
    calcOverviewFromList(list)
    
    // 获取最近访问记录
    const visits = await shortLinkApi.recentVisits()
    recentVisits.value = visits
    
    // 如果请求成功，设置后端状态为在线
    backendStatus.value = 'online'
  } catch {
    // 失败时保留默认值，后端状态为离线
    backendStatus.value = 'offline'
  }
})
</script>