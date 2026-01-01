<template>
  <div class="dashboard">
    <div class="top-row">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-title">用户总数</div>
        <div class="stat-value">
          {{ stats.userCount }}
        </div>
        <div class="stat-desc">包含顾客与各类管理员</div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-title">食堂数量</div>
        <div class="stat-value">
          {{ stats.canteenCount }}
        </div>
        <div class="stat-desc">当前系统已接入的食堂</div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-title">订单总数（当前食堂）</div>
        <div class="stat-value">
          {{ stats.orderCount }}
        </div>
        <div class="stat-desc">
          <span v-if="currentCanteen">
            {{ currentCanteen.name }}
          </span>
          <span v-else>暂无食堂</span>
        </div>
      </el-card>
    </div>

    <el-card class="middle-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>订单概览</span>
          <el-select
            v-model="state.selectedCanteenId"
            placeholder="选择食堂"
            size="small"
            style="width: 220px"
            @change="reloadOrders"
          >
            <el-option
              v-for="c in state.canteens"
              :key="c.id"
              :label="c.name + '（' + (c.location || '') + '）'"
              :value="c.id"
            />
          </el-select>
        </div>
      </template>

      <el-table
        v-loading="state.loadingOrders"
        :data="state.recentOrders"
        size="small"
        stripe
      >
        <el-table-column prop="id" label="订单号" width="80" />
        <el-table-column prop="customerName" label="顾客" min-width="120" />
        <el-table-column
          label="金额"
          width="90"
          :formatter="(row) => '￥' + ((row.amountCent || 0) / 100).toFixed(2)"
        />
        <el-table-column
          prop="status"
          label="状态"
          width="110"
          :formatter="(row) => statusLabel(row.status)"
        />
        <el-table-column
          prop="createdAt"
          label="下单时间"
          min-width="160"
        />
      </el-table>

      <div
        v-if="!state.loadingOrders && state.recentOrders.length === 0"
        class="empty-tip"
      >
        当前食堂暂无订单数据
      </div>
    </el-card>
  </div>
</template>


<script setup>
import { reactive, computed, onMounted } from 'vue'
import { getUserList } from '../../api/user'
import { listCanteens } from '../../api/canteen'
import { getCanteenOrders } from '../../api/order'

const stats = reactive({
  userCount: 0,
  canteenCount: 0,
  orderCount: 0
})

const state = reactive({
  canteens: [],
  selectedCanteenId: null,
  recentOrders: [],
  loadingOrders: false
})

const currentCanteen = computed(() =>
  state.canteens.find((c) => c.id === state.selectedCanteenId)
)

const statusLabel = (status) => {
  switch (status) {
    case 'PENDING':
      return '待确认'
    case 'CONFIRMED':
      return '已确认'
    case 'SHIPPED':
      return '配送中'
    case 'COMPLETED':
      return '已完成'
    case 'CANCELLED':
      return '已取消'
    default:
      return status
  }
}

const loadUserCount = async () => {
  const pageData = await getUserList({
    page: 0,
    size: 1
  })
  stats.userCount = pageData.totalElements || 0
}

const loadCanteens = async () => {
  const pageData = await listCanteens({
    page: 0,
    size: 50
  })
  state.canteens = pageData.content || []
  stats.canteenCount = pageData.totalElements || state.canteens.length || 0
  if (state.canteens.length > 0 && !state.selectedCanteenId) {
    state.selectedCanteenId = state.canteens[0].id
  }
}

const loadOrdersForSelected = async () => {
  if (!state.selectedCanteenId) {
    stats.orderCount = 0
    state.recentOrders = []
    return
  }
  state.loadingOrders = true
  try {
    const pageData = await getCanteenOrders(state.selectedCanteenId, {
      page: 0,
      size: 10
    })
    stats.orderCount = pageData.totalElements || 0
    state.recentOrders = pageData.content || []
  } finally {
    state.loadingOrders = false
  }
}

const reloadOrders = () => {
  loadOrdersForSelected()
}

onMounted(async () => {
  await Promise.all([loadUserCount(), loadCanteens()])
  await loadOrdersForSelected()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.top-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 16px;
}

.stat-card {
  border-radius: 14px;
}

.stat-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-desc {
  font-size: 12px;
  color: #c0c4cc;
}

.middle-card {
  border-radius: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-tip {
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
  text-align: center;
}
</style>
