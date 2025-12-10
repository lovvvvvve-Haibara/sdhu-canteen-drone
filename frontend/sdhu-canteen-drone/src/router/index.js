import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const Login = () => import('../views/Login.vue')
const CanteenList = () => import('../views/CanteenList.vue')
const CanteenDetail = () => import('../views/CanteenDetail.vue')
const MyOrders = () => import('../views/MyOrders.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: { public: true },
    },
    {
      path: '/',
      redirect: '/canteens',
    },
    {
      path: '/canteens',
      name: 'CanteenList',
      component: CanteenList,
    },
    {
      path: '/canteens/:id',
      name: 'CanteenDetail',
      component: CanteenDetail,
      props: true,
    },
    {
      path: '/orders',
      name: 'MyOrders',
      component: MyOrders,
    },
  ],
})

// 简单登录拦截：没登录只能访问 /login
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.public) {
    return next()
  }
  if (!userStore.isLoggedIn) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  next()
})

export default router
