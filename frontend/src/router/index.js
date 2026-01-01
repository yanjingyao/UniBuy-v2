import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import BasicLayout from '@/layout/BasicLayout.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPassword.vue')
  },
  {
    path: '/message-center',
    component: BasicLayout,
    children: [
      {
        path: '',
        name: 'MessageCenter',
        component: () => import('@/views/common/MessageCenter.vue')
      }
    ]
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/common/Chat.vue'),
    meta: { roles: ['student', 'merchant', 'admin'] }
  },
  // Student Routes
  {
    path: '/student',
    component: BasicLayout,
    meta: { roles: ['student'] },
    children: [
      {
        path: 'home',
        name: 'StudentHome',
        component: () => import('@/views/student/Home.vue')
      },
      {
        path: 'publish',
        name: 'PublishRequest',
        component: () => import('@/views/student/PublishRequest.vue')
      },
      {
        path: 'orders',
        name: 'StudentOrders',
        component: () => import('@/views/student/MyOrders.vue')
      },
      {
        path: 'my-requests',
        name: 'MyRequests',
        component: () => import('@/views/student/MyRequests.vue')
      },
      {
        path: 'payment-history',
        name: 'PayHistory',
        component: () => import('@/views/student/PayHistory.vue')
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/Profile.vue')
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/student/ProductDetail.vue')
      }
    ]
  },
  // Merchant Routes
  {
    path: '/merchant',
    component: BasicLayout, // Can be different layout
    meta: { roles: ['merchant'] },
    children: [
      {
        path: 'dashboard',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/Dashboard.vue')
      },
      {
        path: 'products',
        name: 'ProductMgr',
        component: () => import('@/views/merchant/ProductMgr.vue')
      },
      {
        path: 'requests',
        name: 'RequestPool',
        component: () => import('@/views/merchant/RequestPool.vue')
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/OrderHandle.vue')
      },
      {
        path: 'profile',
        name: 'MerchantProfile',
        component: () => import('@/views/merchant/Profile.vue')
      }
    ]
  },
  // Admin Routes
  {
    path: '/admin',
    component: BasicLayout,
    meta: { roles: ['admin'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'audit',
        name: 'AdminAudit',
        component: () => import('@/views/admin/Audit.vue')
      },
      {
        path: 'users',
        name: 'UserMgr',
        component: () => import('@/views/admin/UserMgr.vue')
      },
      {
        path: 'admins',
        name: 'AdminUserMgr',
        component: () => import('@/views/admin/AdminUserMgr.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrderMgr',
        component: () => import('@/views/admin/OrderMgr.vue')
      },
      {
        path: 'system',
        name: 'SystemMgr',
        component: () => import('@/views/admin/SystemMgr.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const userRole = userStore.role
  
  // Whitelist
  if (to.path === '/login' || to.path === '/register' || to.path === '/forgot-password') {
    next()
    return
  }
  
  // Check auth
  if (!userRole) {
    next('/login')
    return
  }
  
  // Check role permission
  if (to.meta.roles && !to.meta.roles.includes(userRole)) {
    // Redirect to correct dashboard based on role
    if (userRole === 'student') next('/student/home')
    else if (userRole === 'merchant') next('/merchant/dashboard')
    else if (userRole === 'admin') next('/admin/dashboard')
    else next('/login')
    return
  }
  
  next()
})

export default router
