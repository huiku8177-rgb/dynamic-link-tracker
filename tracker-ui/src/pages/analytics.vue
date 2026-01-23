<template>
  <div class="p-6 space-y-6">
    <!-- 页面标题 -->
    <div>
      <h2 class="text-2xl font-bold mb-2">访问统计 (Analytics)</h2>
      <p class="text-sm text-gray-400">
        <template v-if="isLoggedIn">
          深度数据分析，洞察短链接访问趋势与热点
        </template>
        <template v-else>
          查看公开统计数据。登录后可以查看个人专属数据分析。
        </template>
      </p>
    </div>

    <!-- 未登录提示 -->
    <GuestView
      v-if="!isLoggedIn"
      alert-title="游客模式"
      alert-message="您当前以游客身份访问，可以查看公开统计数据，登录后查看个人专属数据"
      icon="info"
      title="登录查看个人数据"
      sub-title="登录后可以查看个人短链接的详细访问统计和分析"
      :show-alert="true"
    >
      <div class="mt-6 space-y-4">
        <el-alert
          title="公开数据预览"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <p class="text-sm">
              以下展示的是所有用户的汇总统计数据。登录后可以查看您个人创建的短链接的详细访问分析。
            </p>
          </template>
        </el-alert>
      </div>
    </GuestView>

    <!-- 趋势图表区（已登录或显示公开数据） -->
    <el-row :gutter="16">
      <!-- 点击量趋势折线图 -->
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover">
          <template #header>
            <div class="flex items-center justify-between">
              <span>点击量趋势</span>
              <el-radio-group v-model="trendDays" size="small" @change="loadClickTrend">
                <el-radio-button :value="7">近 7 天</el-radio-button>
                <el-radio-button :value="14">近 14 天</el-radio-button>
                <el-radio-button :value="30">近 30 天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" style="width: 100%; height: 320px;"></div>
        </el-card>
      </el-col>

      <!-- 热门排行榜柱状图 -->
      <el-col :xs="24" :lg="10">
        <el-card shadow="hover">
          <template #header>
            <span>热门短链接排行 TOP 5</span>
          </template>
          <div ref="topLinksChartRef" style="width: 100%; height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 访问详情表 -->
    <el-card shadow="hover">
      <template #header>
        <div class="flex items-center justify-between">
          <span>访问详情记录</span>
          <el-tag type="info">共 {{ totalRecords }} 条记录</el-tag>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="visitRecords"
        size="small"
        border
        stripe
        empty-text="暂无访问记录"
      >
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="createTime" label="访问时间" min-width="160" />
        <el-table-column prop="shortCode" label="短码" min-width="100">
          <template #default="{ row }">
            <el-tag size="small" type="primary">{{ row.shortCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="来源 IP" min-width="140" />
        <el-table-column label="地理位置" min-width="120">
          <template #default="{ row }">
            <span>{{ row.location || '未知' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="设备类型" min-width="150">
          <template #default="{ row }">
            <el-tag size="small" :type="getDeviceTagType(row.userAgent)">
              {{ formatDeviceType(row.userAgent) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalRecords"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadVisitRecords"
          @size-change="loadVisitRecords"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useAuth } from '~/composables/useAuth'
import * as echarts from 'echarts'
import { statsApi, type VisitLog, type ClickTrendItem, type TopLinkItem } from '~/api/stats'
import { ElMessage } from 'element-plus'
import GuestView from '~/components/GuestView.vue'

const { isLoggedIn } = useAuth()

// 图表实例
let trendChart: echarts.ECharts | null = null
let topLinksChart: echarts.ECharts | null = null

// 图表 DOM 引用
const trendChartRef = ref<HTMLDivElement>()
const topLinksChartRef = ref<HTMLDivElement>()

// 趋势天数选择
const trendDays = ref(7)

// 访问记录分页
const visitRecords = ref<VisitLog[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalRecords = ref(0)
const loading = ref(false)

// 加载点击量趋势
const loadClickTrend = async () => {
  try {
    const data = await statsApi.clickTrend(trendDays.value)
    renderTrendChart(data)
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 加载热门链接排行
const loadTopLinks = async () => {
  try {
    const data = await statsApi.topLinks(5)
    renderTopLinksChart(data)
  } catch (error) {
    console.error('加载热门排行失败:', error)
  }
}

// 加载访问记录
const loadVisitRecords = async () => {
  loading.value = true
  try {
    const response = await statsApi.allVisits(currentPage.value - 1, pageSize.value)
    visitRecords.value = response.content
    totalRecords.value = response.totalElements
  } catch (error) {
    console.error('加载访问记录失败:', error)
    ElMessage.error('加载访问记录失败')
  } finally {
    loading.value = false
  }
}

// 渲染趋势折线图
const renderTrendChart = (data: ClickTrendItem[]) => {
  if (!trendChartRef.value) return

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(item => item.date.substring(5)) // 显示 MM-DD
    },
    yAxis: {
      type: 'value',
      name: '点击量'
    },
    series: [
      {
        name: '点击量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        data: data.map(item => item.clicks)
      }
    ]
  }

  trendChart.setOption(option)
}

// 渲染热门链接柱状图
const renderTopLinksChart = (data: TopLinkItem[]) => {
  if (!topLinksChartRef.value) return

  if (!topLinksChart) {
    topLinksChart = echarts.init(topLinksChartRef.value)
  }

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '点击量'
    },
    yAxis: {
      type: 'category',
      data: data.map(item => item.shortCode).reverse()
    },
    series: [
      {
        name: '点击量',
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 1, color: '#188df0' }
          ]),
          borderRadius: [0, 5, 5, 0]
        },
        label: {
          show: true,
          position: 'right'
        },
        data: data.map(item => item.totalClicks).reverse()
      }
    ]
  }

  topLinksChart.setOption(option)
}

// 格式化设备类型
const formatDeviceType = (ua: string | undefined): string => {
  if (!ua) return '未知设备'
  
  let browser = '未知'
  let device = 'PC'
  
  if (ua.includes('Chrome')) browser = 'Chrome'
  else if (ua.includes('Safari') && !ua.includes('Chrome')) browser = 'Safari'
  else if (ua.includes('Firefox')) browser = 'Firefox'
  else if (ua.includes('Edge')) browser = 'Edge'
  
  if (ua.includes('Mobile') || ua.includes('Android')) device = 'Mobile'
  else if (ua.includes('iPhone')) device = 'iPhone'
  else if (ua.includes('iPad')) device = 'iPad'
  
  return `${browser} · ${device}`
}

// 获取设备标签类型
const getDeviceTagType = (ua: string | undefined): 'success' | 'info' => {
  if (!ua) return 'info'
  if (ua.includes('Mobile') || ua.includes('Android') || ua.includes('iPhone') || ua.includes('iPad')) {
    return 'success'
  }
  return 'info'
}

// 窗口大小改变时重绘图表
const handleResize = () => {
  trendChart?.resize()
  topLinksChart?.resize()
}

onMounted(async () => {
  // 未登录时，不请求后端数据，只展示 GuestView 和登录/注册按钮
  if (!isLoggedIn.value) {
    return
  }

  // 已登录时加载数据
  await Promise.all([
    loadClickTrend(),
    loadTopLinks(),
    loadVisitRecords()
  ])

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

// 当用户在当前页面完成登录后，自动加载数据
watch(
  () => isLoggedIn.value,
  async (val) => {
    if (val) {
      await Promise.all([
        loadClickTrend(),
        loadTopLinks(),
        loadVisitRecords()
      ])
      window.addEventListener('resize', handleResize)
    }
  }
)

onUnmounted(() => {
  // 销毁图表实例
  trendChart?.dispose()
  topLinksChart?.dispose()
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>