<template>
  <el-card shadow="never">
    <template #header>
      <div class="header-bar">
        <span>我的订单</span>
        <el-select
          v-model="state.status"
          placeholder="全部状态"
          clearable
          size="small"
          style="width: 160px"
          @change="loadOrders"
        >
          <el-option label="待确认" value="PENDING" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="配送中" value="SHIPPED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELED" />
        </el-select>
      </div>
    </template>

    <el-table
      v-loading="state.loading"
      :data="state.orders"
      size="small"
      stripe
    >
      <el-table-column prop="id" label="订单号" width="90" />
      <el-table-column prop="canteenName" label="食堂" min-width="120" />
      <el-table-column
        label="金额"
        width="90"
        :formatter="(_, __, row) => '￥' + ((row.amountCent || 0) / 100).toFixed(2)"
      />
      <el-table-column prop="statusLabel" label="状态" width="100" />
      <el-table-column
        prop="deliveryMethod"
        label="配送方式"
        width="100"
        :formatter="(_, __, row) =>
          row.deliveryMethod === 'DRONE' ? '无人机' : '人工'"
      />
      <el-table-column prop="createdAt" label="下单时间" min-width="160" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button type="text" size="small" @click="viewDetail(row)">
            详情
          </el-button>
          <el-button
            v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'"
            type="text"
            size="small"
            @click="onCancel(row)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="table-footer">
      <el-pagination
        background
        layout="prev, pager, next"
        :page-size="state.pageSize"
        :current-page="state.page + 1"
        :total="state.total"
        @current-change="onPageChange"
      />
    </div>

    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="520px"
      :destroy-on-close="true"
    >
      <div v-if="currentOrder">
        <p>订单号：{{ currentOrder.id }}</p>
        <p>食堂：{{ currentOrder.canteenName }}</p>
        <p>配送地址：{{ currentOrder.deliveryAddress }}</p>
        <p>
          金额：￥{{ ((currentOrder.amountCent || 0) / 100).toFixed(2) }}
        </p>
        <p>状态：{{ currentOrder.statusLabel }}</p>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentUser } from '../../utils/auth'
import { getMyOrders, cancelOrder } from '../../api/order'
import { getOrderDetail } from '../../api/order'

const user = getCurrentUser()

const state = reactive({
  loading: false,
  status: '',
  page: 0,
  pageSize: 10,
  total: 0,
  orders: []
})

const detailVisible = ref(false)
const currentOrder = ref(null)

const loadOrders = async () => {
  if (!user) return
  state.loading = true
  try {
    const pageData = await getMyOrders({
      userId: user.id,
      status: state.status || undefined,
      page: state.page,  
      size: state.pageSize
    })
    state.orders = pageData.content || []
    state.total = pageData.totalElements || 0
  } catch {
    state.orders = []
    state.total = 0
  } finally {
    state.loading = false
  }
}

const onPageChange = (page) => {
  state.page = page - 1
  loadOrders()
}

const viewDetail = async (row) => {
  const detail = await getOrderDetail(row.id)
  currentOrder.value = detail
  detailVisible.value = true
}

const onCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '提示', {
      type: 'warning'
    })
  } catch {
    return
  }

  await cancelOrder(row.id, '用户主动取消')
  ElMessage.success('订单已取消')
  loadOrders()
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
