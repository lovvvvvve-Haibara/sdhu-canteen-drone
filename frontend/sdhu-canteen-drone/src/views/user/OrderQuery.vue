<template>
  <div class="order-query">
    <el-card shadow="never" class="query-card">
      <template #header>
        <div class="header">订单查询</div>
      </template>
      <div class="query-form">
        <el-input
          v-model="queryId"
          placeholder="请输入订单号"
          clearable
          @keyup.enter.native="onSearch"
        >
          <template #append>
            <el-button type="primary" :loading="loading" @click="onSearch">
              查询
            </el-button>
          </template>
        </el-input>
      </div>
    </el-card>

    <el-card v-if="orderDetail" shadow="never" class="detail-card">
      <template #header>
        <div class="header">订单详情</div>
      </template>
      <div class="detail-grid">
        <div class="detail-item">
          <span class="label">订单号</span>
          <span class="value">{{ orderDetail.id }}</span>
        </div>
        <div class="detail-item">
          <span class="label">食堂</span>
          <span class="value">{{ orderDetail.canteenName || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">配送地址</span>
          <span class="value">{{ orderDetail.deliveryAddress || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">配送方式</span>
          <span class="value">
            {{ orderDetail.deliveryMethod === 'DRONE' ? '无人机' : '人工' }}
          </span>
        </div>
        <div class="detail-item">
          <span class="label">状态</span>
          <span class="value">{{ orderDetail.statusLabel || orderDetail.status }}</span>
        </div>
        <div class="detail-item">
          <span class="label">金额</span>
          <span class="value">￥{{ amountText }}</span>
        </div>
        <div class="detail-item">
          <span class="label">下单时间</span>
          <span class="value">{{ orderDetail.createdAt || '-' }}</span>
        </div>
      </div>
    </el-card>

    <el-card v-if="orderDetail" shadow="never" class="timeline-card">
      <template #header>
        <div class="header">订单时间轴</div>
      </template>
      <el-timeline v-if="timeline.length">
        <el-timeline-item
          v-for="item in timeline"
          :key="item.id || item.occurredAt"
          :timestamp="item.occurredAt"
        >
          <div class="timeline-item">
            <div class="timeline-title">{{ item.label || item.code }}</div>
            <div class="timeline-note" v-if="item.note">{{ item.note }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无时间轴记录" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrderDetail, getOrderTimeline } from '../../api/order'

const queryId = ref('')
const loading = ref(false)
const orderDetail = ref(null)
const timeline = ref([])

const statusTextMap = {
  PENDING: '待确认',
  CONFIRMED: '已确认',
  PACKED: '已打包',
  SHIPPED: '配送中',
  DELIVERED: '已送达',
  COMPLETED: '已完成',
  CANCELED: '已取消'
}

const amountText = computed(() => {
  const amount = orderDetail.value?.amountCent || 0
  return (amount / 100).toFixed(2)
})

const normalizeTimeline = (items) =>
  (items || []).map((item) => ({
    ...item,
    label: item.label || statusTextMap[item.code] || item.code
  }))

const onSearch = async () => {
  if (!queryId.value) {
    ElMessage.warning('请输入订单号')
    return
  }
  loading.value = true
  try {
    const detail = await getOrderDetail(queryId.value)
    orderDetail.value = detail
    const timeRes = await getOrderTimeline(queryId.value)
    timeline.value = normalizeTimeline(timeRes || [])
  } catch {
    orderDetail.value = null
    timeline.value = []
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.order-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.query-card,
.detail-card,
.timeline-card {
  border-radius: 12px;
}

.header {
  font-weight: 600;
}

.query-form {
  max-width: 420px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label {
  font-size: 12px;
  color: #909399;
}

.value {
  font-weight: 600;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.timeline-title {
  font-weight: 600;
}

.timeline-note {
  font-size: 12px;
  color: #909399;
}
</style>
