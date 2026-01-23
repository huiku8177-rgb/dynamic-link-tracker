import request from './request'

// 访问日志结构
export interface VisitLog {
  id: number
  shortCode: string
  ip: string
  location?: string
  userAgent?: string
  createTime: string
}

// 点击量趋势数据点
export interface ClickTrendItem {
  date: string // 日期，格式：YYYY-MM-DD 或 HH:00
  clicks: number // 点击量
}

// 热门链接排行项
export interface TopLinkItem {
  shortCode: string
  longUrl: string
  totalClicks: number
}

export const statsApi = {
  /**
   * 获取最近 N 条访问记录（后端当前返回 5 条）
   */
  recentVisits() {
    return request<VisitLog[]>({
      url: '/shortLink/visits/recent',
      method: 'get',
    })
  },

  /**
   * 获取点击量趋势（过去 N 天）
   * @param days 天数，默认 7
   */
  clickTrend(days: number = 7) {
    return request<ClickTrendItem[]>({
      url: '/shortLink/stats/clickTrend',
      method: 'get',
      params: { days },
    })
  },

  /**
   * 获取热门短链接排行
   * @param limit 返回数量，默认 5
   */
  topLinks(limit: number = 5) {
    return request<TopLinkItem[]>({
      url: '/shortLink/stats/topLinks',
      method: 'get',
      params: { limit },
    })
  },

  /**
   * 获取所有访问记录（分页）
   * @param page 页码（从 0 开始）
   * @param size 每页大小
   */
  allVisits(page: number = 0, size: number = 20) {
    return request<{ content: VisitLog[]; totalElements: number }>({
      url: '/shortLink/visits/all',
      method: 'get',
      params: { page, size },
    })
  },
}

