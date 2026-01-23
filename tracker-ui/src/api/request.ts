import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '~/router'

// 全局用户信息清除函数（由 useAuth 注册）
let globalClearUserInfo: (() => void) | null = null

/**
 * 注册全局用户信息清除函数（由 useAuth 调用）
 */
export const registerClearUserInfo = (fn: () => void) => {
  globalClearUserInfo = fn
}

// 后端统一返回格式
interface Result<T> {
  code: number
  message: string
  data: T
}

const service = axios.create({
  baseURL: '/api', // 统一后端地址
  timeout: 10000
})

// Token 存储的 key
const TOKEN_KEY = 'token'
// 游客模式标记 key
const GUEST_KEY = 'isGuest'

/**
 * 获取 Token
 */
export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 是否为游客模式
 */
export const isGuestMode = (): boolean => {
  return localStorage.getItem(GUEST_KEY) === 'true'
}

/**
 * 设置游客模式
 */
export const setGuestMode = (value: boolean): void => {
  if (value) {
    localStorage.setItem(GUEST_KEY, 'true')
  } else {
    localStorage.removeItem(GUEST_KEY)
  }
}

/**
 * 设置 Token
 */
export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除 Token
 */
export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(GUEST_KEY)
}

/**
 * 清除所有认证相关的本地存储
 */
export const clearAuthStorage = (): void => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(GUEST_KEY)
  // 清除其他可能的认证相关存储
  localStorage.removeItem('userInfo')
}

// 请求拦截器：自动添加 Token
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    config.headers = config.headers || {}

    if (token) {
      // 登录态：带上 Bearer Token
      config.headers['Authorization'] = `Bearer ${token}`
    } else if (isGuestMode()) {
      // 游客模式：不带 Token，但加一个特殊头让后端识别
      config.headers['Guest-Access'] = 'true'
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 防止重复跳转的标志
let isRedirecting = false

// 响应拦截器：自动解包后端定义的 Result 规范
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 假设后端 code 为 200 代表成功
    if (res.code !== 200) {
      ElMessage.error(res.message || '系统错误')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data // 直接返回真正的业务数据
  },
  (error) => {
    // 401 未授权
    if (error.response?.status === 401) {
      // 如果是游客模式，不清除状态，不跳转，只提示
      if (isGuestMode()) {
        ElMessage.error('游客模式下权限访问受限，请登录后重试')
        return Promise.reject(error)
      }

      // 防止重复跳转
      if (isRedirecting) {
        return Promise.reject(error)
      }
      isRedirecting = true

      // 1. 清除所有本地存储
      clearAuthStorage()

      // 2. 清除用户状态（如果已注册）
      if (globalClearUserInfo) {
        try {
          globalClearUserInfo()
        } catch (e) {
          console.warn('清除用户信息失败:', e)
        }
      }

      // 3. 显示错误提示
      ElMessage.error('登录已过期，请重新登录')

      // 4. 尝试使用 Vue Router 跳转
      const currentPath = router.currentRoute.value.path
      if (currentPath !== '/login') {
        router.push('/login').catch((err) => {
          // 如果 router.push 失败，使用硬跳转
          console.warn('Router push failed, using hard redirect:', err)
          window.location.href = '/login'
        })
      } else {
        // 如果已经在登录页，使用硬刷新确保状态清理
        setTimeout(() => {
          window.location.reload()
        }, 100)
      }

      // 5. 重置标志（延迟重置，确保跳转完成）
      setTimeout(() => {
        isRedirecting = false
      }, 1000)
    } else {
      ElMessage.error(error.response?.data?.message || '网络异常，请检查后端服务')
    }
    return Promise.reject(error)
  }
)

// 包装函数，提供正确的类型推断
function request<T>(config: AxiosRequestConfig): Promise<T> {
  return service.request(config)
}

export default request