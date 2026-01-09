import request from './request';

// 1. 定义后端统一返回的结构
export interface Result<T> {
  code: number;
  message: string;
  data: T;
}
 
// 2. 定义短链接数据项结构（对应后端实体类）
export interface ShortLinkItem {
  id: number;           // 自增主键
  longUrl: string;      // 原始链接
  shortCode: string;    // 短码
  workspace: string;    // 工作空间
  totalClicks: number;  // 点击统计
  expireTime: string;   // 过期时间
  createTime: string;   // 创建时间
}

export interface CreateShortLinkParam {
  longUrl: string;
  workspace: string;
  expireDate?: string;
}

export const shortLinkApi = {
  /**
   * 创建短链接
   */
  create(data: CreateShortLinkParam) {
    return request<string>({
      url: '/shortLink/create',
      method: 'post',
      data
    });
  },

  /**
   * 🚨 新增：获取短链接列表
   * 对应后端 @GetMapping("/list") 接口
   */
  list() {
    return request<ShortLinkItem[]>({
      url: '/shortLink/list', // 请根据你 Controller 的实际 @RequestMapping 路径调整
      method: 'get'
    });
  },

  /**
   * 🚨 新增：删除短链接
   * @param id 短链接 ID
   */
  delete(id: number) {
    return request<void>({
      url: `/shortLink/${id}`,
      method: 'delete'
    });
  }
};