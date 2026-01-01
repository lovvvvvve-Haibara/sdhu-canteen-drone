<!-- src/views/admin/AdminOrderList.vue -->
<template>
  <div class="page">
    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar">
        <div class="left">
          <el-select
            v-model="state.selectedCanteenId"
            placeholder="选择食堂"
            style="min-width: 220px"
            @change="onCanteenChange"
          >
            <el-option
              v-for="c in state.canteens"
              :key="c.id"
              :label="c.name + '（' + (c.location || '') + '）'"
              :value="c.id"
            />
          </el-select>

          <el-select
            v-model="state.status"
            placeholder="全部状态"
            clearable
            size="small"
            style="width: 160px; margin-left: 12px"
            @change="loadOrders"
          >
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="配送中" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table
        v-loading="state.loading"
        :data="state.orders"
        size="small"
        stripe
      >
        <el-table-column prop="id" label="订单号" width="80" />
        <el-table-column prop="customerName" label="顾客" min-width="120" />
        <el-table-column prop="canteenName" label="食堂" min-width="140" />
        <el-table-column
          label="金额"
          width="90"
          :formatter="(row) => '￥' + ((row.amountCent || 0) / 100).toFixed(2)"
        />
        <el-table-column
          prop="deliveryMethod"
          label="配送方式"
          width="100"
          :formatter="(row) =>
            row.deliveryMethod === 'DRONE' ? '无人机' : '人工'"
        />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag
              size="small"
              :type="statusTagType(row.status)"
            >
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" min-width="160" />
        <el-table-column label="操作" min-width="320">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="viewDetail(row)">
              详情
            </el-button>

            <!-- 修改状态下拉 -->
            <el-dropdown @command="(cmd) => onStatusCommand(row, cmd)">
              <el-button type="text" size="small">
                修改状态
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="CONFIRMED">
                    标记为已确认
                  </el-dropdown-item>
                  <el-dropdown-item command="SHIPPED">
                    标记为配送中
                  </el-dropdown-item>
                  <el-dropdown-item command="COMPLETED">
                    标记为已完成
                  </el-dropdown-item>
                  <el-dropdown-item command="CANCELLED">
                    取消订单
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 开始配送（无人机起飞） -->
            <el-button
              v-if="row.deliveryMethod === 'DRONE' && row.status === 'CONFIRMED'"
              type="text"
              size="small"
              @click="handleStartDelivery(row)"
            >
              开始配送
            </el-button>

            <!-- 标记送达 -->
            <el-button
              v-if="row.deliveryMethod === 'DRONE' && row.status === 'SHIPPED'"
              type="text"
              size="small"
              @click="handleMarkDelivered(row)"
            >
              标记送达
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
    </el-card>

    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="640px"
      :destroy-on-close="true"
    >
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">
            {{ currentOrder.id }}
          </el-descriptions-item>
          <el-descriptions-item label="顾客">
            {{ currentOrder.customerName }}
          </el-descriptions-item>
          <el-descriptions-item label="食堂">
            {{ currentOrder.canteenName }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ statusLabel(currentOrder.status) }}
          </el-descriptions-item>
          <el-descriptions-item label="金额">
            ￥{{ ((currentOrder.amountCent || 0) / 100).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="配送方式">
            {{ currentOrder.deliveryMethod === 'DRONE' ? '无人机' : '人工' }}
          </el-descriptions-item>
          <el-descriptions-item label="配送地址" :span="2">
            {{ currentOrder.deliveryAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">
            {{ currentOrder.createdAt }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentOrder.remarks || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-top: 16px">菜品列表</h4>
        <el-table
          :data="currentOrder.items || []"
          size="small"
          border
        >
          <el-table-column prop="foodName" label="菜品" min-width="160" />
          <el-table-column prop="qty" label="数量" width="80" />
          <el-table-column
            label="单价"
            width="100"
            :formatter="(row) => '￥' + ((row.priceCent || 0) / 100).toFixed(2)"
          />
          <el-table-column
            label="小计"
            width="100"
            :formatter="(row) =>
              '￥' + (((row.priceCent || 0) * row.qty) / 100).toFixed(2)"
          />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCanteens } from '../../api/canteen'
import {
  getCanteenOrders,
  updateOrderStatus,
  getOrderDetail,
  startDelivery,
  markDelivered
} from '../../api/order'
import { changeDroneStatus } from '../../api/drone'  // ⭐ 新增：引入修改无人机状态的 API

const state = reactive({
  canteens: [],
  selectedCanteenId: null,
  status: '',
  orders: [],
  loading: false,
  page: 0,
  pageSize: 10,
  total: 0
})

const detailVisible = ref(false)
const currentOrder = ref(null)

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

const statusTagType = (status) => {
  switch (status) {
    case 'PENDING':
      return 'info'
    case 'CONFIRMED':
      return 'warning'
    case 'SHIPPED':
      return 'primary'
    case 'COMPLETED':
      return 'success'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
  }
}

const loadCanteens = async () => {
  const pageData = await listCanteens({
    page: 0,
    size: 50
  })
  state.canteens = pageData.content || []
  if (state.canteens.length > 0 && !state.selectedCanteenId) {
    state.selectedCanteenId = state.canteens[0].id
  }
}

const loadOrders = async () => {
  if (!state.selectedCanteenId) return
  state.loading = true
  try {
    const pageData = await getCanteenOrders(state.selectedCanteenId, {
      status: state.status || undefined,
      page: state.page,
      size: state.pageSize
    })
    state.orders = pageData.content || []
    state.total = pageData.totalElements || 0
  } finally {
    state.loading = false
  }
}

const onCanteenChange = () => {
  state.page = 0
  loadOrders()
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

const onStatusCommand = async (row, status) => {
  const label = statusLabel(status)
  try {
    await ElMessageBox.confirm(
      `确定将订单【${row.id}】状态修改为「${label}」？`,
      '修改订单状态',
      { type: 'warning' }
    )
  } catch {
    return
  }

  await updateOrderStatus(row.id, status)
  ElMessage.success('订单状态已更新')
  loadOrders()
}

const handleStartDelivery = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定将订单【${row.id}】标记为开始配送（无人机起飞）？`,
      '开始配送',
      { type: 'warning' }
    )
  } catch {
    return
  }

  await startDelivery(row.id)
  ElMessage.success('已标记开始配送')
  loadOrders()
}

const handleMarkDelivered = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定将订单【${row.id}】标记为已送达？`,
      '标记送达',
      { type: 'warning' }
    )
  } catch {
    return
  }

  // 1. 先标记订单送达
  await markDelivered(row.id)

  // 2. 如果是无人机配送且行里带有 droneId，则把无人机状态改回 IDLE
  if (row.deliveryMethod === 'DRONE' && row.droneId) {
    await changeDroneStatus(row.droneId, 'IDLE')
  }

  ElMessage.success('已标记送达')
  loadOrders()
}

onMounted(async () => {
  await loadCanteens()
  if (state.selectedCanteenId) {
    await loadOrders()
  }
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar-card {
  border-radius: 12px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left {
  display: flex;
  align-items: center;
}

.table-footer {
  margin-top: 12px;
  display: flex;
 justify-content: flex-end;
}
</style>
