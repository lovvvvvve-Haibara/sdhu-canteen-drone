<!-- src/views/admin/AdminLayout.vue -->
<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">食堂点餐 · 管理端</div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        router
      >
        <el-menu-item index="/admin/dashboard">仪表盘</el-menu-item>
        <el-menu-item index="/admin/canteen">食堂 / 菜品管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/drone-assign">无人机分配</el-menu-item>

      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-right">
          <span class="user-name">{{ user?.displayName }} ({{ user?.role }})</span>
          <el-button type="text" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCurrentUser, clearAuth } from '../../utils/auth'

const route = useRoute()
const router = useRouter()

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

.aside {
  background: #ffffff;
  color: #303133;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #eef1f6;
}

.logo {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  font-weight: 600;
  font-size: 15px;
  border-bottom: 1px solid #eef1f6;
}

.menu {
  border-right: none;
  flex: 1;
  background: transparent;
}

.header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  background: #ffffff;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.06);
  border-bottom: 1px solid #f0f2f5;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  font-size: 13px;
  color: #606266;
}

.main {
  background: #f7f8fb;
  padding: 20px;
}
</style>
