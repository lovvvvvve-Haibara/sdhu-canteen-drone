// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { getCurrentUser } from '../utils/auth'

const Login = () => import('../views/auth/Login.vue')
const Register = () => import('../views/auth/Register.vue')

const UserLayout = () => import('../views/user/UserLayout.vue')
const UserHome = () => import('../views/user/UserHome.vue')
const UserOrders = () => import('../views/user/OrderList.vue')

const AdminLayout = () => import('../views/admin/AdminLayout.vue')
const Dashboard = () => import('../views/admin/Dashboard.vue')
const CanteenManage = () => import('../views/admin/CanteenManage.vue')
const UserManage = () => import('../views/admin/UserManage.vue')
const AdminOrderList = () => import('../views/admin/AdminOrderList.vue')
const DroneAssign = () => import('../views/admin/DroneAssign.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: { public: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: Register,
      meta: { public: true }
    },
    {
      path: '/user',
      component: UserLayout,
      meta: {
        requiresAuth: true,
        roles: ['CUSTOMER']
      },
      children: [
        {
          path: 'home',
          name: 'UserHome',
          component: UserHome
        },
        {
          path: 'orders',
          name: 'UserOrders',
          component: UserOrders
        },
        {
          path: '',
          redirect: { name: 'UserHome' }
        }
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: {
        requiresAuth: true,
        roles: ['ADMIN', 'CANTEEN']
      },
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: Dashboard
        },
        {
          path: 'canteen',
          name: 'CanteenManage',
          component: CanteenManage
        },
        {
          path: 'users',
          name: 'UserManage',
          component: UserManage
        },
        {
          path: 'orders',
          name: 'AdminOrderList',
          component: AdminOrderList
        },
        {
          path: 'drone-assign',
          name: 'DroneAssign',
          component: DroneAssign
        },
        {
          path: '',
          redirect: { name: 'AdminDashboard' }
        }
      ]
    },
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    }
  ]
})

// 全局前置守卫：登录 & 权限
router.beforeEach((to, from, next) => {
  if (to.meta.public) {
    return next()
  }

  const user = getCurrentUser()

  if (!user) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  if (to.meta.roles && !to.meta.roles.includes(user.role)) {
    // 角色不匹配，根据角色跳转到各自首页
    if (user.role === 'CUSTOMER') {
      return next('/user/home')
    } else {
      return next('/admin/dashboard')
    }
  }

  next()
})

export default router
