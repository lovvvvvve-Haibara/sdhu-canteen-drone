<template>
  <div class="page">
    <el-page-header content="食堂列表">
      <template #extra>
        <el-space>
          <span>当前用户：{{ userStore.user?.displayName || '未登录' }}</span>
          <el-button type="primary" @click="goMyOrders">我的订单</el-button>
          <el-button type="danger" @click="onLogout">退出登录</el-button>
        </el-space>
      </template>
    </el-page-header>

    <div class="filter-row">
      <el-select v-model="status" placeholder="营业状态" clearable style="width: 160px">
        <el-option label="全部" :value="null" />
        <el-option label="营业中" value="OPEN" />
        <el-option label="已关闭" value="CLOSED" />
      </el-select>
      <el-button type="primary" @click="fetchData">刷新</el-button>
    </div>

    <el-table :data="canteens" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="食堂名称" />
      <el-table-column prop="location" label="位置" />
      <el-table-column prop="openStatus" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.openStatus === 'OPEN' ? 'success' : 'info'">
            {{ row.openStatus === 'OPEN' ? '营业中' : '已关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button type="primary" link @click="goDetail(row.id)">进入菜单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :current-page="page + 1"
        :page-size="size"
        :total="total"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { listCanteens } from '../api/canteen'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const canteens = ref([])

const page = ref(0) // Spring Data Page：0-based
const size = ref(10)
const total = ref(0)

const status = ref(null) // CanteenOpenStatus: OPEN / CLOSED

const fetchData = async () => {
  loading.value = true
  try {
    const data = await listCanteens({
      page: page.value,
      size: size.value,
      status: status.value || undefined,
    })
    // Spring Data Page: { content, totalElements, number, size }
    canteens.value = data.content || []
    total.value = data.totalElements || 0
    page.value = data.number || 0
  } finally {
    loading.value = false
  }
}

const onPageChange = (p) => {
  page.value = p - 1
  fetchData()
}

const goDetail = (id) => {
  router.push({ name: 'CanteenDetail', params: { id } })
}

const goMyOrders = () => {
  router.push({ name: 'MyOrders' })
}

const onLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page {
  padding: 16px 24px;
}

.filter-row {
  margin: 16px 0;
  display: flex;
  gap: 12px;
  align-items: center;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
