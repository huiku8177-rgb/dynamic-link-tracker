import request from './request'

/**
 * 系统配置接口
 */
export const configApi = {
  /**
   * 获取所有系统配置
   */
  getAll() {
    return request<Record<string, string>>({
      url: '/config',
      method: 'get',
    })
  },

  /**
   * 批量更新系统配置
   * @param configs 配置对象，例如 { base_domain: 'http://192.168.1.16:8080', default_expire_days: '7' }
   */
  update(configs: Record<string, string>) {
    return request<void>({
      url: '/config',
      method: 'post',
      data: configs,
    })
  },

  /**
   * 获取单个配置项
   * @param key 配置键
   */
  get(key: string) {
    return request<string>({
      url: `/config/${key}`,
      method: 'get',
    })
  },
}

