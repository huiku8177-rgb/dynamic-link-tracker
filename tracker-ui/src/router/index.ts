import { createRouter, createWebHistory } from 'vue-router'
import { getToken, isGuestMode } from '~/api/request'

const routes = [
  // 0. 认证相关路由（公开访问）
  {
    path: '/login',
    name: 'Login',
    component: () => import('~/pages/login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('~/pages/register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },

  // 1. 根路径重定向：默认跳到"链接管理"页面
  {
    path: '/',
    redirect: '/links'
  },
  
  // 2. 核心业务路由：链接管理
  {
    path: '/links',
    name: 'Links',
    component: () => import('~/pages/links.vue'),
    meta: { title: '链接管理', requiresAuth: false } // 可根据需要设为 true
  },
  
  // 3. 个人和团队空间（保留用于后续扩展）
  {
    path: '/personal',
    name: 'Personal',
    component: () => import('~/pages/links.vue'),
    meta: { title: '个人空间', type: 'personal', requiresAuth: false }
  },
  {
    path: '/team',
    name: 'Team',
    component: () => import('~/pages/links.vue'),
    meta: { title: '团队协作', type: 'team', requiresAuth: false }
  },

  // 4. 其他功能路由
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('~/pages/dashboard.vue'),
    meta: { title: '控制台', requiresAuth: false }
  },
  {
    path: '/analytics',
    name: 'Analytics',
    component: () => import('~/pages/analytics.vue'),
    meta: { title: '访问统计', requiresAuth: false }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('~/pages/settings.vue'),
    meta: { title: '系统设置', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：如果需要登录的页面没有 Token 且不是游客模式，跳转到登录页
router.beforeEach((to, from, next) => {
  const token = getToken()
  
  // 如果访问登录/注册页且已登录，跳转到首页
  if ((to.path === '/login' || to.path === '/register') && token) {
    next('/')
    return
  }
  
  // 如果路由要求登录但没有 Token 且不是游客模式，跳转到登录页
  if (to.meta.requiresAuth && !token && !isGuestMode()) {
    next('/login')
    return
  }
  
  next()
})

export default router