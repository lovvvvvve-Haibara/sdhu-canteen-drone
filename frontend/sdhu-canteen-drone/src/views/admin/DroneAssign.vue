<!-- src/views/admin/DroneAssign.vue -->
<template>
  <div class="page">
    <!-- 顶部筛选工具栏 -->
    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar">
        <div class="left">
          <!-- 食堂选择 -->
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

          <!-- 订单状态筛选 -->
          <el-select
            v-model="state.orderStatus"
            placeholder="订单状态"
            clearable
            size="small"
            style="width: 160px; margin-left: 12px"
            @change="loadOrders"
          >
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="配送中" value="SHIPPED" />
          </el-select>
        </div>

        <div class="right">
          <!-- 无人机状态筛选 -->
          <el-select
            v-model="state.droneStatus"
            placeholder="无人机状态"
            size="small"
            style="width: 160px"
            @change="loadDrones"
          >
            <el-option label="空闲" value="IDLE" />
            <el-option label="执行任务中" value="IN_MISSION" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="全部" value="" />
          </el-select>
        </div>
      </div>
    </el-card>

    <div class="content-row">
      <!-- 左侧：订单列表 -->
      <el-card shadow="never" class="orders-card">
        <template #header>
          <div class="card-header">
            <span>需要无人机配送的订单</span>
          </div>
        </template>

        <el-table
          v-loading="state.loadingOrders"
          :data="state.orders"
          size="small"
          stripe
        >
          <el-table-column prop="id" label="订单号" width="80" />
          <el-table-column prop="customerName" label="顾客" min-width="120" />
          <el-table-column prop="deliveryAddress" label="配送地址" min-width="200" />
          <el-table-column
            label="金额"
            width="90"
            :formatter="(row) => '￥' + ((row.amountCent || 0) / 100).toFixed(2)"
          />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button
                type="text"
                size="small"
                @click="openAssignDialog(row)"
              >
                分配无人机
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="table-footer">
          <el-pagination
            background
            layout="prev, pager, next"
            :page-size="state.orderPageSize"
            :current-page="state.orderPage + 1"
            :total="state.orderTotal"
            @current-change="onOrderPageChange"
          />
        </div>
      </el-card>

      <!-- 右侧：无人机列表 -->
      <el-card shadow="never" class="drones-card">
        <template #header>
          <div class="card-header">
            <span>无人机列表</span>
            <el-button type="primary" size="small" @click="openCreateDialog">
              新增无人机
            </el-button>
          </div>
        </template>

        <el-table
          v-loading="state.loadingDrones"
          :data="state.drones"
          size="small"
          stripe
        >
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="code" label="编号" min-width="120" />
          <el-table-column prop="model" label="型号" min-width="120" />
          <el-table-column
            prop="maxPayloadKg"
            label="最大载重(kg)"
            width="120"
          />
          <el-table-column prop="battery" label="电量(%)" width="100" />
          <el-table-column prop="status" label="状态" width="110" />
        </el-table>

        <div class="table-footer">
          <el-pagination
            background
            layout="prev, pager, next"
            :page-size="state.dronePageSize"
            :current-page="state.dronePage + 1"
            :total="state.droneTotal"
            @current-change="onDronePageChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 分配无人机对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配无人机"
      width="480px"
      :destroy-on-close="true"
    >
      <div v-if="currentOrder">
        <p class="dialog-subtitle">
          订单号：{{ currentOrder.id }} ，顾客：{{ currentOrder.customerName }}
        </p>
        <p class="dialog-subtitle">
          配送地址：{{ currentOrder.deliveryAddress }}
        </p>
      </div>

      <el-form label-width="90px" style="margin-top: 12px">
        <el-form-item label="选择无人机">
          <el-select
            v-model="selectedDroneId"
            placeholder="请选择无人机"
            style="width: 100%"
          >
            <el-option
              v-for="d in state.drones"
              :key="d.id"
              :label="`${d.code}（${d.model}，载重${d.maxPayloadKg}kg）`"
              :value="d.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取 消</el-button>
          <el-button
            type="primary"
            :disabled="!selectedDroneId"
            @click="confirmAssign"
          >
            确 定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新增无人机对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新增无人机"
      width="480px"
      :destroy-on-close="true"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="90px"
      >
        <el-form-item label="编号" prop="code">
          <el-input v-model="createForm.code" placeholder="请输入无人机编号" />
        </el-form-item>

        <el-form-item label="型号" prop="model">
          <el-input v-model="createForm.model" placeholder="如 DJI-M300" />
        </el-form-item>

        <el-form-item label="最大载重" prop="maxPayloadKg">
          <el-input-number
            v-model="createForm.maxPayloadKg"
            :min="0.1"
            :max="100"
            :step="0.1"
            style="width: 100%"
          />
          <span style="margin-left: 4px">kg</span>
        </el-form-item>

        <el-form-item label="电量" prop="battery">
          <el-input-number
            v-model="createForm.battery"
            :min="0"
            :max="100"
            :step="1"
            style="width: 100%"
          />
          <span style="margin-left: 4px">%</span>
        </el-form-item>

        <el-form-item label="位置">
          <el-input
            v-model="createForm.location"
            placeholder="如：东区食堂楼顶"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="createForm.note"
            type="textarea"
            :rows="3"
            placeholder="可填写飞行范围、维护说明等"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitCreateDrone">
            保 存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listCanteens } from '../../api/canteen'
import { getCanteenOrders, assignDroneToOrder } from '../../api/order'
import { listDrones, createDrone, changeDroneStatus } from '../../api/drone'

const state = reactive({
  canteens: [],
  selectedCanteenId: null,

  // 订单
  orders: [],
  loadingOrders: false,
  orderStatus: '',
  orderPage: 0,
  orderPageSize: 10,
  orderTotal: 0,

  // 无人机
  drones: [],
  loadingDrones: false,
  droneStatus: 'IDLE',
  dronePage: 0,
  dronePageSize: 10,
  droneTotal: 0
})

const assignDialogVisible = ref(false)
const currentOrder = ref(null)
const selectedDroneId = ref(null)

// 新建无人机弹窗 & 表单
const createDialogVisible = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  code: '',
  model: '',
  maxPayloadKg: 1,
  battery: 100,
  location: '',
  note: ''
})

const createRules = {
  code: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  model: [{ required: true, message: '请输入型号', trigger: 'blur' }],
  maxPayloadKg: [
    { required: true, message: '请输入最大载重', trigger: 'change' }
  ],
  battery: [
    { required: true, message: '请输入电量', trigger: 'change' }
  ]
}

const resetCreateForm = () => {
  createForm.code = ''
  createForm.model = ''
  createForm.maxPayloadKg = 1
  createForm.battery = 100
  createForm.location = ''
  createForm.note = ''
}

const openCreateDialog = () => {
  resetCreateForm()
  createDialogVisible.value = true
}

const submitCreateDrone = () => {
  if (!createFormRef.value) return
  createFormRef.value.validate(async (valid) => {
    if (!valid) return
    await createDrone({
      code: createForm.code,
      model: createForm.model,
      maxPayloadKg: createForm.maxPayloadKg,
      battery: createForm.battery,
      status: 'IDLE', // 新建默认空闲
      location: createForm.location,
      note: createForm.note
    })
    ElMessage.success('新增无人机成功')
    createDialogVisible.value = false
    await loadDrones()
  })
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
  state.loadingOrders = true
  try {
    const pageData = await getCanteenOrders(state.selectedCanteenId, {
      status: state.orderStatus || undefined,
      page: state.orderPage,
      size: state.orderPageSize
    })
    const all = pageData.content || []
    // 只取无人机配送的订单
    state.orders = all.filter((o) => o.deliveryMethod === 'DRONE' && o.status !== 'DELIVERED')
    state.orderTotal = pageData.totalElements || 0
  } finally {
    state.loadingOrders = false
  }
}

const loadDrones = async () => {
  state.loadingDrones = true
  try {
    const pageData = await listDrones({
      status: state.droneStatus || undefined,
      page: state.dronePage,
      size: state.dronePageSize
    })
    state.drones = pageData.content || []
    state.droneTotal = pageData.totalElements || 0
  } finally {
    state.loadingDrones = false
  }
}

const onCanteenChange = () => {
  state.orderPage = 0
  loadOrders()
}

const onOrderPageChange = (page) => {
  state.orderPage = page - 1
  loadOrders()
}

const onDronePageChange = (page) => {
  state.dronePage = page - 1
  loadDrones()
}

const openAssignDialog = (order) => {
  currentOrder.value = order
  selectedDroneId.value = null
  assignDialogVisible.value = true
  // 打开时刷新无人机列表，确保状态最新
  state.dronePage = 0
  loadDrones()
}

const confirmAssign = async () => {
  if (!currentOrder.value || !selectedDroneId.value) return
  // 1. 为订单分配无人机
  await assignDroneToOrder(currentOrder.value.id, selectedDroneId.value)
  // 2. 将该无人机状态修改为 IN_MISSION
  await changeDroneStatus(selectedDroneId.value, 'IN_MISSION')
  ElMessage.success('已为该订单分配无人机')
  assignDialogVisible.value = false
  await loadOrders()
  await loadDrones()
}

onMounted(async () => {
  await loadCanteens()
  await Promise.all([loadOrders(), loadDrones()])
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

.left,
.right {
  display: flex;
  align-items: center;
}

.content-row {
  display: grid;
  grid-template-columns: 2fr 1.8fr;
  gap: 16px;
}

.orders-card,
.drones-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.dialog-subtitle {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}
</style>
