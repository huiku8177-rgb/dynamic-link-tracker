import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '~/router'

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

/**
 * 获取 Token
 */
export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY)
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
}

// 请求拦截器：自动添加 Token
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      // 优先使用 Authorization 头
      config.headers = config.headers || {}
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

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
    // 401 未授权，清除 Token 并跳转到登录页
    if (error.response?.status === 401) {
      removeToken()
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
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