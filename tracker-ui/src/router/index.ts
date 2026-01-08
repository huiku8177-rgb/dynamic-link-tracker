import { createRouter, createWebHistory } from 'vue-router'
// 注意：import { pa } from ... 这一行如果是自动生成的且没用到，建议删除

const routes = [
  // 1. 根路径重定向：默认跳到“个人空间”
  {
    path: '/',
    redirect: '/personal' 
  },
  
  // 2. 你的核心业务路由
  {
    path: '/personal',
    name: 'Personal',
    component: () => import('~/pages/shortLinkList.vue'),
    meta: { title: '个人空间', type: 'personal' } // 传参：告诉组件这是个人空间
  },
  {
    path: '/team',
    name: 'Team',
    component: () => import('~/pages/shortLinkList.vue'),
    meta: { title: '团队协作', type: 'team' } // 传参：告诉组件这是团队空间
  },

  // 3. 其他功能路由 (保持你原有的)
  {
    path: '/dashboard',
    component: () => import('~/pages/dashboard.vue')
  },
  {
    path: '/links',
    component: () => import('~/pages/links.vue')
  },
  {
    path: '/analytics',
    component: () => import('~/pages/analytics.vue')
  },
  {
    path: '/settings',
    component: () => import('~/pages/settings.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router