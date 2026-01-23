import request from './request'

// 登录请求参数
export interface LoginParams {
  usernameOrEmail: string
  password: string
}

// 注册请求参数
export interface RegisterParams {
  username: string
  email: string
  password: string
  nickname?: string
}

// 登录结果
export interface LoginResult {
  token: string
  userId: number
  username: string
  nickname: string
  email: string
}

// 用户信息（用于刷新后恢复登录态）
export interface UserInfoResult {
  userId: number
  username: string
  nickname: string
  email: string
}

export const authApi = {
  /**
   * 用户登录
   */
  login(data: LoginParams) {
    return request<LoginResult>({
      url: '/auth/login',
      method: 'post',
      data
    })
  },

  /**
   * 用户注册
   */
  register(data: RegisterParams) {
    return request<LoginResult>({
      url: '/auth/register',
      method: 'post',
      data
    })
  },

  /**
   * 获取当前登录用户信息（用于前端刷新后恢复登录态）
   */
  getUserInfo() {
    return request<UserInfoResult>({
      url: '/user/info',
      method: 'get',
    })
  }
}

