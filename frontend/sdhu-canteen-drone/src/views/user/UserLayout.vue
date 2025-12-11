<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo">食堂点餐 · 用户端</div>
      <div class="header-right">
        <span class="user-name">{{ user?.displayName }}</span>
        <el-button type="text" @click="logout">退出登录</el-button>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser, clearAuth } from '../../utils/auth'

const router = useRouter()
const user = computed(() => getCurrentUser())

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
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
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
  background: #f5f7fa;
  padding: 16px;
}
</style>
