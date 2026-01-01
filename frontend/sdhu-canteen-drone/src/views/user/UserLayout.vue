<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo">食堂点餐 · 用户端</div>
      <div class="header-right">
        <span class="user-name">{{ user?.displayName }}</span>
        <el-button type="text" @click="logout">退出登录</el-button>
      </div>
    </el-header>
    <div class="nav-wrap">
      <el-menu
        mode="horizontal"
        :default-active="activeMenu"
        class="nav-menu"
        router
      >
        <el-menu-item index="/user/home">点餐大厅</el-menu-item>
        <el-menu-item index="/user/orders">我的订单</el-menu-item>
        <el-menu-item index="/user/order-query">订单查询</el-menu-item>
      </el-menu>
    </div>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCurrentUser, clearAuth } from '../../utils/auth'

const router = useRouter()
const route = useRoute()
const user = computed(() => getCurrentUser())
const activeMenu = computed(() => route.path)

const logout = () => {
  clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.06);
  border-bottom: 1px solid #f0f2f5;
}

.logo {
  font-weight: 600;
  font-size: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-size: 14px;
  color: #606266;
}

.main {
  background: #f7f8fb;
  padding: 20px;
}

.nav-wrap {
  background: #ffffff;
  border-bottom: 1px solid #eef1f6;
}

.nav-menu {
  padding: 0 16px;
  border-bottom: none;
}
</style>
