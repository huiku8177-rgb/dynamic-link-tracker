import type { UserModule } from './types'
import { ViteSSG } from 'vite-ssg'
import { getToken, isGuestMode, removeToken } from '~/api/request'
import { authApi } from '~/api/auth'
import { useAuth } from '~/composables/useAuth'

// import "~/styles/element/index.scss";

// import ElementPlus from "element-plus";
// import all element css, uncommented next line
// import "element-plus/dist/index.css";

// or use cdn, uncomment cdn link in `index.html`

import { routes } from 'vue-router/auto-routes'
import App from './App.vue'

import '~/styles/index.scss'

import 'uno.css'
// If you want to use ElMessage, import it.
import 'element-plus/theme-chalk/src/message.scss'
import 'element-plus/theme-chalk/src/message-box.scss'
import 'element-plus/theme-chalk/src/overlay.scss' // the modal class for message box

// if you do not need ssg:
// import { createApp } from "vue";

// const app = createApp(App);
// app.use(createRouter({
//   history: createWebHistory(),
//   routes,
// }))
// // app.use(ElementPlus);
// app.mount("#app");

// https://github.com/antfu/vite-ssg
export const createApp = ViteSSG(
  App,
  {
    routes,
    base: import.meta.env.BASE_URL,
  },
  (ctx) => {
    // install all modules under `modules/`
    Object.values(import.meta.glob<{ install: UserModule }>('./modules/*.ts', { eager: true }))
      .forEach(i => i.install?.(ctx))

    // 仅在浏览器端执行登录恢复和路由守卫
    if (import.meta.env.SSR) return

    const token = getToken()

    // 路由守卫：统一处理未登录和游客模式
    ctx.router.beforeEach((to, _from, next) => {
      const hasToken = !!getToken()
      const isGuest = isGuestMode()
      const isPublicRoute = to.path === '/login' || to.path === '/register'

      // 1. 根路径默认跳转到登录页
      if (to.path === '/') {
        next('/login')
        return
      }

      // 2. 公开路由（登录/注册页）：已登录用户访问时跳转到控制台
      if (isPublicRoute && hasToken && !isGuest) {
        next('/dashboard')
        return
      }

      // 3. 非公开路由：未登录且非游客时，统一重定向到登录页
      if (!isPublicRoute && !hasToken && !isGuest) {
        next('/login')
        return
      }

      // 4. 其他情况正常放行
      next()
    })

    // 游客模式：本地有 isGuest 即可，跳过后端校验
    if (!token || isGuestMode()) {
      return
    }

    // 有 Token：调用后端 /api/user/info 恢复用户信息
    const { setUserInfo } = useAuth()
    authApi.getUserInfo()
      .then((info) => {
        setUserInfo(info)
      })
      .catch(() => {
        // Token 无效时清理并保持未登录状态
        removeToken()
      })
  },
)
