<template>
  <div class="page">
    <el-page-header content="我的订单" @back="goBack">
      <template #extra>
        <el-space>
          <el-button @click="goCanteens">去点餐</el-button>
        </el-space>
      </template>
    </el-page-header>

    <div class="filter-row">
      <el-select v-model="status" placeholder="订单状态" clearable style="width: 220px">
        <el-option label="全部" :value="null" />
        <el-option label="待处理" value="PENDING" />
        <el-option label="已确认" value="CONFIRMED" />
        <el-option label="已打包" value="PACKED" />
        <el-option label="配送中" value="SHIPPED" />
        <el-option label="已送达" value="DELIVERED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELED" />
      </el-select>
      <el-button type="primary" @click="fetchData">刷新</el-button>
    </div>

    <el-table :data="orders" v-loading="loading" border>
      <el-table-column prop="id" label="订单号" width="120" />
      <el-table-column prop="canteenName" label="食堂" />
      <el-table-column prop="firstFoodName" label="主要菜品" />
      <el-table-column prop="amountCent" label="金额(元)" width="120">
        <template #default="{ row }">
          {{ ((row.amountCent || 0) / 100).toFixed(2) }}
        </template>
      </el-table-column>

      <el-table-column prop="status" label="状态" width="130">
        <template #default="{ row }">
          <el-tag>
            <!-- 优先用后端返回的中文 statusLabel，没有就用本地映射，再不行用原始枚举 -->
            {{ row.statusLabel || statusTextMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="deliveryMethod" label="配送方式" width="130">
        <template #default="{ row }">
          {{ row.deliveryMethod === 'DRONE' ? '无人机' : '人工/自取' }}
        </template>
      </el-table-column>

      <el-table-column prop="deliveryAddress" label="配送地址" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
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
import { listMyOrders } from '../api/order'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const orders = ref([])

const page = ref(0)
const size = ref(10)
const total = ref(0)

// 筛选用的“枚举值”状态
const status = ref(null)

// 本地备用的状态 -> 中文映射（防止后端暂时没加 statusLabel 时也能显示中文）
const statusTextMap = {
  PENDING: '待处理',
  CONFIRMED: '已确认',
  PACKED: '已打包',
  SHIPPED: '配送中',
  DELIVERED: '已送达',
  COMPLETED: '已完成',
  CANCELED: '已取消',
}

const fetchData = async () => {
  if (!userStore.user?.id) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  loading.value = true
  try {
    const data = await listMyOrders({
      userId: userStore.user.id,
      status: status.value || undefined, // 这里传的是枚举值，不是中文
      page: page.value,
      size: size.value,
    })
    orders.value = data.content || []
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

const goBack = () => router.back()
const goCanteens = () => router.push('/canteens')

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
