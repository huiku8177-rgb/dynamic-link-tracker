import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi, type LoginParams, type RegisterParams, type LoginResult, type UserInfoResult } from '~/api/auth'
import { setToken, removeToken, getToken, isGuestMode, registerClearUserInfo } from '~/api/request'

// 用户信息
interface UserInfo {
  userId: number
  username: string
  nickname: string
  email: string
}

const userInfo = ref<UserInfo | null>(null)

/**
 * 认证相关的 composable
 */
export const useAuth = () => {
  // 是否已登录
  const isLoggedIn = computed(() => {
    return !!getToken() && !!userInfo.value
  })

  // 是否为游客模式（本地 isGuest 标记为 true 且没有正常登录态）
  const isGuest = computed(() => {
    return isGuestMode() && !isLoggedIn.value
  })

  /**
   * 登录
   */
  const login = async (params: LoginParams): Promise<boolean> => {
    try {
      const result = await authApi.login(params)
      setToken(result.token)
      userInfo.value = {
        userId: result.userId,
        username: result.username,
        nickname: result.nickname,
        email: result.email
      }
      ElMessage.success('登录成功')
      return true
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败')
      return false
    }
  }

  /**
   * 注册
   */
  const register = async (params: RegisterParams): Promise<boolean> => {
    try {
      const result = await authApi.register(params)
      setToken(result.token)
      userInfo.value = {
        userId: result.userId,
        username: result.username,
        nickname: result.nickname,
        email: result.email
      }
      ElMessage.success('注册成功')
      return true
    } catch (error: any) {
      ElMessage.error(error.message || '注册失败')
      return false
    }
  }

  /**
   * 登出
   */
  const logout = () => {
    removeToken()
    userInfo.value = null
    ElMessage.success('已退出登录')
  }

  /**
   * 清除用户信息（用于 401 等场景强制清理）
   */
  const clearUserInfo = () => {
    userInfo.value = null
  }

  // 注册清除函数到全局，供响应拦截器使用
  registerClearUserInfo(clearUserInfo)

  /**
   * 设置用户信息（用于从 Token 中恢复）
   */
const setUserInfo = (info: UserInfo | UserInfoResult) => {
  userInfo.value = {
    userId: info.userId,
    username: info.username,
    nickname: info.nickname,
    email: info.email,
  }
}

  /**
   * 获取用户信息
   */
  const getUserInfo = () => {
    return userInfo.value
  }

  return {
    isLoggedIn,
    isGuest,
    userInfo: computed(() => userInfo.value),
    login,
    register,
    logout,
    setUserInfo,
    getUserInfo,
    clearUserInfo
  }
}

