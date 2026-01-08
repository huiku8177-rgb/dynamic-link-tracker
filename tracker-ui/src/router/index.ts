import { pa } from 'element-plus/es/locales.mjs';

const routes = [
  {
    path: '/dashboard',
    component: () => import('~/pages/dashboard.vue') // 对应“控制台”
  },
  {
    path: '/links',
    component: () => import('~/pages/links.vue')     // 对应“链接管理”
  },
  {
    path: '/analytics',
    component: () => import('~/pages/analytics.vue') // 对应“访问统计”
  },
  {
    path: '/settings',
    component: () => import('~/pages/settings.vue')   // 对应“设置”
  }
]