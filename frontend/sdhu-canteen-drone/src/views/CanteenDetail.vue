<template>
  <div class="page">
    <el-page-header @back="goBack">
      <template #content>
        <span>{{ canteen?.name || '食堂详情' }}</span>
      </template>
      <template #extra>
        <el-space>
          <span>{{ canteen?.location }}</span>
          <el-tag v-if="canteen" :type="canteen.openStatus === 'OPEN' ? 'success' : 'info'">
            {{ canteen.openStatus === 'OPEN' ? '营业中' : '已关闭' }}
          </el-tag>
          <el-button type="primary" @click="goMyOrders">我的订单</el-button>
        </el-space>
      </template>
    </el-page-header>

    <div class="layout">
      <div class="left">
        <h3>分类</h3>
        <el-menu
          :default-active="String(activeCategoryId || '')"
          class="category-menu"
          @select="onCategorySelect"
        >
          <el-menu-item index="">
            全部
          </el-menu-item>
          <el-menu-item
            v-for="cat in categories"
            :key="cat.id"
            :index="String(cat.id)"
          >
            {{ cat.name }}
          </el-menu-item>
        </el-menu>
      </div>

      <div class="right">
        <h3>菜品列表</h3>
        <div class="tool-row">
          <el-input
            v-model="keyword"
            placeholder="搜索菜品名"
            style="max-width: 260px"
            clearable
            @keyup.enter="fetchFoods"
          />
          <el-button type="primary" @click="fetchFoods">搜索</el-button>
        </div>

        <el-table :data="foods" v-loading="loadingFoods" border>
          <el-table-column prop="name" label="菜品" />
          <el-table-column prop="priceCent" label="价格(元)" width="120">
            <template #default="{ row }">
              {{ (row.priceCent || 0) / 100 }}
            </template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="100" />
          <el-table-column prop="onShelf" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.onShelf ? 'success' : 'info'">
                {{ row.onShelf ? '在售' : '已下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                :disabled="!row.onShelf || row.stock <= 0"
                @click="openOrderDialog(row)"
              >
                下单
              </el-button>
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
    </div>

    <!-- 简单下单弹窗：一次点一个菜 -->
    <el-dialog
      v-model="orderDialogVisible"
      :title="selectedFood ? `下单：${selectedFood.name}` : '下单'"
      width="420px"
    >
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="数量">
          <el-input-number v-model="orderForm.qty" :min="1" :max="selectedFood?.stock || 99" />
        </el-form-item>
        <el-form-item label="配送方式">
          <el-radio-group v-model="orderForm.deliveryMethod">
            <el-radio label="MANUAL">人工配送/自取</el-radio>
            <el-radio label="DRONE">无人机配送</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="orderForm.deliveryMethod === 'DRONE'" label="配送地址">
          <el-input
            v-model="orderForm.deliveryAddress"
            placeholder="请填写宿舍楼/具体地址"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="orderSubmitting" @click="submitOrder">
          提交订单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getCanteenById } from '../api/canteen'
import { listCategories, listFoods } from '../api/menu'
import { createOrder } from '../api/order'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const canteenId = Number(route.params.id)

const canteen = ref(null)
const categories = ref([])

const foods = ref([])
const loadingFoods = ref(false)

const page = ref(0)
const size = ref(10)
const total = ref(0)

const activeCategoryId = ref(null)
const keyword = ref('')

// 下单弹窗相关
const orderDialogVisible = ref(false)
const selectedFood = ref(null)
const orderSubmitting = ref(false)
const orderForm = reactive({
  qty: 1,
  deliveryMethod: 'MANUAL',
  deliveryAddress: '',
})

const fetchCanteen = async () => {
  canteen.value = await getCanteenById(canteenId)
}

const fetchCategories = async () => {
  const res = await listCategories(canteenId)
  // CanteenDto.CategoryList: { canteenId, categories: [ { id, name, ... } ] }
  categories.value = res.categories || []
}

const fetchFoods = async () => {
  loadingFoods.value = true
  try {
    const data = await listFoods(canteenId, {
      categoryId: activeCategoryId.value || undefined,
      keyword: keyword.value || undefined,
      page: page.value,
      size: size.value,
    })
    foods.value = data.content || []
    total.value = data.totalElements || 0
    page.value = data.number || 0
  } finally {
    loadingFoods.value = false
  }
}

const onCategorySelect = (index) => {
  activeCategoryId.value = index ? Number(index) : null
  page.value = 0
  fetchFoods()
}

const onPageChange = (p) => {
  page.value = p - 1
  fetchFoods()
}

const openOrderDialog = (food) => {
  selectedFood.value = food
  orderForm.qty = 1
  orderForm.deliveryMethod = 'MANUAL'
  orderForm.deliveryAddress = ''
  orderDialogVisible.value = true
}

const submitOrder = async () => {
  if (!userStore.user?.id) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  if (orderForm.deliveryMethod === 'DRONE' && !orderForm.deliveryAddress) {
    ElMessage.error('无人机配送需要填写配送地址')
    return
  }

  try {
    orderSubmitting.value = true
    const payload = {
      customerId: userStore.user.id,
      canteenId,
      deliveryMethod: orderForm.deliveryMethod,
      deliveryAddress:
        orderForm.deliveryMethod === 'DRONE' ? orderForm.deliveryAddress : '',
      items: [
        {
          foodId: selectedFood.value.id,
          qty: orderForm.qty,
        },
      ],
    }

    const orderDetail = await createOrder(payload)
    ElMessage.success(`下单成功，订单号：${orderDetail.id}`)
    orderDialogVisible.value = false
  } catch (e) {
    // 已提示
  } finally {
    orderSubmitting.value = false
  }
}

const goBack = () => router.back()
const goMyOrders = () => router.push({ name: 'MyOrders' })

onMounted(async () => {
  await fetchCanteen()
  await fetchCategories()
  await fetchFoods()
})

// 如果路由参数变化（从一个食堂跳到另一个）
watch(
  () => route.params.id,
  async (newId) => {
    if (!newId) return
    const id = Number(newId)
    if (Number.isNaN(id)) return
    // 更新 canteenId 并重新加载
    router.replace({ name: 'CanteenDetail', params: { id } })
  }
)
</script>

<style scoped>
.layout {
  margin-top: 16px;
  display: flex;
  gap: 16px;
}

.left {
  width: 200px;
}

.right {
  flex: 1;
}

.category-menu {
  border-right: none;
}

.tool-row {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
